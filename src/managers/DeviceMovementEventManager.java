package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 *  @author Joel Seidel
 *
 *  Data manager for device movement events
 */


public class DeviceMovementEventManager extends MovementEventManager {
    private DatabaseInteraction database;
    public DeviceMovementEventManager() { database = new DatabaseInteraction(DatabaseType.AppData); }
    public MovementStatus getCurrentStatus(MaverickItem item){
        String getCurrentStatusSql = "SELECT * FROM device_movements WHERE mid = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement currentStatusStmt = database.prepareStatement(getCurrentStatusSql);
        try{
            currentStatusStmt.setString(1, item.getMaverickID());
            ResultSet currentStatusResult = database.query(currentStatusStmt);
            if(currentStatusResult.next()){
                String movementTypeStr = currentStatusResult.getString("movementtype");
                MovementType movementType = parseMovementType(movementTypeStr);
                return convertToMovementStatus(movementType);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }
    public void commitMovement(DeviceMovementEvent committedEvent){
        String writeMovementEventSql = "INSERT INTO device_movements(mid, movementtype, cid movementtime) VALUES (?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            writeMovementEventStatement.setString(1, committedEvent.getItemId());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getCompanyID());
            database.nonQuery(writeMovementEventStatement);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }
    public void relatedDeviceMovementCommit(PalletMovementEvent palletMovementEvent){
        String getPalletRelatedDevicesSql = "SELECT * FROM table_itempalletmapping WHERE mlot = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement getPalletRelatedDevicesStatement = database.prepareStatement(getPalletRelatedDevicesSql);
        try{
            getPalletRelatedDevicesStatement.setString(1, palletMovementEvent.getPallet().getPalletID());
            ResultSet palletRelatedDevicesResults = database.query(getPalletRelatedDevicesStatement);
            //Create the master statement for batch item movement write and prepare
            String writeThisDeviceSql = "INSERT INTO device_movements(mid, movementtype, cid, movementtime) VALUES (?, ?, ?, NOW())";
            PreparedStatement writeThisDeviceStatement = database.prepareStatement(writeThisDeviceSql);
            //About to prepare a batch of prepared statements so auto commit needs to be off
            database.setAutoCommit(false);
            while(palletRelatedDevicesResults.next()){
                //Get the item id of the current item from query results
                String thisItemId = palletRelatedDevicesResults.getString("mid");
                //Create an individual device statement to be added to write batch
                writeThisDeviceStatement.setString(1, thisItemId);
                writeThisDeviceStatement.setString(2, MovementEventManager.movementTypeToString(palletMovementEvent.getType()));
                writeThisDeviceStatement.setString(3, palletMovementEvent.getCompanyID());
                //Add this device statement to the batch
                writeThisDeviceStatement.addBatch();
            }
            //Execute created nonquery batch
            database.batchNonQuery(writeThisDeviceStatement);
            database.commitBatches();
            //Reset database connection auto commit property now that the batch is completed
            database.setAutoCommit(true);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }
    public List<DeviceMovementEvent> getMovements(MaverickItem maverickItem){
        String getItemMovementsSql = "SELECT * FROM device_movements WHERE mid = ? ORDER BY movementtime DESC";
        PreparedStatement getItemMovementsStatement = database.prepareStatement(getItemMovementsSql);
        try{
            getItemMovementsStatement.setString(1, maverickItem.getMaverickID());
            ResultSet getItemMovementsResults = database.query(getItemMovementsStatement);
            List<DeviceMovementEvent> deviceMovementEvents = new ArrayList<>();
            while(getItemMovementsResults.next()){
                String mid = maverickItem.getMaverickID();
                MovementType movementType = MovementEventManager.parseMovementType(getItemMovementsResults.getString("movementtype"));
                String cid = getItemMovementsResults.getString("cid");
                Date movementTime = getItemMovementsResults.getDate("movementtime");
                DeviceMovementEvent thisMovementEvent = new DeviceMovementEvent(mid, cid, movementType, movementTime);
                deviceMovementEvents.add(thisMovementEvent);
            }
            return deviceMovementEvents;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }

    /**
     * Create the data record for the initial movement of an item which will active it and allow it to be moved
     * @param item the item that is being initialized
     */
    public void initializeItemMovement(MaverickItem item){
        //Create the SQL statement for inserting the initial movement record
        String createInitialMovementSql = "INSERT INTO device_movements(movementtype, cid, movementtime, mid) VALUES(?, ?, NOW(), ?)";
        PreparedStatement createInitialMovementStatement = database.prepareStatement(createInitialMovementSql);
        try{
            //Set the parameters of the nonquery
            createInitialMovementStatement.setString(1, MovementEventManager.movementTypeToString(MovementType.CycleIn));
            createInitialMovementStatement.setString(2, item.getCustomerID());
            createInitialMovementStatement.setString(3, item.getMaverickID());
            //Perform the nonquery and insert the movement record
            database.nonQuery(createInitialMovementStatement);
        } catch(SQLException sqlEx){
            //u failed nerd
            sqlEx.printStackTrace();
        }
    }

    public void initializeItemMovement(List<MaverickItem> items){
        database.setAutoCommit(false);
        PreparedStatement initItemMovementStmt = database.prepareStatement("INSERT INTO device_movements(movementtype, cid, movementtime, mid) VALUES(?, ?, NOW(), ?)");
        for(MaverickItem item : items){
            try{
                initItemMovementStmt.setString(1, MovementEventManager.movementTypeToString(MovementType.CycleIn));
                initItemMovementStmt.setString(2, item.getCustomerID());
                initItemMovementStmt.setString(3, item.getMaverickID());
                initItemMovementStmt.addBatch();
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
        database.batchNonQuery(initItemMovementStmt);
        database.commitBatches();
        database.setAutoCommit(true);
    }
}
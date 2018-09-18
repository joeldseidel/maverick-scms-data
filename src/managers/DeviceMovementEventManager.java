package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String writeMovementEventSql = "INSERT INTO device_movements(mid, movementtype, fromcid, tocid, movementtime) VALUES (?, ?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            writeMovementEventStatement.setString(1, committedEvent.getItemId());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getFromCid());
            writeMovementEventStatement.setString(4, committedEvent.getToCid());
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }
    public void relatedDeviceMovementCommit(PalletMovementEvent palletMovementEvent){
        String getPalletRelatedDevicesSql = "SELECT * FROM table_itempalletmapping WHERE mlot = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement getPalletRelatedDevicesStatement = database.prepareStatement(getPalletRelatedDevicesSql);
        try{
            getPalletRelatedDevicesStatement.setString(1, palletMovementEvent.getPallet().getPalletID());
            ResultSet palletRelatedDevicesResults = database.query(getPalletRelatedDevicesStatement);
            //Create the master statement for batch item movement write and prepare
            String writeThisDeviceSql = "INSERT INTO device_movements(mid, movementtype, fromcid, tocid, movementtime) VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement writeThisDeviceStatement = database.prepareStatement(writeThisDeviceSql);
            //About to prepare a batch of prepared statements so auto commit needs to be off
            database.setAutoCommit(false);
            while(palletRelatedDevicesResults.next()){
                //Get the item id of the current item from query results
                String thisItemId = palletRelatedDevicesResults.getString("mid");
                //Create an individual device statement to be added to write batch
                writeThisDeviceStatement.setString(1, thisItemId);
                writeThisDeviceStatement.setString(2, MovementEventManager.movementTypeToString(palletMovementEvent.getType()));
                writeThisDeviceStatement.setString(3, palletMovementEvent.getFromCompanyId());
                writeThisDeviceStatement.setString(4, palletMovementEvent.getToCompanyId());
                //Add this device statement to the batch
                writeThisDeviceStatement.addBatch();
            }
            //Execute created nonquery batch
            database.batchNonQuery(writeThisDeviceStatement);
            //Reset database connection auto commit property now that the batch is completed
            database.setAutoCommit(true);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }
}
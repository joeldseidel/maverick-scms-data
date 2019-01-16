package managers;

import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 *  @author Joel Seidel
 *  Data manager for device movement events
 */
public class DeviceMovementEventManager extends MovementEventManager {
    /**
     * Default constructor to initialize the database connection for manager
     */
    public DeviceMovementEventManager() { initDb(DatabaseType.AppData); }

    /**
     * Get movement status of a specified device
     * @param item maverick item representing the specified device
     * @return status of the specified device
     */
    public MovementStatus getCurrentStatus(MaverickItem item){
        //Create get movement status sql query
        String getCurrentStatusSql = "SELECT * FROM device_movements WHERE mid = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement currentStatusStmt = database.prepareStatement(getCurrentStatusSql);
        try{
            currentStatusStmt.setString(1, item.getMaverickID());
            //Perform get movement status sql query
            ResultSet currentStatusResult = database.query(currentStatusStmt);
            if(currentStatusResult.next()){
                //Get the most recent movement type string
                String movementTypeStr = currentStatusResult.getString("movementtype");
                //Convert the movement type string to a movement type object
                MovementType movementType = parseMovementType(movementTypeStr);
                //Convert most recent movement type to a movement status
                return convertToMovementStatus(movementType);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }

    /**
     * Write the device movement record to the database
     * @param committedEvent event instance to be written into the database
     * @return success / fail boolean
     */
    public boolean commitMovement(DeviceMovementEvent committedEvent){
        //Create the device movement record insert query
        String writeMovementEventSql = "INSERT INTO device_movements(mid, movementtype, cid, movementtime) VALUES (?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            //Prepare the device movement event statement
            writeMovementEventStatement.setString(1, committedEvent.getItemId());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getCompanyID());
            //Perform the nonquery
            database.nonQuery(writeMovementEventStatement);
            return true;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
    }


    //FIXME this needs to be moved to the pallet movement event class. Its really an egregious example of screwed up encapsulation
    /**
     * Write records of devices on the same pallet to mirror the movement of this device
     * @param palletMovementEvent the pallet movement event that invokes this method
     */
    public void relatedDeviceMovementCommit(PalletMovementEvent palletMovementEvent) {
        //Create the get related devices sql query
        String getPalletRelatedDevicesSql = "SELECT * FROM table_itempalletmapping WHERE mlot = ?";
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

    /**
     * Get the movement events of a specified device
     * @param maverickItem specified device object
     * @return a list of device movement event objects
     */
    public List<DeviceMovementEvent> getMovements(MaverickItem maverickItem){
        //Create get movement events sql query
        String getItemMovementsSql = "SELECT * FROM device_movements WHERE mid = ? ORDER BY movementtime DESC";
        PreparedStatement getItemMovementsStatement = database.prepareStatement(getItemMovementsSql);
        try{
            getItemMovementsStatement.setString(1, maverickItem.getMaverickID());
            //Perform get movement events sql query
            ResultSet getItemMovementsResults = database.query(getItemMovementsStatement);
            List<DeviceMovementEvent> deviceMovementEvents = new ArrayList<>();
            //Iterate through each of the movement event records to create a movement event object
            while(getItemMovementsResults.next()){
                //Get necessary fields to create a device movement event from record
                String mid = maverickItem.getMaverickID();
                MovementType movementType = MovementEventManager.parseMovementType(getItemMovementsResults.getString("movementtype"));
                String cid = getItemMovementsResults.getString("cid");
                Date movementTime = getItemMovementsResults.getDate("movementtime");
                //Instantiate the movement class from the fields
                DeviceMovementEvent thisMovementEvent = new DeviceMovementEvent(mid, cid, movementType, movementTime);
                //Add to device movement collection
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

    /**
     * Create the 'cycle in' movement of a device when it is imported
     * @param items list of items to be cycled in
     */
    public void initializeItemMovement(List<MaverickItem> items){
        //Toggle auto commit to allow the creation of a query batch
        database.setAutoCommit(false);
        //Create insert device movement sql query
        PreparedStatement initItemMovementStmt = database.prepareStatement("INSERT INTO device_movements(movementtype, cid, movementtime, mid) VALUES(?, ?, NOW(), ?)");
        //Iterate through each of the specified items to cycle in
        for(MaverickItem item : items){
            try{
                //Prepare cycle in nonquery statement
                initItemMovementStmt.setString(1, MovementEventManager.movementTypeToString(MovementType.CycleIn));
                initItemMovementStmt.setString(2, item.getCustomerID());
                initItemMovementStmt.setString(3, item.getMaverickID());
                //Add nonquery statement to the query batch
                initItemMovementStmt.addBatch();
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
        //Run query batch
        database.batchNonQuery(initItemMovementStmt);
        //Commit query batch to the database. complete batch transaction
        database.commitBatches();
        //Toggle auto commit back to default on
        database.setAutoCommit(true);
    }
}
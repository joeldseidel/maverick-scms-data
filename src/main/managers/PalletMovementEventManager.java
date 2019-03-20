package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pallet movement event data base interaction manager class
 * @author Joel Seidel
 */
public class PalletMovementEventManager extends MovementEventManager {
    /**
     * Default constructor to initialize database connection
     */
    public PalletMovementEventManager(DatabaseInteraction database){ this.database = database; }

    /**
     * Get current status of a pallet
     * @param pallet pallet to get status of
     * @return movement status of the pallet
     */
    public MovementStatus getCurrentStatus(MaverickPallet pallet){
        //Create get most recent pallet movement query
        String getCurrentStatusSql = "SELECT * FROM pallet_movements WHERE palletid = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement currentStatusStmt = database.prepareStatement(getCurrentStatusSql);
        System.out.println("Checking for PID " + pallet.getPalletID());
        try{
            //Perform get most recent pallet movement query
            currentStatusStmt.setString(1, pallet.getPalletID());
            ResultSet currentStatusResult = database.query(currentStatusStmt);
            if(currentStatusResult.next()){
                //Get field to convert to movement type and determine status
                String movementTypeStr = currentStatusResult.getString("movementtype");
                 System.out.println("Got movment type " + movementTypeStr);
                 //Convert movement type to movement status
                MovementType movementType = parseMovementType(movementTypeStr);
                return convertToMovementStatus(movementType);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }

    /**
     * Commit a pallet movement to the database
     * @param committedEvent movement to commit to the database
     */
    public void commitMovement(PalletMovementEvent committedEvent){
        //Create insert pallet movement query
        String writeMovementEventSql = "INSERT INTO pallet_movements(palletid, movementtype, cid, movementtime) VALUES (?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            //Perform insert pallet movement query
            writeMovementEventStatement.setString(1, committedEvent.getPallet().getPalletID());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getCompanyID());
            database.nonQuery(writeMovementEventStatement);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        //Perform the same move to each of the devices on the pallet
        updatePalletRelatedDevices(committedEvent);
    }

    /**
     * Set devices associated with a pallet to make the same motion that its related pallet made
     * @param committedEvent pallet movement event that invoked this method
     */
    private void updatePalletRelatedDevices(PalletMovementEvent committedEvent){
        //All of this occurs within the device movement event manager
        //LMAO pranked you thought it was gonna be processed here didn't you nerd
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager(database);
        deviceMovementEventManager.relatedDeviceMovementCommit(committedEvent);
    }

    /**
     * Get movement history of a specified pallet
     * @param pallet pallet to get the history of
     * @return a list of pallet movement events related to the specified pallet
     */
    public List<PalletMovementEvent> getMovements(MaverickPallet pallet){
        //Create the get pallet movement records query
        String getMovementsSql = "SELECT * FROM pallet_movements WHERE palletid = ? ORDER BY movementtime DESC";
        PreparedStatement getMovementsStatement = database.prepareStatement(getMovementsSql);
        try{
            //Perform get pallet movement records query
            getMovementsStatement.setString(1, pallet.getPalletID());
            ResultSet getMovementsResult = database.query(getMovementsStatement);
            List<PalletMovementEvent> palletMovementEvents = new ArrayList<>();
            while(getMovementsResult.next()){
                //Get fields to instantiate a pallet movement event object
                MovementType type = MovementEventManager.parseMovementType(getMovementsResult.getString("movementtype"));
                String cid = getMovementsResult.getString("cid");
                Date movementTime = getMovementsResult.getDate("movementtime");
                String opid = getMovementsResult.getString("operatorid");
                //Instantiate pallet movement event object
                PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(pallet.getPalletID(), type, cid, movementTime, opid);
                //Add to pallet movement event collection
                palletMovementEvents.add(thisPalletMovementEvent);
            }
            return palletMovementEvents;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }

    /**
     * Create the initial pallet movement record which will active the pallet and allow it to be moved
     * @param pallet the pallet to be initialized
     */
    public void initializePalletMovement(MaverickPallet pallet){
        //Create the SQL statement for the nonquery to insert the movement record
        String createInitialPalletMovementSql = "INSERT INTO pallet_movements(movementtype, cid, movementtime, palletid) VALUES (?, ?, NOW(), ?)";
        PreparedStatement createInitialPalletMovementStatement = database.prepareStatement(createInitialPalletMovementSql);
        try{
            //Set the insert nonquery parameters
            createInitialPalletMovementStatement.setString(1, movementTypeToString(MovementType.CycleIn));
            createInitialPalletMovementStatement.setString(2, pallet.getCustomerID());
            createInitialPalletMovementStatement.setString(3, pallet.getPalletID());
            //Perform the nonquery and insert the movement record
            database.nonQuery(createInitialPalletMovementStatement);
        } catch(SQLException sqlEx){
            //I'll take things that ain't it for 500 Alex
            sqlEx.printStackTrace();
        }
    }
}

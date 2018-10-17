package managers;

/*
 *  @author Joel Seidel
 */

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PalletMovementEventManager extends MovementEventManager {
    private DatabaseInteraction database;
    public PalletMovementEventManager(){ database = new DatabaseInteraction(DatabaseType.AppData); }
    public MovementStatus getCurrentStatus(MaverickPallet pallet){
        String getCurrentStatusSql = "SELECT * FROM pallet_movements WHERE palletid = ? ORDER BY movementtime DESC LIMIT 1";
        PreparedStatement currentStatusStmt = database.prepareStatement(getCurrentStatusSql);
        System.out.println("Checking for PID " + pallet.getPalletID());
        try{
            currentStatusStmt.setString(1, pallet.getPalletID());
            ResultSet currentStatusResult = database.query(currentStatusStmt);
            if(currentStatusResult.next()){
                String movementTypeStr = currentStatusResult.getString("movementtype");
                 System.out.println("Got movment type " + movementTypeStr);
                MovementType movementType = parseMovementType(movementTypeStr);
                return convertToMovementStatus(movementType);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }
    public void commitMovement(PalletMovementEvent committedEvent){
        String writeMovementEventSql = "INSERT INTO pallet_movements(palletid, movementtype, cid, movementtime) VALUES (?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            writeMovementEventStatement.setString(1, committedEvent.getPallet().getPalletID());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getCompanyID());
            database.nonQuery(writeMovementEventStatement);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        updatePalletRelatedDevices(committedEvent);
    }
    private void updatePalletRelatedDevices(PalletMovementEvent committedEvent){
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        deviceMovementEventManager.relatedDeviceMovementCommit(committedEvent);
    }
    public List<PalletMovementEvent> getMovements(MaverickPallet pallet){
        String getMovementsSql = "SELECT * FROM pallet_movements WHERE palletid = ? ORDER BY movementtime DESC";
        PreparedStatement getMovementsStatement = database.prepareStatement(getMovementsSql);
        try{
            getMovementsStatement.setString(1, pallet.getPalletID());
            ResultSet getMovementsResult = database.query(getMovementsStatement);
            List<PalletMovementEvent> palletMovementEvents = new ArrayList<>();
            while(getMovementsResult.next()){
                MovementType type = MovementEventManager.parseMovementType(getMovementsResult.getString("movementtype"));
                String cid = getMovementsResult.getString("cid");
                Date movementTime = getMovementsResult.getDate("movementtime");
                PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(pallet.getPalletID(), type, cid, movementTime);
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

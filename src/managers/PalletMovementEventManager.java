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
        try{
            currentStatusStmt.setString(1, pallet.getPalletID());
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
    public void commitMovement(PalletMovementEvent committedEvent){
        String writeMovementEventSql = "INSERT INTO pallet_movements(palletid, movementtype, fromcid, tocid, movementtime) VALUES (?, ?, ?, ?, NOW())";
        PreparedStatement writeMovementEventStatement = database.prepareStatement(writeMovementEventSql);
        try{
            writeMovementEventStatement.setString(1, committedEvent.getPallet().getPalletID());
            writeMovementEventStatement.setString(2, MovementEventManager.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getFromCompanyId());
            writeMovementEventStatement.setString(4, committedEvent.getToCompanyId());
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
                String fromCid = getMovementsResult.getString("fromcid");
                String toCid = getMovementsResult.getString("tocid");
                String movementTime = getMovementsResult.getString("movementtime");
                PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(pallet.getPalletID(), type, fromCid, toCid, Date.valueOf(movementTime));
                palletMovementEvents.add(thisPalletMovementEvent);
            }
            return palletMovementEvents;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return null;
    }
}

package managers;

/*
 *  @author Joel Seidel
 */

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}

package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 *  @author Joel Seidel
 *
 *  Data manager for device movements
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
}
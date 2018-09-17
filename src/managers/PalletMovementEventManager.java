package managers;

/*
    @author Joel Seidel
 */

import maverick_data.DatabaseInteraction;
import maverick_types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PalletMovementEventManager extends MovementEventPrototype {
    private DatabaseInteraction database;
    public PalletMovementEventManager(){ database = new DatabaseInteraction(DatabaseType.AppData); }
    public boolean isLegalMovement(PalletMovementEvent movementEvent, MovementType intendedMovementType){
        MovementStatus currentStatus = getCurrentStatus(movementEvent.getPallet());
        switch(currentStatus){
            case InTransit:
                //Pallet is currently in transit
                switch(intendedMovementType){
                    case CheckIn:
                        //Pallet can be moved from in transit to in storage, check in is legal
                        return true;
                    case CheckOut:
                        //Pallet cannot be moved from in transit to in transit, check out is illegal
                        return false;
                    case HoldDesignated:
                        //Pallet can be moved from in transit to on hold, hold designation is legal
                        return true;
                    case HoldReleased:
                        //Pallet can not be released from hold because it is in transit, hold release is illegal
                        return false;
                    case HoldMisplaced:
                        //Pallet cannot be cycled out from hold with a misplaced designation because it is in transit, hold misplaced designation is illegal
                        return false;
                    case HoldReworked:
                        //Pallet cannot be cycled out from hold with a reworked designation because it is in transit, hold rework designation is illegal
                        return false;
                    case CycleOut:
                        //Pallet cannot be cycled out while in transit, cycle out is illegal
                        return false;
                    default: return false;
                }
            case InStorage:
                //Pallet is currently in storage
                switch(intendedMovementType){
                    case CheckIn:
                        //Pallet cannot be moved from in storage to in storage, check in is illegal
                        return false;
                    case CheckOut:
                        //Pallet can be moved from in storage to in transit, check out is legal
                        return true;
                    case HoldDesignated:
                        //Pallet can be moved from in storage to on hold, hold designation is legal
                        return true;
                    case HoldReleased:
                        //Pallet can not be released from hold because it is in storage, hold release is illegal
                        return false;
                    case HoldMisplaced:
                        //Pallet cannot be cycled out from hold with a misplaced designation because it is in storage, hold misplaced designation is illegal
                        return false;
                    case HoldReworked:
                        //Pallet cannot be cycled out from hold with a reworked designation because it is in storage, hold rework designation is illegal
                        return false;
                    case CycleOut:
                        //Pallet can be cycled out from in storage
                        return true;
                    default: return false;
                }
            case OnHold:
                //Pallet is currently on hold
                switch(intendedMovementType){
                    case CheckIn:
                        //Pallet cannot be moved from on hold to in storage, check in is illegal
                        return false;
                    case CheckOut:
                        //Pallet cannot be moved from on hold to in transit, check out is illegal
                        return false;
                    case HoldDesignated:
                        //Pallet cannot be moved from on hold to on hold, hold designation is illegal
                        return false;
                    case HoldReleased:
                        //Pallet can by released from hold, hold release is legal
                        return true;
                    case HoldMisplaced:
                        //Pallet can be cycled out from hold with a misplaced designation, hold misplaced designation is legal
                        return true;
                    case HoldReworked:
                        //Pallet can be cycled out from hold with a reworked designation, hold reworked designation is legal
                        return true;
                    case CycleOut:
                        //Pallet cannot be cycled from hold without a designation
                        return false;
                    default: return false;
                }
            case CycledOut:
                //Pallet is currently cycled out
                //A pallet cannot be moved once it and its devices are cycled out
                return false;
            default: return false;
        }
    }
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
            writeMovementEventStatement.setString(2, MovementEventPrototype.movementTypeToString(committedEvent.getType()));
            writeMovementEventStatement.setString(3, committedEvent.getFromCompanyId());
            writeMovementEventStatement.setString(4, committedEvent.getToCompanyId());
            database.nonQuery(writeMovementEventStatement);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }
}

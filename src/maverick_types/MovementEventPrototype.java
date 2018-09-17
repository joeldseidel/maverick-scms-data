package maverick_types;

/*
 * @author Joel Seidel
 *
 * Super class for pallet movement events. Abstracts movement event mechanics
 */

public class MovementEventPrototype {
    public static MovementType parseMovementType(String movementTypeString){
        switch(movementTypeString){
            case "checkin":
                return MovementType.CheckIn;
            case "checkout":
                return MovementType.CheckOut;
            case "holddesignated":
                return MovementType.HoldDesignated;
            case "holdreleased":
                return MovementType.HoldReleased;
            case "holdmisplaced":
                return MovementType.HoldMisplaced;
            case "holdreworked":
                return MovementType.HoldReworked;
            case "cycleout":
                return MovementType.CycleOut;
            default:
                return null;
        }
    }
    public static String movementTypeToString(MovementType movementType){
        switch(movementType){
            case CheckIn:
                return "checkin";
            case CheckOut:
                return "checkout";
            case HoldDesignated:
                return "holddesignated";
            case HoldReleased:
                return "holdreleased";
            case HoldMisplaced:
                return "holdmisplaced";
            case HoldReworked:
                return "holdreworked";
            case CycleOut:
                return "cycleout";
            default: return null;
        }
    }
    public MovementStatus convertToMovementStatus(MovementType movementType){
        switch(movementType){
            case CheckIn:
                //Most recent move was a check in, pallet went from in transit to in storage, pallet is in storage currently
                return MovementStatus.InStorage;
            case CheckOut:
                //Most recent move was a check out, pallet went from in storage to in transit, pallet is in transit currently
                return MovementStatus.InTransit;
            case HoldDesignated:
                //Most recent move was a hold designation, pallet went from a status to on hold, pallet is in hold currently
                return MovementStatus.OnHold;
            case HoldReleased:
                //Most recent move was a hold release designation, pallet went from hold to in storage, pallet is currently in storage
                return MovementStatus.InStorage;
            case HoldMisplaced:
                //Most recent move was a hold misplaced designation, pallet went from on hold to cycled out, pallet is currently cycled out
                return MovementStatus.CycledOut;
            case HoldReworked:
                //Most recent move was a hold reworked designation, pallet went from on hold to cycled out, pallet is currently cycled out
                return MovementStatus.CycledOut;
            case CycleOut:
                //Most recent move was a cycle out, pallet went from a status to cycled out, pallet is currently cycled out
                return MovementStatus.CycledOut;
            default:
                return null;
        }
    }
}

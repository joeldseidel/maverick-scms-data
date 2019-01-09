package managers;

/*
 * @author Joel Seidel
 *
 * Super class for pallet and device movement events. Abstracts movement event mechanics
 */

import maverick_types.*;

public abstract class MovementEventManager extends ManagerPrototype {
    /**
     * Convert a movement type string to a movement type type
     * @param movementTypeString the string to convert to a type
     * @return the equivalent movement type type of the provided string
     */
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
            case "cyclein":
                return MovementType.CycleIn;
            default:
                return null;
        }
    }

    /**
     * Convert a movement type type to its equivalent string
     * @param movementType the type to convert to string
     * @return the string equivalent to the movement type type specified
     */
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
            case CycleIn:
                return "cyclein";
            default: return null;
        }
    }

    /**
     * Convert a movement type to a movement status based on last movement
     * @param movementType the movement to convert
     * @return the movement status determined from provided movement type
     */
    static MovementStatus convertToMovementStatus(MovementType movementType){
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
            case CycleIn:
                //Most recent move was a cycle in, pallet went from not existing to being indeterminate - this shouldn't happen
                return MovementStatus.InStorage;
            default:
                return null;
        }
    }

    /**
     * Convert a movement status type to its equivalent string
     * @param movementStatus the movement status to convert
     * @return the equivalent string of the provided movement status
     */
    public static String movementStatusToString(MovementStatus movementStatus){
        switch(movementStatus){
            case InTransit:
                return "In Transit";
            case InStorage:
                return "In Storage";
            case OnHold:
                return "On Hold";
            case CycledOut:
                return "Cycled Out";
             default:
                 return null;
        }
    }

    /**
     * Determine if the current movement status will allow the intended movement type to occur
     * THIS IS THE LOGICAL DETERMINATE OF A PALLET MOVEMENT'S VALIDITY
     * @param currentStatus the current movement status of the pallet
     * @param intendedMovementType the movement type of the intended movement
     * @return true if the movement is legal and false if the move is illegal
     */
    public boolean isLegalMovement(MovementStatus currentStatus, MovementType intendedMovementType) {
        switch (currentStatus) {
            case InTransit:
                //Pallet is currently in transit
                switch (intendedMovementType) {
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
                    default:
                        return false;
                }
            case InStorage:
                //Pallet is currently in storage
                switch (intendedMovementType) {
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
                    default:
                        return false;
                }
            case OnHold:
                //Pallet is currently on hold
                switch (intendedMovementType) {
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
                    default:
                        return false;
                }
            case CycledOut:
                //Pallet is currently cycled out
                //A pallet cannot be moved once it and its devices are cycled out
                return false;
            default:
                return false;
        }
    }
}
package maverick_types;

import managers.PalletMovementEventManager;

import java.util.Date;

public class PalletMovementEvent {
    private String palletid, companyID;
    private MovementType type;
    private Date movementTime;
    private String opid;
    private PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();

    /**
     * Construct a pallet movement event that has not occurred
     * @param palletid mlot number
     * @param type type of movement
     * @param companyID move to company id
     * @param opid operator id of user who made this movement
     */
    public PalletMovementEvent(String palletid, MovementType type, String companyID, String opid){
        this.palletid = palletid;
        this.type = type;
        this.companyID = companyID;
        this.opid = opid;
    }

    /**
     * Construct a pallet movement that has already occurred
     * @param palletid mlot number
     * @param type movement type
     * @param companyID move to company id
     * @param movementTime time the movement occurred
     * @param opid operator id of user who made this movement
     */
    public PalletMovementEvent(String palletid, MovementType type, String companyID, Date movementTime, String opid){
        this.palletid = palletid;
        this.type = type;
        this.companyID = companyID;
        this.movementTime = movementTime;
        this.opid = opid;
    }
    public boolean isValid(){
        MovementStatus currentStatus = palletMovementEventManager.getCurrentStatus(getPallet());
        System.out.println("Got current status " + currentStatus);
        if(!palletMovementEventManager.isLegalMovement(currentStatus, type)){
            System.out.println("Illegal movmement");
            return false;
        }
        System.out.println("Legal movmement");
        //Todo: implement checks for the companies
        return true;
    }
    public void commit(){
        palletMovementEventManager.commitMovement(this);
    }
    public MaverickPallet getPallet(){
        return new MaverickPallet(palletid);
    }
    public String getCompanyID(){
        return companyID;
    }
    public MovementType getType() {
        return type;
    }
    public Date getMovementTime(){ return movementTime; }
}

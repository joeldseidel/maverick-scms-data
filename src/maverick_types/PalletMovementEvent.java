package maverick_types;

import managers.PalletMovementEventManager;

import java.util.Date;

public class PalletMovementEvent {
    private String palletid, companyID;
    private MovementType type;
    private Date movementTime;
    private PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();
    public PalletMovementEvent(String palletid, MovementType type, String companyID){
        this.palletid = palletid;
        this.type = type;
        this.companyID = companyID;
    }
    public PalletMovementEvent(String palletid, MovementType type, String companyID, Date movementTime){
        this.palletid = palletid;
        this.type = type;
        this.companyID = companyID;
        this.movementTime = movementTime;
    }
    public boolean isValid(){
        MovementStatus currentStatus = palletMovementEventManager.getCurrentStatus(getPallet());
        if(!palletMovementEventManager.isLegalMovement(currentStatus, type)){
            return false;
        }
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

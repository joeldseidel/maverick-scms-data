package maverick_types;

import managers.DeviceMovementEventManager;

import java.util.Date;

public class DeviceMovementEvent {
    private String mid, companyId;
    private MovementType type;
    private Date movementTime;
    private DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
    public DeviceMovementEvent(String mid, String companyId, MovementType type){
        this.mid = mid;
        this.companyId = companyId;
        this.type = type;
    }
    public DeviceMovementEvent(String mid, String companyId, MovementType type, Date movementTime){
        this.mid = mid;
        this.companyId = companyId;
        this.type = type;
        this.movementTime = movementTime;
    }
    public boolean isValid(){
        MovementStatus currentStatus = deviceMovementEventManager.getCurrentStatus(getItem());
        if(!deviceMovementEventManager.isLegalMovement(currentStatus, type)){
            return false;
        }
        //Todo: implement checks for other things probably
        return true;
    }
    public void commit(){ deviceMovementEventManager.commitMovement(this); }

    public String getItemId(){
        return mid;
    }
    public String getCompanyId(){
        return companyId;
    }
    public MovementType getType(){
        return type;
    }
    public MaverickItem getItem(){
        return new MaverickItem(mid);
    }
    public Date getMovementTime() { return movementTime; }
}

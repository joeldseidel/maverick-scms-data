package maverick_types;

import managers.DeviceMovementEventManager;
import maverick_data.DatabaseInteraction;

import java.util.Date;

public class DeviceMovementEvent {
    private String mid, companyID;
    private MovementType type;
    private Date movementTime;
    private DeviceMovementEventManager deviceMovementEventManager;
    public DeviceMovementEvent(String mid, String companyID, MovementType type){
        this.mid = mid;
        this.companyID = companyID;
        this.type = type;
    }
    public DeviceMovementEvent(String mid, String companyID, MovementType type, Date movementTime){
        this.mid = mid;
        this.companyID = companyID;
        this.type = type;
        this.movementTime = movementTime;
    }
    public boolean isValid(){
        deviceMovementEventManager = new DeviceMovementEventManager(new DatabaseInteraction(DatabaseType.AppData));
        MovementStatus currentStatus = deviceMovementEventManager.getCurrentStatus(getItem());
        if(!deviceMovementEventManager.isLegalMovement(currentStatus, type)){
            return false;
        }
        //Todo: implement checks for other things probably
        return true;
    }
    public void commit(){ new DeviceMovementEventManager(new DatabaseInteraction(DatabaseType.AppData)).commitMovement(this); }

    public String getItemId(){
        return mid;
    }
    public String getCompanyID(){
        return companyID;
    }
    public MovementType getType(){
        return type;
    }
    public MaverickItem getItem(){
        return new MaverickItem(mid);
    }
    public Date getMovementTime() { return movementTime; }
}

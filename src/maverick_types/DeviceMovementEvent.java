package maverick_types;

import managers.DeviceMovementEventManager;

public class DeviceMovementEvent {
    private String mid, fromCompanyId, toCompanyId;
    private MovementType type;
    private DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
    public DeviceMovementEvent(String mid, String fromCompanyId, String toCompanyId, MovementType type){
        this.mid = mid;
        this.fromCompanyId = fromCompanyId;
        this.toCompanyId = toCompanyId;
        this.type = type;
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
    public String getFromCid(){
        return fromCompanyId;
    }
    public String getToCid(){
        return toCompanyId;
    }
    public MovementType getType(){
        return type;
    }
    public MaverickItem getItem(){
        return new MaverickItem(mid);
    }
}

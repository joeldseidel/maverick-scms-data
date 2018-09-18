package maverick_types;

public class DeviceMovementEvent {
    private String mid, fromCompanyId, toCompanyId;
    private MovementType type;
    public DeviceMovementEvent(String mid, String fromCompanyId, String toCompanyId, MovementType type){
        this.mid = mid;
        this.fromCompanyId = fromCompanyId;
        this.toCompanyId = toCompanyId;
        this.type = type;
    }
    public boolean isValid(){
        return false;
    }
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
}

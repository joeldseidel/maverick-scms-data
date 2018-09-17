package maverick_types;

import managers.PalletMovementEventManager;

public class PalletMovementEvent {
    private String palletid, fromCompanyId, toCompanyId;
    private MovementType type;
    PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();
    public PalletMovementEvent(String palletid, MovementType type, String fromCompanyId, String toCompanyId){
        this.palletid = palletid;
        this.type = type;
        this.fromCompanyId = fromCompanyId;
        this.toCompanyId = toCompanyId;
    }
    public boolean isValid(){
        if(!palletMovementEventManager.isLegalMovement(this, type)){
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
    public String getFromCompanyId(){
        return fromCompanyId;
    }
    public String getToCompanyId(){
        return toCompanyId;
    }
    public MovementType getType() {
        return type;
    }
}

package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.DeviceMovementEventManager;
import managers.MovementEventManager;
import maverick_types.MaverickItem;
import maverick_types.MovementStatus;
import org.json.JSONObject;

public class GetCurrentItemStatusHandler extends HandlerPrototype implements HttpHandler {
    public GetCurrentItemStatusHandler(){
        requiredKeys = new String[] {"mid", "cid", "token"};
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String mid = requestParams.getString("mid");
        String cid = requestParams.getString("cid");
        MaverickItem mItem = new MaverickItem(mid);
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        MovementStatus currentStatus = deviceMovementEventManager.getCurrentStatus(mItem);
        this.response = MovementEventManager.movementStatusToString(currentStatus);
    }
}

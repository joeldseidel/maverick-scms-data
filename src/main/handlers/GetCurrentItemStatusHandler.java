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
        handlerName = "GetCurrentItemStatusHandler";
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String mid = requestParams.getString("mid");
        String cid = requestParams.getString("cid");
        MaverickItem mItem = new MaverickItem(mid);
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        MovementStatus currentStatus = deviceMovementEventManager.getCurrentStatus(mItem);
        if(currentStatus == null){
            this.response = new JSONObject(0).put("status", "undefined pallet").toString();
        } else {
            String currentStatusString = MovementEventManager.movementStatusToString(currentStatus);
            this.response = new JSONObject().put("status", currentStatusString).toString();
        }
    }
}

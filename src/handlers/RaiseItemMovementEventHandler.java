package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import maverick_types.DeviceMovementEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class RaiseItemMovementEventHandler extends HandlerPrototype implements HttpHandler {

    public RaiseItemMovementEventHandler(){
        requiredKeys = new String[] {"mid", "type", "fromcid", "tocid", "token"};
        handlerName = "RaiseItemMovementEventHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Get params from request params objects
        String itemid = requestParams.getString("mid");
        String type = requestParams.getString("type");
        String fromcid = requestParams.getString("fromcid");
        String tocid = requestParams.getString("tocid");
        //Create item movement event object from parameters
        DeviceMovementEvent deviceMovementEvent = new DeviceMovementEvent(itemid, fromcid, tocid, MovementEventManager.parseMovementType(type));
        //Validate movement event and commit if valid
        if(deviceMovementEvent.isValid()){
            //Item movement event is valid and legal, commit to database
            deviceMovementEvent.commit();
            //Return successful message to client
            this.response = "success";
        } else {
            //Pallet movement request was invalid :(
            this.response = "invalid request";
        }
    }
}

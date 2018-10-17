package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import maverick_types.PalletMovementEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class RaisePalletMovementEventHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor for defining the required keys for the request parameters
     */
    public RaisePalletMovementEventHandler(){
        //Define the required keys in the super class
        requiredKeys = new String[] {"palletid", "type", "cid", "token"};
        handlerName = "RaisePalletMovementEventHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Get params from request params object
        String palletid = requestParams.getString("palletid");
        String type = requestParams.getString("type");
        String cid = requestParams.getString("cid");
        //Create a pallet movement object from parameters
        PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(palletid, MovementEventManager.parseMovementType(type), cid);
        //Validate and commit movement event
        if(thisPalletMovementEvent.isValid()){
            //Pallet movement event is valid and legal, commit to database
            thisPalletMovementEvent.commit();
            //Return successful message to client
            this.response = "success";
        } else {
            //Pallet movement was invalid
            this.response = "invalid request";
        }
    }
}

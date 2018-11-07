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
        JSONObject responseObject = new JSONObject();
        System.out.println("Entered pallet movement event!");
        //Get params from request params object
        String palletid = requestParams.getString("palletid");
        String type = requestParams.getString("type");
        String cid = requestParams.getString("cid");
        //Create a pallet movement object from parameters
        PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(palletid, MovementEventManager.parseMovementType(type), cid);
        //Validate and commit movement event
        System.out.println("Successfully made pallet movement event!");
        if(thisPalletMovementEvent.isValid()){
            System.out.println("Pallet mvmt valid");
            //Pallet movement event is valid and legal, commit to database
            thisPalletMovementEvent.commit();
            System.out.println("Successfully committed pallet movement event!");
            //Return successful message to client
            responseObject.put("message", "Success");
        } else {
            System.out.println("Pallet mvmt invalid");
            //Pallet movement was invalid
            responseObject.put("message", "InvalidType");
        }
        this.response = responseObject.toString();
    }
}

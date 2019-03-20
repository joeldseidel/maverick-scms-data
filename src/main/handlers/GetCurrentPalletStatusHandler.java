package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import managers.PalletMovementEventManager;
import maverick_types.DatabaseType;
import maverick_types.MaverickPallet;
import maverick_types.MovementStatus;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles requests for a pallet's current status, returns a string containing current pallet status
 *
 * @author Joel Seidel
 */

public class GetCurrentPalletStatusHandler extends HandlerPrototype implements HttpHandler {


    public GetCurrentPalletStatusHandler(){
        requiredKeys = new String[] {"mid", "cid", "token"};
        handlerName = "GetCurrentPalletStatusHandler";
        initDb(DatabaseType.AppData);
    }

    /**
     * Parse parameters, query for pallet status, convert status to response string
     * @param requestParams validated params from the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse parameters from client
        String mid = requestParams.getString("mid");
        String cid = requestParams.getString("cid");
        //Instantiate referenced pallet object
        MaverickPallet thisPallet = new MaverickPallet(cid, mid);
        //Instantiate pallet movement manager to get the status (not moving anything just accessing that kinda data)
        PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager(database);
        //Get the current movement status from manager query
        MovementStatus currentStatus = palletMovementEventManager.getCurrentStatus(thisPallet);
        if(currentStatus == null){
            this.response = new JSONObject().put("status", "undefined pallet").toString();
        } else {
            //Convert the movement status to string and return as response object
            this.response = new JSONObject().put("status", MovementEventManager.movementStatusToString(currentStatus)).toString();
        }
    }
}

package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import managers.PalletMovementEventManager;
import maverick_types.DatabaseType;
import maverick_types.MaverickPallet;
import maverick_types.PalletMovementEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Joel Seidel
 *
 * Handler for retrieving the history of a pallet by fetching and returning movement events from database
 */

public class GetPalletHistoryHandler extends HandlerPrototype implements HttpHandler{

    /**
     * Constructor to set the required keys inherited from super class on creation of handler context
     */
    public GetPalletHistoryHandler(){
        //Set inherited required keys property to unique handler required keys
        requiredKeys = new String[]{"palletid", "cid", "token"};
        handlerName = "GetPalletHistoryHandler";
        initDb(DatabaseType.AppData);
    }

    /**
     * Complete request action. Parse parameters, fetch data, format response
     * @param requestParams validated parameters from request
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse the request parameters
        String palletlot = requestParams.getString("palletid");
        String cid = requestParams.getString("cid");
        //Instantiate pallet object from request parameters
        MaverickPallet thisPallet = new MaverickPallet(palletlot, cid);
        //Instantiate pallet movement event manager to fetch pallet movement data from database
        PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager(database);
        //Get pallet movement event history on requested pallet
        List<PalletMovementEvent> palletMovementEvents = palletMovementEventManager.getMovements(thisPallet);
        //Format pallet movement event list into returnable JSON array
        JSONArray movementEventArray = formatRequestResponse(palletMovementEvents);
        //Create response to JSON object to enclose movement event JSON array
        JSONObject responseObject = new JSONObject();
        //Add movement event JSON array to response object
        responseObject.put("", movementEventArray);
        //Stringify response to object and return for writing to client
        this.response = responseObject.toString();
    }

    /**
     * Take list of pallet movement events and convert into a JSON array of JSON objects, each representing a pallet movement event
     * @param palletMovementEvents list of pallet movement events to be converted into JSON objects and put into the JSON array
     * @return JSON array of JSON objects, each representing a pallet movement event
     */
    private JSONArray formatRequestResponse(List<PalletMovementEvent> palletMovementEvents){
        //Create JSON array for all movement events to be stored in after conversion to JSON objects
        JSONArray movementEventJsonArray = new JSONArray();
        for(PalletMovementEvent event : palletMovementEvents){
            //Create JSON object for the current pallet movement event
            JSONObject thisEventObject = new JSONObject();
            //Get values from the movement event object and put into json object with corresponding key
            thisEventObject.put("movementtype", MovementEventManager.movementTypeToString(event.getType()));
            thisEventObject.put("fromcid", event.getCompanyID());
            thisEventObject.put("movementtime", event.getMovementTime().toString());
            //Add the created JSON object to the cumulative json array
            movementEventJsonArray.put(thisEventObject);
        }
        return movementEventJsonArray;
    }
}

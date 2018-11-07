package handlers;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.DeviceMovementEventManager;
import managers.MovementEventManager;
import maverick_types.DeviceMovementEvent;
import maverick_types.MaverickItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Joel Seidel
 */

public class GetItemHistoryHandler extends HandlerPrototype implements HttpHandler{

    /**
     * Constructor to set the required keys property inherited from the super class upon creation of the handler context
     */
    public GetItemHistoryHandler(){
        //Set inherited required keys property to unique handler required keys
        requiredKeys = new String[]{"mid", "token"};
        handlerName = "GetItemHistoryHandler";
    }

    /**
     * Complete request action. Parse parameters, fetch data, format resposne
     * @param requestParams validated parameters object from request
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse the request parameters
        String mid = requestParams.getString("mid");
        //Instantiate item object from request parameters
        MaverickItem thisItem = new MaverickItem(mid);
        //Instantiate item movement event manager to fetch device movement history records
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        //Get device movement history on requested device
        List<DeviceMovementEvent> deviceMovementEvents = deviceMovementEventManager.getMovements(thisItem);
        //Format device movement event list into returnable JSON array
        JSONArray movementEventArray = formatRequestResponse(deviceMovementEvents);
        //Create empty JSON object to hold the movement event array
        JSONObject responseObject = new JSONObject();
        //Insert the movement event json array into the returning object
        responseObject.put("", movementEventArray);
        //Stringify the response object and return as result
        this.response = responseObject.toString();
    }
    /**
     * Take list of device movement events and convert into a JSON array of JSON objects, each representing a device movement event
     * @param deviceMovementEvents list of device movement events to be converted into JSON objects and put into the JSON array
     * @return JSON array of JSON objects, each representing a device movement event
     */
    private JSONArray formatRequestResponse(List<DeviceMovementEvent> deviceMovementEvents){
        //Create JSON array for all movement events to be stored in after conversion to JSON objects
        JSONArray movementEventJsonArray = new JSONArray();
        for(DeviceMovementEvent event : deviceMovementEvents){
            //Create JSON object for the current pallet movement event
            JSONObject thisEventObject = new JSONObject();
            //Get values from the movement event object and put into json object with corresponding key
            thisEventObject.put("movementtype", MovementEventManager.movementTypeToString(event.getType()));
            thisEventObject.put("cid", event.getCompanyID());
            thisEventObject.put("movementtime", event.getMovementTime().toString());
            //Add the created JSON object to the cumulative json array
            movementEventJsonArray.put(thisEventObject);
        }
        return movementEventJsonArray;
    }
}

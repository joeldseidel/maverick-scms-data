package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import managers.PalletDataManager;
import managers.PalletMovementEventManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.MaverickPallet;
import maverick_types.PalletMovementEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PalletRequestHandler extends HandlerPrototype implements HttpHandler {
    public PalletRequestHandler(){
        handlerName = "Pallet Request Handler";
        requiredKeys = new String[] { "cmd" };
    }

    public PalletRequestHandler(String cmd){
        handlerName = "Pallet Request Handler : " + cmd;
        getRequiredKeys(cmd);
    }

    /**
     * Get the command keys that required for each of the command types
     * @param cmd command type name
     */
    private void getRequiredKeys(String cmd){
        switch(cmd){
            case "new":
                //Create a new pallet
                requiredKeys = new String[] { "cid", "op", "token" };
                break;
            case "add":
                //Add an item to a pallet
                requiredKeys = new String[] { "cid", "mid", "mlot", "op", "token" };
                break;
            case "edit":
                //Edit an existing pallet
            case "delete":
                //Delete a pallet
            case "move":
                //Move a pallet somewhere
            case "get":
                //Get a pallet and its items
                requiredKeys = new String[] { "cid", "mlot", "op", "token" };
                break;
        }
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Route the handler
        switch(handleCmd){
            case "new":
                createNewPallet(requestParams);
                break;
            case "add":
                addItemToPallet(requestParams);
                break;
            case "edit":
                editPallet(requestParams);
                break;
            case "delete":
                deletePallet(requestParams);
                break;
            case "move":
                movePallet(requestParams);
                break;
            case "get":
                getPallet(requestParams);
                break;

        }
    }

    private void createNewPallet(JSONObject requestParams){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String cid = requestParams.getString("cid");
        MaverickPallet thisPallet = new MaverickPallet(cid);
        PalletDataManager palletDataManager = new PalletDataManager(database);
        System.out.println("Created pallet and manager successfully");
        //PERFORM PALLET ADDING
        palletDataManager.addPallet(thisPallet);
        //Write response object
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }

    private void addItemToPallet(JSONObject requestParams){
        //TODO: figure out the add item to pallet mess
    }

    private void editPallet(JSONObject requestParams){
        //TODO: figure out the pallet editing mess
    }

    private void deletePallet(JSONObject requestParams){
        //TODO: figure out the delete pallet mess
    }
    private void movePallet(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();
        System.out.println("Entered pallet movement event!");
        //Get params from request params object
        String palletid = requestParams.getString("palletid");
        String type = requestParams.getString("type");
        String cid = requestParams.getString("cid");
        //Create a pallet movement object from parameters
        PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(palletid, MovementEventManager.parseMovementType(type), cid, "");
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
    private void getPallet(JSONObject requestParams){
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
        //Create response to JSON object to enclose movement event JSON array
        JSONObject responseObject = new JSONObject();
        //Add movement event JSON array to response object
        responseObject.put("", movementEventJsonArray);
        //Stringify response to object and return for writing to client
        this.response = responseObject.toString();
    }
}

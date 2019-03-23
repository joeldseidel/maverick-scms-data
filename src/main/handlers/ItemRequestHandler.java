package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.ItemDataManager;
import managers.MovementEventManager;
import maverick_types.DeviceMovementEvent;
import maverick_types.MaverickItem;
import org.json.JSONObject;

public class ItemRequestHandler extends HandlerPrototype implements HttpHandler {
    /**
     * Generic request constructor
     */
    public ItemRequestHandler(){
        handlerName = "Item Request Handler";
        requiredKeys = new String[] { "cmd" };
    }

    /**
     * Item request with a command string defined
     * Specify the required keys for each branch
     * @param cmd
     */
    public ItemRequestHandler(String cmd){
        handlerName = "Item Request Handler " + cmd;
        switch (cmd) {
            case "new":
                //Create a new item
                requiredKeys = new String[] { "fdaid", "name", "category", "cid", "op", "token" };
                break;
            case "get":
                //Get an item
                requiredKeys = new String[] { "mid", "cid", "op", "token" };
                break;
            case "edit":
                requiredKeys = new String[] { "cid", "mid", "field", "newvalue", "op", "token"};
                break;
            case "move":
                requiredKeys = new String[] { "mid", "type", "cid", "op", "token" };
                break;
            case "delete":
                requiredKeys = new String[] { "mid", "cid", "op", "token" };
                break;
        }
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        switch (handleCmd) {
            case "new":
                createNewItem(requestParams);
                break;
            case "get":
                getItem(requestParams);
                break;
            case "edit":
                editItem(requestParams);
                break;
            case "move":
                moveItem(requestParams);
                break;
            case "delete":
                deleteItem(requestParams);
                break;
        }
    }

    /**
     * @author Joshua Famous
     * Create a new item and save to database
     * @param requestParams Request parameters / object properties
     */
    private void createNewItem(JSONObject requestParams){
        //Parse the request parameters
        String fdaid = requestParams.getString("fdaid");
        String cid = requestParams.getString("cid");
        String name = requestParams.getString("name");
        String category = requestParams.getString("category");
        //Instantiate an item object from data received within request parameters
        MaverickItem thisItem = new MaverickItem(fdaid, name, category, cid);
        //Instantiate item data manager to interact with item data
        ItemDataManager itemDataManager = new ItemDataManager(database);
        //Write created item to the database
        itemDataManager.addItem(thisItem);
        //Create response object
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }

    /**
     * @author Joel Seidel
     * Handles requests to get a single item by its maverick lot number
     * @param requestParams Request arguments / item properties
     */
    private void getItem(JSONObject requestParams){
        //Parse params from the request parameters object
        String mlot = requestParams.getString("mid");
        //Instantiate item manager to get item from database with lot number
        ItemDataManager itemDataManager = new ItemDataManager(database);
        //Get maverick item from database by lot number
        MaverickItem thisItem = itemDataManager.getItem(mlot);
        //Format maverick object into a json object to stringify and send back as response
        //Create json object to format data
        JSONObject responseObject = new JSONObject();
        //Get data from maverick item and put into response object
        responseObject.put("mid", thisItem.getMaverickID());
        responseObject.put("fdaid", thisItem.getFdaID());
        responseObject.put("itemName", thisItem.getItemName());
        responseObject.put("itemcategory", thisItem.getItemCategory());
        this.response = responseObject.toString();
    }


    /**
     * Edit user information, including username, password, and user deletion
     * UID should be the ID of the user to change
     * Field should be the field to change, or 'delete' if deleting a user
     * newvalue is the value to replace the old value of Field
     *
     * @author Joshua Famous
     */
    private void editItem(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();
        //Parse the parameters from the client
        String cid = requestParams.getString("cid");
        String mid = requestParams.getString("mid");
        String field = requestParams.getString("field");
        String newvalue = requestParams.getString("newvalue");

        ItemDataManager itemDataManager = new ItemDataManager(database);
        //Check that CID of User matches requesting CID
        if(!itemDataManager.getItemCID(mid).equals(cid)){
            responseObject.put("message","OutsideCompanyError");
            this.response = responseObject.toString();
        } else {
            //Perform requested operation
            switch(field){
                case "name":
                    if(newvalue.length() > 40 || newvalue.length() < 1){
                        responseObject.put("message","NameLengthError");
                        this.response = responseObject.toString();
                    } else{
                        itemDataManager.editName(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "category":
                    if(newvalue.length() > 40 || newvalue.length() < 1){
                        responseObject.put("message","CategoryLengthError");
                        this.response = responseObject.toString();
                    } else {
                        itemDataManager.editCategory(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "delete":
                    itemDataManager.removeItem(mid);
                    responseObject.put("message","Success");
                    this.response = responseObject.toString();
                    break;
                default:
                    responseObject.put("message","InvalidFieldError");
                    this.response = responseObject.toString();
                    break;
            }
        }
    }

    /**
     * Move an item from one place to another ~ raise a movement event
     * @author Joel Seidel
     * @param requestParams object parameters ~ request thing-a-ma-jig
     */
    private void moveItem(JSONObject requestParams){
        //Get params from request params objects
        String itemid = requestParams.getString("mid");
        String type = requestParams.getString("type");
        String cid = requestParams.getString("cid");
        //Create item movement event object from parameters
        DeviceMovementEvent deviceMovementEvent = new DeviceMovementEvent(itemid, cid, MovementEventManager.parseMovementType(type));
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

    /**
     * Delete an item from existence ~ not cycle out ~ wipe it off the face of the Earth
     * @param requestParams how many times do I need to define this its the same every time
     */
    private void deleteItem(JSONObject requestParams){
        //TODO: find this? maybe write it
    }
}

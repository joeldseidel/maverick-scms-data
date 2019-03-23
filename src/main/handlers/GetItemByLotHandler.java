package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.ItemDataManager;
import maverick_types.DatabaseType;
import maverick_types.MaverickItem;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Joel Seidel
 * Handles requests to get a single item by its maverick lot number
 */
public class GetItemByLotHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public GetItemByLotHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"mid", "token"};
        handlerName = "GetItemByLotHandler";
        initDb(DatabaseType.AppData);
    }

    /**
     * Parse params, get item data by id, format response, return to client
     * @param requestParams validated parameters sent from client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse params from the request parameters object
        String mlot = requestParams.getString("mid");
        //Instantiate item manager to get item from database with lot number
        ItemDataManager itemDataManager = new ItemDataManager(database);
        //Get maverick item from database by lot number
        MaverickItem thisItem = itemDataManager.getItem(mlot);
        //Format maverick object into a json object to stringify and send back as response
        this.response = formatResponse(thisItem).toString();
    }

    /**
     * Convert the maverick item object into a json object
     * @param item maverick item object to be put into the response json object
     * @return json object containing the data from the maverick item object
     */
    private JSONObject formatResponse(MaverickItem item){
        //Create json object to format data
        JSONObject responseObject = new JSONObject();
        //Get data from maverick item and put into response object
        responseObject.put("mid", item.getMaverickID());
        responseObject.put("fdaid", item.getFdaID());
        responseObject.put("itemName", item.getItemName());
        responseObject.put("itemcategory", item.getItemCategory());
        return responseObject;
    }
}

//FIXME: deprecated
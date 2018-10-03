package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.ItemDataManager;
import maverick_types.MaverickItem;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Joel Seidel
 * Handles requests to get a single item by its maverick lot number
 */
public class GetItemByLotHandler extends HandlerPrototype implements HttpHandler {
    private String response;
    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public GetItemByLotHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"mid", "token"};
    }
    /**
     * Entry point for handler. Get parameters, verify request validity, fulfill request, return response to client
     * @param httpExchange inherited from super class, set from client with params
     * @throws IOException thrown if there is an issue with writing response data to client
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            //Request was valid, fulfill the request with params
            fulfillRequest(requestParams);
        } else {
            //Request was invalid, set response to reflect this
            this.response = "invalid request";
        }
        //Create response to client
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to Get Item By Lot Request : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
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
        ItemDataManager itemDataManager = new ItemDataManager();
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

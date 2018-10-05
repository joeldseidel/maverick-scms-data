package handlers;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.ItemDataManager;
import maverick_types.MaverickItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
/**
 * Handles a search request by the client. Returns a JSON array of items which have properties containing the search term within the
 * specified company.
 *
 * @author Joel Seidel
 */
public class GetItemBySearchTermHandler extends HandlerPrototype implements HttpHandler {
    private String response;
    /**
     * Constructor to set the required keys with the super class
     */
    public GetItemBySearchTermHandler(){
        //Set the required keys within the super class
        requiredKeys = new String[] {"cid", "term", "token"};
    }
    /**
     * Entry point for handler. Get parameters, verify request validity, fulfill request, return response to client
     * @param httpExchange inherited from super class, set from client with params
     * @throws IOException thrown if there is an issue with writing response data to client
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if (isValidRequest) {
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
        System.out.println("Response to Get Item By Search Term Handler : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    /**
     * Parse the parameters, perform query by term, convert the results to json array, return the json array
     * @param requestParams validated params from the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse parameters from client
        String searchTerm = requestParams.getString("term");
        String cid = requestParams.getString("cid");
        //Item data manager instance for query and convert
        ItemDataManager itemDataManager = new ItemDataManager();
        //Get the search results from the database
        List<MaverickItem> searchResults = itemDataManager.searchItemByTerm(searchTerm, cid);
        //Convert the list of items into a json array to return to client
        JSONArray searchResultArray = itemDataManager.convertListToJsonArray(searchResults);
        //Put array to response container object
        JSONObject responseObject = new JSONObject().put("results", searchResultArray);
        //Set the return value, request fulfilled :)
        this.response = responseObject.toString();
    }
}

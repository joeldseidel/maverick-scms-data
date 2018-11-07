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

    /**
     * Constructor to set the required keys with the super class
     */
    public GetItemBySearchTermHandler(){
        //Set the required keys within the super class
        requiredKeys = new String[] {"cid", "term", "token"};
        handlerName = "GetItemBySearchTermHandler";
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

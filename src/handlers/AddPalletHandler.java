package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import maverick_types.MaverickItem;
import maverick_types.MaverickPallet;
import managers.PalletDataManager;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to create new pallets and potentially assign items to them if sent with pallet creation
 */

public class AddPalletHandler extends HandlerPrototype implements HttpHandler {
    private String response;

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AddPalletHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"cid", "items", "token"};
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
        System.out.println("Response to Add Pallet Request : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    /**
     * Fulfills valid request. Reads/parses params, performs request actions, and formats response
     * @param requestParams validated parameters sent by the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams) {
        //Parse the request parameters
        String cid = requestParams.getString("cid");
        //Create pallet items
        MaverickPallet thisPallet = new MaverickPallet(cid);
        PalletDataManager palletDataManager = new PalletDataManager();
        //PERFORM PALLET ADDING
        palletDataManager.addPallet(thisPallet);
        //Write response object
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }
}

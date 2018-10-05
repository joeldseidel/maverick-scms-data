package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import managers.PalletDataManager;
import managers.PalletMovementEventManager;
import maverick_types.MaverickPallet;
import maverick_types.MovementStatus;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class GetCurrentPalletStatus extends HandlerPrototype implements HttpHandler {
    private String response;
    public GetCurrentPalletStatus(){
        requiredKeys = new String[] {"mid", "cid", "token"};
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
        System.out.println("Response to Get Current Item Status : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
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
        PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();
        //Get the current movement status from manager query
        MovementStatus currentStatus = palletMovementEventManager.getCurrentStatus(thisPallet);
        //Convert the movement status to string and return as response
        this.response = MovementEventManager.movementStatusToString(currentStatus);
    }
}

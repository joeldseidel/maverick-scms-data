package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import maverick_types.PalletMovementEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class RaisePalletMovementEventHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = {"palletid", "type" , "fromcid", "tocid", "token"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            fulfillRequest(requestParams);
        } else {
            this.response = "invalid request";
        }
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to Add Item Request : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }
    @Override
    protected boolean isRequestValid(JSONObject requestParams){
        if(requestParams == null){
            //Request did not come with parameters, is invalid
            System.out.println("Request Params Null");
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                System.out.println("Request Params Missing Key " + requiredKey);
                return false;
            }
        }
        if(!verifyToken(requestParams.getString("token"))){
            //Could not verify provided token, request is invalid
            return false;
        }
        //Request contains all required keys
        return true;
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Get params from request params object
        String palletid = requestParams.getString("palletid");
        String type = requestParams.getString("type");
        String fromCid = requestParams.getString("fromcid");
        String toCid = requestParams.getString("tocid");
        //Create a pallet movement object from parameters
        PalletMovementEvent thisPalletMovementEvent = new PalletMovementEvent(palletid, MovementEventManager.parseMovementType(type), fromCid, toCid);
        //Validate and commit movement event
        if(thisPalletMovementEvent.isValid()){
            //Pallet movement event is valid and legal, commit to database
            thisPalletMovementEvent.commit();
            //Return successful message to client
            this.response = "success";
        } else {
            //Pallet movement was invalid
            this.response = "invalid response";
        }
    }
}

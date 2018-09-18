package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.MovementEventManager;
import maverick_types.DeviceMovementEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class RaiseItemMovementEventHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = {"mid", "type", "fromcid", "tocid", "token"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isRequestValid = isRequestValid(requestParams);
        displayRequestValidity(isRequestValid);
        if(isRequestValid){
            fulfillRequest(requestParams);
        } else {
            this.response = "invalid response";
        }
        int responseCode = isRequestValid ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to raise item movement event: " + this.response);
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
        /*if(!verifyToken(requestParams.getString("token"))){
            //Could not verify provided token, request is invalid
            return false;
        }*/
        //Request contains all required keys
        return true;
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Get params from request params objects
        String itemid = requestParams.getString("mid");
        String type = requestParams.getString("type");
        String fromcid = requestParams.getString("fromcid");
        String tocid = requestParams.getString("tocid");
        //Create item movement event object from parameters
        DeviceMovementEvent deviceMovementEvent = new DeviceMovementEvent(itemid, fromcid, tocid, MovementEventManager.parseMovementType(type));
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
}

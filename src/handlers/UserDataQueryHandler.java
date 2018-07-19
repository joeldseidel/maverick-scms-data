package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserDataManager;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class for handling a generic user data query without performing a specific, related action
 * This class fetches the data from the database using request params as args and returns the result
 *
 *
 * @author Joel Seidel
 */

public class UserDataQueryHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = {"request_type", "args"};
    private String response = "";
    public void handle(HttpExchange httpExchange) throws IOException{
        System.out.println("Enter user data handler");
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
        System.out.println("Response to user data query : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    @Override
    protected boolean isRequestValid(JSONObject requestParams){
        if(requestParams == null){
            //Request did not come with parameters, is invalid
            System.out.println("Request params invalid");
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                System.out.println("Request params missing key " + requiredKey);
                return false;
            }
        }
        //Request contains all required keys
        return true;
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String requestType = requestParams.getString("request_type");
        JSONArray requestArgsArray = requestParams.getJSONArray("args");
        switch(requestType){
            case "is_username_unique":
                String username = requestArgsArray.getJSONObject(0).getString("username");
                this.response = Boolean.toString(UserDataManager.isUniqueUsername(username));
                break;
            default:
                throw new NotImplementedException();
        }
    }
}

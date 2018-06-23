package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import maverickdata.*;
import org.json.JSONObject;

import java.io.*;

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = {"username", "password", "returnUserData"};
    public void handle(HttpExchange httpExchange) throws IOException {
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        String response = isValidRequest ? "received some data: " + requestParams.toString() : "invalid request";
        int responseCode = isValidRequest ? 200 : 400;
        httpExchange.sendResponseHeaders(responseCode, response.length());
        System.out.println(response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    protected boolean isRequestValid(JSONObject requestParams){
        if(requestParams == null){
            //Request did not come with parameters, is invalid
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                return false;
            }
        }
        //Request contains all required keys
        return true;
    }
}
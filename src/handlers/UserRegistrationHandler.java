package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserDataManager;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.impl.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class for handling a request to register a user
 *
 *
 * @author Joshua Famous
 */

public class UserRegistrationHandler extends HandlerPrototype implements HttpHandler {
    private String response = "";
    public void UserRegistrationHandler(){
        requiredKeys = new String[]{"cid", "username", "password", "token"};
    }
    public void handle(HttpExchange httpExchange) throws IOException{
        System.out.println("Enter user registration handler");
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
    protected void fulfillRequest(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();
        String cid = requestParams.getString("cid");
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        if(username.length() > 30 || username.length() < 1) {
            responseObject.put("message","UsernameLengthError");
            this.response = responseObject.toString();
        } else if(password.length() > 30 || password.length() < 1) {
            responseObject.put("message","PasswordLengthError");
            this.response = responseObject.toString();
        } else {
            //ADD USER THROUGH UserDataManager
            UserDataManager.addUser(cid, username, password);
            responseObject.put("message","Success");
            this.response = responseObject.toString();
        }
    }
}

package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserDataManager;
import maverick_types.DatabaseType;
import org.json.JSONObject;


import java.io.IOException;
import java.io.OutputStream;

/**
 * A class for handling a request to register a user
 *
 *
 * @author Joshua Famous
 */

public class UserRegistrationHandler extends HandlerPrototype implements HttpHandler {

    public void UserRegistrationHandler(){
        requiredKeys = new String[]{"cid", "username", "password", "token"};
        handlerName = "UserRegistrationHandler";
        initDb(DatabaseType.AppData);
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
            UserDataManager userDataManager = new UserDataManager(database);
            userDataManager.addUser(cid, username, password);
            responseObject.put("message","Success");
            this.response = responseObject.toString();
        }
    }
}

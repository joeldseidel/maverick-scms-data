package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserDataManager;
import maverick_data.DatabaseInteraction;
import maverick_data.Config;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"username", "password", "returnUserData"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Entered User Authentication Handler");
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        if(isValidRequest){
            System.out.println("Valid Request");
            fulfillRequest(requestParams);
        } else {
            System.out.println("Invalid Request");
            this.response = "invalid request";
        }
        int responseCode = isValidRequest ? 200 : 400;
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response : " + this.response);
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
        //Request contains all required keys
        return true;
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        boolean isUserValid = isUserValid(username, password);
        if(requestParams.getBoolean("returnUserData") && isUserValid){
            //Request wants the user data row in return
            JSONObject userDataObject = getUserData(username);
            //If user data fetched, return data, otherwise say no
            this.response = userDataObject == null ? "invalid user" : userDataObject.toString();
        } else {
            //Either the request only wants the authentication result or authentication failed, either way, it doesn't matter, return it
            this.response = Boolean.toString(isUserValid);
        }
    }

    private boolean isUserValid(String username, String password){
        boolean userIsValid;
        boolean userExists = (UserDataManager.getUserCount(username) == 1);
        if(userExists){
            //No need to get UUID yet, it means nothing to us
            //long userUUID = UserDataManager.getUserUUID(username);
            //TODO: query the password from the user_auth table and see if it matches
            userIsValid = true;
        } else {
            userIsValid = false;
        }
        System.out.println("User Validity : " + userIsValid);
        return userIsValid;
    }

    private JSONObject getUserData(String username){
        System.out.println("Attempting to get user data for username : " + username);
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
        String getUserDataSql = "SELECT * FROM table_users WHERE username = ?";
        PreparedStatement getUserDataStatement = database.prepareStatement(getUserDataSql);
        JSONObject userDataObject;
        try{
            getUserDataStatement.setString(1, username);
            ResultSet getUserDataResults = database.query(getUserDataStatement);
            userDataObject = getUserDataFormattedResponse(getUserDataResults);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            userDataObject = null;
        }
        System.out.println("Got User Data Object : " + userDataObject);
        return userDataObject;
    }

    private JSONObject getUserDataFormattedResponse(ResultSet userDataResults) throws SQLException{
        ResultSetMetaData rsMeta = userDataResults.getMetaData();
        int rsColumns = rsMeta.getColumnCount();
        userDataResults.next();
        List<String> columnList = new ArrayList<>();
        for(int col = 1; col <= rsColumns; col++){
            Object value = userDataResults.getObject(col);
            columnList.add(value.toString());
        }
        return null;
    }

}
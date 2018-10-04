package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import managers.UserDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.*;

/**
 * @author Joel Seidel
 *
 * Determine the authenticity of a username and password pair and return user data
 */

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {
    private String response;
    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AuthenticateUserHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"username", "password", "returnUserData"};
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
     * Override the super class request validity method because the token has not be assigned yet and cannot be verified
     * @param requestParams parameters from the client to be validated
     * @return boolean with results of validity test
     */
    @Override
    protected boolean isRequestValid(JSONObject requestParams) {
        if (requestParams == null) {
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
        return true;
    }
    /**
     * Parse request parameters, query database to determine authenticity, format and structure user data, return requested data
     * @param requestParams validated parameters sent by the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        boolean isUserValid = isUserValid(username, password);
        if(requestParams.getBoolean("returnUserData") && isUserValid){
            //Request wants the user data row in return
            JSONObject userDataObject = getUserData(username);
            userDataObject.put("token",createToken());
            //If user data fetched, return data, otherwise say no
            this.response = userDataObject == null ? "invalid user" : userDataObject.toString();
        } else {
            //Either the request only wants the authentication result or authentication failed, either way, it doesn't matter, return it
            this.response = Boolean.toString(isUserValid);
        }
    }

    /**
     * Get user count from user database and determine authenticity
     * @param username username parameter from client
     * @param password password parameter from client
     * @return boolean representing the authenticity of the user
     */
    private boolean isUserValid(String username, String password){
        boolean userIsValid;
        boolean userExists = (UserDataManager.getUserCount(username) == 1);
        if(userExists){
            //Get UUID for password checking
            long userUUID = UserDataManager.getUserUUID(username);
            //Query the password from the users table and see if it matches
            userIsValid = UserDataManager.checkPasswordMatch(userUUID, password);
        } else {
            userIsValid = false;
        }
        System.out.println("User Validity : " + userIsValid);
        return userIsValid;
    }

    /**
     * Get user data from database by username as key
     * @param username key of username to query by
     * @return JSONObject containing structured user data from database query
     */
    //Todo: Migrate this method functionality to use manager class
    private JSONObject getUserData(String username){
        System.out.println("Attempting to get user data for username : " + username);
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String getUserDataSql = "SELECT * FROM table_users WHERE username = ?";
        PreparedStatement getUserDataStatement = database.prepareStatement(getUserDataSql);
        JSONObject userDataObject = new JSONObject();
        try{
            getUserDataStatement.setString(1, username);
            ResultSet getUserDataResults = database.query(getUserDataStatement);
            try{
            userDataObject.put("arrayResult",getUserDataFormattedResponse(getUserDataResults));
            }
            catch(Exception e){
                System.out.println("Failed to get Formatted Response");
                userDataObject = null;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            userDataObject = null;
        }
        System.out.println("Got User Data Object : " + userDataObject);
        return userDataObject;
    }

    /**
     * Convert a result set into a JSON Array
     * @param userDataResults result set from the database query for the user data
     * @return a JSONArray containing the structured user data
     * @throws Exception may be thrown in the structuring of the data and parsing from result set
     */
    private JSONArray getUserDataFormattedResponse(ResultSet userDataResults) throws Exception {
        JSONArray jsonArray = new JSONArray();
        //Loop through each of the user data results contained in the result set and parse to json object to be inserted into the array
        while (userDataResults.next()) {
            //Get count of columns within result set
            int total_rows = userDataResults.getMetaData().getColumnCount();
            //Loop through each of the columns to parse
            for (int i = 0; i < total_rows; i++) {
                JSONObject obj = new JSONObject();
                System.out.println("Got " + userDataResults.getObject(i + 1) + " for " + userDataResults.getMetaData().getColumnLabel(i + 1).toLowerCase());
                //Create json object from parsed data
                obj.put(userDataResults.getMetaData().getColumnLabel(i + 1).toLowerCase(), userDataResults.getObject(i + 1));
                //Put created json object into
                jsonArray.put(obj);
            }
        }
        return jsonArray;
    }

    /**
     * Create a JSON Web Token to pass to client on authenticated login
     * @return string containing the generated token for the authenticated user session
     */
    private static String createToken(){
        String token = "";
        try {
            //Generate the user token
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create().withIssuer("localhost:6969").sign(algorithm);
        } catch (Exception exception){
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        System.out.println("Created token : " + token);
        return token;
    }

}
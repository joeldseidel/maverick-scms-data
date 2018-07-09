package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import managers.UserDataManager;
import maverick_data.DatabaseInteraction;
import maverick_data.Config;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Calendar;
import java.security.Key;

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"username", "password", "returnUserData"};
    private String response, token;
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
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.write(this.token.getBytes());
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
            if(userDataObject != null){
                this.token = createToken(UserDataManager.getUserUUID(username));
            }
        } else {
            //Either the request only wants the authentication result or authentication failed, either way, it doesn't matter, return it
            this.response = Boolean.toString(isUserValid);
        }
    }

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

    private JSONObject getUserData(String username){
        System.out.println("Attempting to get user data for username : " + username);
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
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
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONArray getUserDataFormattedResponse(ResultSet userDataResults) throws Exception {
        System.out.println("Getting NEW Formatted Response");
        JSONArray jsonArray = new JSONArray();
        while (userDataResults.next()) {
            int total_rows = userDataResults.getMetaData().getColumnCount();
            for (int i = 0; i < total_rows; i++) {
                JSONObject obj = new JSONObject();
                System.out.println("Got " + userDataResults.getObject(i + 1) + " for " + userDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase());
                obj.put(userDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), userDataResults.getObject(i + 1));
                jsonArray.put(obj);
            }
        }
        return jsonArray;
    }

    //Create a JSON Web Token to pass to client on authenticated login
    private static String createToken(long uid){
        byte[] key = getSignatureKey();

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
 
        String jwt = 
            Jwts.builder().setIssuer("http://localhost:6969/")
                .setSubject("users/" + uid)
                .setExpiration(cal.getTime())
                .put("scope", "self api/buy") 
                .signWith(SignatureAlgorithm.HS256,key)
                .compact();

        return jwt;
    }

}
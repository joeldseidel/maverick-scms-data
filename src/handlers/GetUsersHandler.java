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

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.impl.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

public class GetUsersHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"cid", "token"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Entered User Authentication Handler");
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
        System.out.println("Response to User Logon Request : " + this.response);
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

        String cid = requestParams.getString("cid");
        String token = requestParams.getString("token");

        //VERIFY TOKEN
        try {

            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("localhost:6969")
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            isVerified = true;
            System.out.println("Token " + token + " was verified");

        } catch (Exception exception){
            //Invalid signature/claims
            isVerified = false;
            System.out.println("Token " + token + " was not verified");
        }

        if(isVerified){

            //Request wants the user data row in return
            JSONObject userDataObject = getUserDataByCompany(cid);
            //If user data fetched, return data, otherwise say no
            this.response = userDataObject.toString();

        }
        else{

            this.response = Boolean.toString(false);

        }
    }

    private JSONObject getUserDataByCompany(String cid){
        System.out.println("Attempting to get user data for company : " + cid);
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String getUserDataSql = "SELECT * FROM table_users WHERE cid = ?";
        PreparedStatement getUserDataStatement = database.prepareStatement(getUserDataSql);
        JSONObject userDataObject = new JSONObject();
        try{
            getUserDataStatement.setString(1, cid);
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

}
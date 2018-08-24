package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import maverick_data.DatabaseInteraction;
import maverick_data.Config;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to return a listing of all items in their company to the client
 */

public class GetItemsHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"cid", "token"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Entered Get Items Handler");
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
        System.out.println("Response to Get Item Request : " + this.response);
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
        boolean isVerified = false;

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

            //Format item data into an object to return
            JSONObject itemDataObject = getItemDataByCompany(cid);
            //If item data fetched, return data, otherwise say no
            this.response = itemDataObject.toString();

        }
        else{

            this.response = Boolean.toString(false);

        }
    }

    private JSONObject getItemDataByCompany(String cid){
        System.out.println("Attempting to get item data for company : " + cid);
        DatabaseInteraction database = new DatabaseInteraction(Config.port, Config.user, Config.pass, Config.databaseName);
        String getItemDataSql = "SELECT table_items.mid, table_items.name, table_items.category, table_itempalletmapping.pallet FROM table_items LEFT JOIN table_itempalletmapping ON table_items.mid = table_itempalletmapping.mid AND table_items.cid = ?";
        PreparedStatement getItemDataStatement = database.prepareStatement(getItemDataSql);
        JSONObject itemDataObject = new JSONObject();
        try{
            getItemDataStatement.setString(1, cid);
            ResultSet getItemDataResults = database.query(getItemDataStatement);
            try{
                itemDataObject.put("arrayResult",getItemDataFormattedResponse(getItemDataResults));
            }
            catch(Exception e){
                System.out.println("Failed to get Formatted Response for " + e);
                itemDataObject = null;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            itemDataObject = null;
        }
        finally{
            database.closeConnection();
        }
        System.out.println("Got Item Data Object : " + itemDataObject);
        return itemDataObject;
    }

    /**
     * Convert a result set into a JSON Array
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONArray getItemDataFormattedResponse(ResultSet itemDataResults) throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (itemDataResults.next()) {
            JSONObject obj = new JSONObject();
            int total_rows = itemDataResults.getMetaData().getColumnCount();
            for (int i = 0; i < total_rows; i++) {
                System.out.println("Got " + itemDataResults.getObject(i + 1) + " for " + itemDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase());
                obj.put(itemDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), itemDataResults.getObject(i + 1));
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }

}
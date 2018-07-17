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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import managers.PurchaseOrderDataManager;

public class AddPurchaseOrderHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"fdaid", "name", "category", "cid", "token"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
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
        System.out.println("Response to Add Item Request : " + this.response);
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

        boolean isVerified;

        int fdaid = requestParams.getInt("fdaid");
        String cid = requestParams.getString("cid");
        String name = requestParams.getString("name");
        String category = requestParams.getString("category");
        String token = requestParams.getString("token");

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

            MaverickItem thisItem = new MaverickItem(fdaid, name, category, cid);
            ItemDataManager itemDataManager = new ItemDataManager();
            itemDataManager.addItem(thisItem);
            JSONObject responseObject = new JSONObject();
            responseObject.put("message","Success");
            this.response = responseObject.toString();

        }
        else{

            this.response = Boolean.toString(false);

        }

    }

}
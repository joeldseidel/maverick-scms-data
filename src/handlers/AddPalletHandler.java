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

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.impl.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

import maverick_types.MaverickItem;
import maverick_types.MaverickPallet;
import managers.PalletDataManager;

public class AddPalletHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"cid", "items", "token"};
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
        System.out.println("Response to Add Purchase Order Request : " + this.response);
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

        String cid = requestParams.getString("cid");
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

            //CREATE PALLET
            MaverickPallet thisPallet = new MaverickPallet(cid);
            PalletDataManager palletDataManager = new PalletDataManager();

            //ADD PALLET ITEMS
            JSONArray items = requestParams.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
              JSONObject item = items.getJSONObject(i);
              thisPallet.addItem(new MaverickItem(
                item.getString("mid")
                ));
            }

            //PERFORM PALLET ADDING
            palletDataManager.addPallet(thisPallet);
            JSONObject responseObject = new JSONObject();
            responseObject.put("message","Success");
            this.response = responseObject.toString();

        }
        else{

            this.response = Boolean.toString(false);

        }

    }

}
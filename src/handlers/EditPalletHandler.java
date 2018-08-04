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
import managers.ItemDataManager;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to add items to pallets or remove items from pallets
 */

public class EditPalletHandler extends HandlerPrototype implements HttpHandler {

    private String[] requiredKeys = {"cid", "mid", "pallet", "token"};
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
        System.out.println("Response to Edit Pallet Request : " + this.response);
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
        JSONObject responseObject = new JSONObject();

        String cid = requestParams.getString("cid");
        String token = requestParams.getString("token");
        String mid = requestParams.getString("mid");
        int pallet = requestParams.getInt("pallet");

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

            //ENSURE PALLET IS IN COMPANY
            if(!PalletDataManager.getPalletCID(pallet).equals(cid)){
                responseObject.put("message","PalletOutsideCompanyError");
                this.response = responseObject.toString();
            }
            else{

                //CHECK VALIDITY OF MAVERICK ITEM
                if(ItemDataManager.itemExists(mid)){

                    //ENSURE ITEM IS IN COMPANY
                    if(!PalletDataManager.getPalletCID(pallet).equals(cid)){

                        responseObject.put("message","ItemOutsideCompanyError");
                        this.response = responseObject.toString();

                    }
                    else{

                        //CHECK VALIDITY OF MAVERICK ITEM
                        if(ItemDataManager.itemExists(mid)){

                            if(pallet == 0){
                                ItemDataManager.removeFromPallet(mid);
                            }
                            else{
                                ItemDataManager.updatePallet(mid, pallet);
                            }

                            responseObject.put("message","Success");
                            this.response = responseObject.toString();

                        }
                        else{
                            responseObject.put("message","InvalidPalletError");
                            this.response = responseObject.toString();
                        }
                        
                    }

                }
                else{
                    responseObject.put("message","InvalidItemError");
                    this.response = responseObject.toString();
                }

            }

        }
        else{

            this.response = Boolean.toString(false);

        }

    }

}
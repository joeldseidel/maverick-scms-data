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
    private String response;
    public EditPalletHandler(){
        requiredKeys = new String[] {"cid", "mid", "pallet", "token"};
    }
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
    protected void fulfillRequest(JSONObject requestParams){

        boolean isVerified;
        JSONObject responseObject = new JSONObject();

        String cid = requestParams.getString("cid");
        String mid = requestParams.getString("mid");
        String pallet = requestParams.getString("pallet");
            //ENSURE PALLET IS IN COMPANY
            if(!PalletDataManager.getPalletCID(pallet).equals(cid)){
                responseObject.put("message","PalletOutsideCompanyError");
                this.response = responseObject.toString();
            }
            else{

                //CHECK VALIDITY OF MAVERICK ITEM
                if(ItemDataManager.itemExists(mid)){

                    //ENSURE ITEM IS IN COMPANY
                    if(!ItemDataManager.getItemCID(mid).equals(cid)){

                        responseObject.put("message","ItemOutsideCompanyError");
                        this.response = responseObject.toString();

                    }
                    else{

                        //CHECK VALIDITY OF PALLET
                        if(PalletDataManager.palletExists(pallet)){
                            ItemDataManager.updatePallet(mid, pallet);
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

}
package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

import managers.PalletDataManager;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to add items to pallets or remove items from pallets
 */

public class RemovePalletHandler extends HandlerPrototype implements HttpHandler {

    public RemovePalletHandler(){
        //Define the required keys in the super class
        requiredKeys = new String[] {"cid", "pallet", "token"};
        handlerName = "RemovePalletHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();
        String cid = requestParams.getString("cid");
        String pallet = requestParams.getString("pallet");
        PalletDataManager palletDataManager = new PalletDataManager();
        //ENSURE PALLET IS IN COMPANY
        if(!palletDataManager.getPalletCID(pallet).equals(cid)){
            responseObject.put("message","PalletOutsideCompanyError");
            this.response = responseObject.toString();
        } else {
            //CHECK VALIDITY OF PALLET
            if(palletDataManager.palletExists(pallet)){
                palletDataManager.removePallet(pallet);
                responseObject.put("message","Success");
                this.response = responseObject.toString();
            } else {
                responseObject.put("message","InvalidPalletError");
                this.response = responseObject.toString();
            }
        }
    }
}
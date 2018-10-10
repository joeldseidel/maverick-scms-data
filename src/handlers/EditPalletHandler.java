package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import managers.PalletDataManager;
import managers.ItemDataManager;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to add items to pallets or remove items from pallets
 */

public class EditPalletHandler extends HandlerPrototype implements HttpHandler {

    public EditPalletHandler(){
        requiredKeys = new String[] {"cid", "mid", "pallet", "token"};
        handlerName = "EditPalletHandler";
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
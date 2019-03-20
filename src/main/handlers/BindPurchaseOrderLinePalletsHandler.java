package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import managers.PalletDataManager;
import managers.PurchaseOrderDataManager;
import maverick_types.DatabaseType;
import maverick_types.MaverickPallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BindPurchaseOrderLinePalletsHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to define the required keys for this handler
     */
    public BindPurchaseOrderLinePalletsHandler(){
        //Define the required keys in super class
        requiredKeys = new String[] {"poid", "poline", "pallets", "token"};
        handlerName = "BindPurchaseOrderLinePalletsHandler";
        initDb(DatabaseType.AppData);
    }

    /**
     * Parse the parameters, parse the pallet list, write each pallet binding to the database
     * @param requestParams validated parameters from the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse the parameters from the request
        int poId = requestParams.getInt("poid");
        int poline = requestParams.getInt("poline");
        JSONArray palletsArray = requestParams.getJSONArray("pallets");
        //Parse the json array to get maverick pallet items from data
        List<MaverickPallet> palletsToBind = PalletDataManager.parseFromJsonArray(palletsArray);
        //Instantiate the data manager to interface with the pallet binding data
        PurchaseOrderDataManager purchaseOrderDataManager = new PurchaseOrderDataManager(database);
        for(MaverickPallet thisPallet : palletsToBind){
            //For each pallet, write the pallet binding to the database
            purchaseOrderDataManager.bindPallet(poId, poline, thisPallet);
        }
    }
}

package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import maverick_types.MaverickItem;
import maverick_types.MaverickPallet;
import managers.PalletDataManager;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to create new pallets and potentially assign items to them if sent with pallet creation
 */

public class AddPalletHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AddPalletHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"cid", "token"};
        handlerName = "addPalletHandler";
    }

    /**
     * Fulfills valid request. Reads/parses params, performs request actions, and formats response
     * @param requestParams validated parameters sent by the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams) {
        //Parse the request parameters
        String cid = requestParams.getString("cid");
        System.out.println("Entered add pallet handler with cid " + requestParams.getString("cid"));
        //Create pallet items
        MaverickPallet thisPallet = new MaverickPallet(cid);
        PalletDataManager palletDataManager = new PalletDataManager();
        System.out.println("Created pallet and manager successfully");
        //PERFORM PALLET ADDING
        palletDataManager.addPallet(thisPallet);
        //Write response object
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }
}

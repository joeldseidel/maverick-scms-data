package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;

import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import managers.PurchaseOrderDataManager;

/**
 * @author Joshua Famous
 *
 * Create a new purchase order object, create line objects as per the lines provided in arguments and write to database
 */

public class AddPurchaseOrderHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AddPurchaseOrderHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"number", "dateplaced", "placingcompany", "cid", "lines", "token"};
        handlerName = "AddPurchaseOrderHandler";
    }

    /**
     * Get parameters, create the purchase order object, parse and add line objects, commit all to the database
     * @param requestParams validated parameters sent by the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //Parse the request parameters
        String cid = requestParams.getString("cid");
        String number = requestParams.getString("number");
        String dateplaced = requestParams.getString("dateplaced");
        String placingcompany = requestParams.getString("placingcompany");
        //CREATE PURCHASE ORDER
        MaverickPurchaseOrder thisOrder = new MaverickPurchaseOrder(number, dateplaced, placingcompany, cid);
        PurchaseOrderDataManager poDataManager = new PurchaseOrderDataManager();
        //ADD PURCHASE ORDER LINES
        JSONArray lines = requestParams.getJSONArray("lines");
        for (int i = 0; i < lines.length(); i++) {
            //Get this purchase order line from the parameter array
            JSONObject line = lines.getJSONObject(i);
            //Create the line within the purchase order line from the provided parameters
            thisOrder.addLine(new MaverickPurchaseOrderLine(
                    line.getInt("line"),
                    line.getString("supplierpartnum"),
                    line.getString("partdesc"),
                    line.getString("deliverydate"),
                    line.getFloat("quantity"),
                    line.getFloat("price")
            ));
        }
        //PERFORM PURCHASE ORDER ADDING
        poDataManager.addPurchaseOrder(thisOrder);
        JSONObject responseObject = new JSONObject();
        //Create response object and stringify for return to client
        responseObject.put("message","Success");
        this.response = responseObject.toString();
    }
}
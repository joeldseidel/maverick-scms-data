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
    private String response;
    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AddPurchaseOrderHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"number", "dateplaced", "placingcompany", "cid", "lines", "token"};
    }
    /**
     * Entry point for handler. Get parameters, verify request validity, fulfill request, return response to client
     * @param httpExchange inherited from super class, set from client with params
     * @throws IOException thrown if there is an issue with writing response data to client
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            //Request was valid, fulfill the request with params
            fulfillRequest(requestParams);
        } else {
            //Request was invalid, set response to reflect this
            this.response = "invalid request";
        }
        //Create response to client
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to Add Pallet Request : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
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
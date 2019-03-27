package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.PurchaseOrderDataManager;
import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import org.json.JSONArray;
import org.json.JSONObject;

public class PurchaseOrderRequestHandler extends HandlerPrototype implements HttpHandler {
    public PurchaseOrderRequestHandler(){
        handlerName = "Purchase Order Request";
        requiredKeys = new String[] { "cmd" };
    }

    public PurchaseOrderRequestHandler(String cmd){
        handlerName = "Purchase Order Request: " + cmd;
        super.handleCmd = cmd;
        switch(cmd){
            case "new":
                requiredKeys = new String[] { "number", "dateplaced", "placingcompany", "cid", "lines", "token" };
                break;
            case "newline":
                requiredKeys = new String[] { "po_num", "supplierpartnum", "partdesc", "deliverydate", "quantity", "unitprice", "token" };
                break;
            case "edit":
                requiredKeys = new String[] { /*TODO*/ };
                break;
            case "delete":
                requiredKeys = new String[] { /*TODO*/ };
                break;
            case "update":
                requiredKeys = new String[] { /*TODO*/};
                break;
            case "get":
                requiredKeys = new String[] { "number", "cid", "token"};
                break;
        }
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        switch (handleCmd){
            case "new":
                createPO(requestParams);
                break;
            case "newline":
                createNewLine(requestParams);
                break;
            case "edit":
                editPO(requestParams);
                break;
            case "delete":
                deletePO(requestParams);
                break;
            case "update":
                updatePO(requestParams);
                break;
            case "get":
                getPO(requestParams);
        }
    }

    /**
     * Create a new purchase order
     * @param requestParams parameters needed to create a purchase order
     */
    private void createPO(JSONObject requestParams){
        //Parse the request parameters
        String cid = requestParams.getString("cid");
        String number = requestParams.getString("number");
        String dateplaced = requestParams.getString("dateplaced");
        String placingcompany = requestParams.getString("placingcompany");
        //CREATE PURCHASE ORDER
        MaverickPurchaseOrder thisOrder = new MaverickPurchaseOrder(number, dateplaced, placingcompany, cid);
        PurchaseOrderDataManager poDataManager = new PurchaseOrderDataManager(database);
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

    /**
     * Create a new purchase order line
     * @param requestParams the parameters provided with the request
     */
    private void createNewLine(JSONObject requestParams){
        //TODO: this
    }

    /**
     * Edit an existing purchase order
     * @param requestParams the parameters provided with the request
     */
    private void editPO(JSONObject requestParams){
        //TODO: this
    }

    /**
     * Delete an existing purchase order ( and all its lines )
     * @param requestParams the parameters provided with the request
     */
    private void deletePO(JSONObject requestParams){
        //TODO: this
    }

    /**
     * Update an existing purchase order properties
     * @param requestParams the parameters provided with the request
     */
    private void updatePO(JSONObject requestParams){
        //TODO: this
    }

    /**
     * Get an existing purchase order ( and all its lines )
     * @param requestParams the parameters provided with the request
     */
    private void getPO(JSONObject requestParams){
        //TODO: this
    }
}
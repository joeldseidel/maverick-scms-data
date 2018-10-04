package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import managers.PalletDataManager;
import managers.PurchaseOrderDataManager;
import maverick_types.MaverickPallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BindPurchaseOrderLinePalletsHandler extends HandlerPrototype implements HttpHandler {
    private String response;
    /**
     * Constructor to define the required keys for this handler
     */
    public BindPurchaseOrderLinePalletsHandler(){
        //Define the required keys in super class
        requiredKeys = new String[] {"poid", "poline", "pallets", "token"};
    }
    /**
     * Entry point for handler. Get parameters, verify request validity, fulfill request, return response to client
     * @param httpExchange inherited from super class, set from client with params
     * @throws IOException thrown if there is an issue with writing response data to client
     */
    public void handle(com.sun.net.httpserver.HttpExchange httpExchange) throws IOException {
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if (isValidRequest) {
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
        System.out.println("Response to Add Item Request : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        int poId = requestParams.getInt("poid");
        int poline = requestParams.getInt("poline");
        JSONArray palletsArray = requestParams.getJSONArray("pallets");
        List<MaverickPallet> palletsToBind = PalletDataManager.parseFromJsonArray(palletsArray);
        PurchaseOrderDataManager purchaseOrderDataManager = new PurchaseOrderDataManager();
        for(MaverickPallet thisPallet : palletsToBind){
            purchaseOrderDataManager.bindPallet(poId, poline, thisPallet);
        }
    }
}

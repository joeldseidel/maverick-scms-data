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

    private String[] requiredKeys = {"cid", "pallet", "token"};
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
        JSONObject responseObject = new JSONObject();
        String cid = requestParams.getString("cid");
        String pallet = requestParams.getString("pallet");
        //ENSURE PALLET IS IN COMPANY
        if(!PalletDataManager.getPalletCID(pallet).equals(cid)){
            responseObject.put("message","PalletOutsideCompanyError");
            this.response = responseObject.toString();
        } else {
            //CHECK VALIDITY OF PALLET
            if(PalletDataManager.palletExists(pallet)){
                PalletDataManager.removePallet(pallet);
                responseObject.put("message","Success");
                this.response = responseObject.toString();
            } else {
                responseObject.put("message","InvalidPalletError");
                this.response = responseObject.toString();
            }
        }
    }
}
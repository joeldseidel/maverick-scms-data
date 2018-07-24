package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.LotNumberManager;
import maverick_types.LotType;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class GenerateLotNumberHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = { "lot_type" };
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Entered generate lot number handler");
        JSONObject requestparams = GetParameterObject(httpExchange);
        boolean isRequestValid = isRequestValid(requestparams);
        displayRequestValidity(isRequestValid);
        if(isRequestValid){
            fulfillRequest(requestparams);
        } else {
            this.response = "invalid lot type specified";
        }
        int responseCode = isRequestValid ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to generate lot number : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    @Override
    protected boolean isRequestValid(JSONObject requestParams){
        if(requestParams == null){
            //Request did not come with parameters, is invalid
            System.out.println("Request params null");
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                System.out.println("Request params missing key " + requiredKey);
                return false;
            }
        }
        //TODO: verify pallet or item specified only, no other options
        //Request contains all required keys
        return true;
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String lotTypeString = requestParams.getString("lot_type");
        LotType lotType = LotType.Item;
        switch(lotTypeString){
            case "item":
                lotType = LotType.Item;
                break;
            case "pallet":
                lotType = LotType.Pallet;
                break;
        }
        this.response = Long.toString(new LotNumberManager().generateLotNumber(lotType));
    }
}

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
    private String response;
    public GenerateLotNumberHandler(){
        requiredKeys = new String[] {"lot_type"};
    }
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
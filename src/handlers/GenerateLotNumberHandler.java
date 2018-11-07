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

    public GenerateLotNumberHandler(){
        requiredKeys = new String[] {"lot_type"};
        handlerName = "GenerateLotNumberHandler";
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
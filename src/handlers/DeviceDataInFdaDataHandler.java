package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import maverick_data.Config;
import maverick_data.DatabaseInteraction;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceDataInFdaDataHandler extends HandlerPrototype implements HttpHandler {
    private String[] requiredKeys = {"fdaId"};
    private String response;
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Entered create device manifest handler");
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            fulfillRequest(requestParams);
        } else {
            this.response = "invalid response";
        }
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to create new manifest request : " + this.response);
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
        //
        String fdaId = requestParams.getString("fdaId");
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        try {
            boolean deviceDataExists = isDeviceDataInFdaData(fdaId, database);
            //Todo: fetch the device data if it exists
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    private boolean isDeviceDataInFdaData(String fdaId, DatabaseInteraction database) throws SQLException{
        int matchingDeviceCount = 0;
        String isDeviceInFdaDataSql = "SELECT COUNT(1) FROM fda_data_devices WHERE fdaId = ?";
        PreparedStatement isDeviceInFdaDataQuery = database.prepareStatement(isDeviceInFdaDataSql);
        isDeviceInFdaDataQuery.setString(1, fdaId);
        ResultSet isDeviceInFdaDataResult = database.query(isDeviceInFdaDataQuery);
        if(isDeviceInFdaDataResult.next()){
            matchingDeviceCount = isDeviceInFdaDataResult.getInt(0);
        }
        return matchingDeviceCount == 1;
    }
}

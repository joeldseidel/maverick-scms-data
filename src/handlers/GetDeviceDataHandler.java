package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.DeviceDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.FDADevice;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetDeviceDataHandler extends HandlerPrototype implements HttpHandler {


    public GetDeviceDataHandler(){
        requiredKeys = new String[] {"devices"};
        handlerName = "GetDeviceDataHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        List<String> deviceIds = getParameterDeviceIds(requestParams.getJSONArray("devices"));
        List<FDADevice> devices = getDeviceDataObjects(deviceIds);
        JSONArray deviceDataArray = getDeviceJsonArray(devices);
        this.response = new JSONObject().put("device_data", deviceDataArray).toString();
    }

    private JSONArray getDeviceJsonArray(List<FDADevice> devices){
        JSONArray deviceArray = new JSONArray();
        for(FDADevice device : devices){
            JSONObject deviceObj = device.serializeToJson(device);
            deviceArray.put(deviceObj);
        }
        return deviceArray;
    }

    private List<FDADevice> getDeviceDataObjects(List<String> deviceIds){
        DeviceDataManager deviceDataManager = new DeviceDataManager();
        List<FDADevice> devices = new ArrayList<>();
        for(String fdaId : deviceIds){
            try {
                FDADevice thisDevice = deviceDataManager.getDeviceByFdaId(fdaId);
                devices.add(thisDevice);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
        return devices;
    }

    private List<String> getParameterDeviceIds(JSONArray devicesArray){
        List<String> fdaIds = new ArrayList<>();
        for(int i = 0; i < devicesArray.length(); i++){
            JSONObject thisFdaIdObj = devicesArray.getJSONObject(i);
            fdaIds.add(thisFdaIdObj.getString("fdaid"));
        }
        return fdaIds;
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

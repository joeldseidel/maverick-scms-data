package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.DeviceDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.FDADevice;
import org.json.JSONObject;
import org.json.JSONArray;

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
        JSONArray deviceJsonArray = requestParams.getJSONArray("devices");
        List<String> deviceIds = getParameterDeviceIds(deviceJsonArray);
        List<FDADevice> devices = getDeviceDataObjects(deviceIds);
        JSONArray deviceDataArray = getDeviceJsonArray(devices);
        this.response = new JSONObject().put("device_data", deviceDataArray).toString();
    }

    private JSONArray getDeviceJsonArray(List<FDADevice> devices){
        JSONArray deviceArray = new JSONArray();
        DeviceDataManager deviceDataManager = new DeviceDataManager(database);
        for(FDADevice device : devices){
            JSONObject deviceObj = deviceDataManager.serializeToJson(device);
            deviceArray.put(deviceObj);
        }
        return deviceArray;
    }

    private List<FDADevice> getDeviceDataObjects(List<String> deviceIds){
        DatabaseInteraction fdaData = new DatabaseInteraction(DatabaseType.Devices);
        DeviceDataManager deviceDataManager = new DeviceDataManager(fdaData);
        List<FDADevice> devices = new ArrayList<>();
        for(String fdaId : deviceIds){
            FDADevice thisDevice = deviceDataManager.getDeviceByFdaId(fdaId);
            devices.add(thisDevice);
        }
        fdaData.closeConnection();
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

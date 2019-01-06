package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.DeviceDataManager;
import managers.ItemDataManager;
import maverick_types.FDADeviceTypes.FDADevice;
import org.json.JSONObject;

import java.util.List;

public class ImportCompanyDevicesHandler extends HandlerPrototype implements HttpHandler {
    public ImportCompanyDevicesHandler(){
        requiredKeys = new String[] {"company_name", "cid", "token"};
        handlerName = "ImportCompanyDevicesHandler";
    }
    protected void fulfillRequest(JSONObject requestParams){
        String companyName = requestParams.getString("company_name");
        String cid = requestParams.getString("cid");
        DeviceDataManager deviceDataManager = new DeviceDataManager();
        List<FDADevice> deviceList = deviceDataManager.getCompanyDevicesForImport(companyName);
        ItemDataManager itemDataManager = new ItemDataManager();
        itemDataManager.importFDADevices(deviceList, cid);
    }
}

package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.DeviceDataManager;
import managers.ItemDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.FDADevice;
import org.json.JSONObject;

import java.util.List;

public class ImportCompanyDevicesHandler extends HandlerPrototype implements HttpHandler {
    public ImportCompanyDevicesHandler(){
        requiredKeys = new String[] {"company_name", "cid", "token"};
        handlerName = "ImportCompanyDevicesHandler";
        initDb(DatabaseType.AppData);
    }
    protected void fulfillRequest(JSONObject requestParams){
        String companyName = requestParams.getString("company_name");
        String cid = requestParams.getString("cid");
        DeviceDataManager deviceDataManager = new DeviceDataManager(new DatabaseInteraction(DatabaseType.Devices));
        List<FDADevice> deviceList = deviceDataManager.getCompanyDevicesForImport(companyName);
        ItemDataManager itemDataManager = new ItemDataManager(database);
        itemDataManager.importFDADevices(deviceList, cid);
    }
}

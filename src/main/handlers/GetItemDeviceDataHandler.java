package handlers;

import com.sun.net.httpserver.HttpHandler;
import managers.DeviceDataManager;
import managers.ItemDataManager;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.FDADevice;
import maverick_types.MaverickItem;
import org.json.JSONObject;

public class GetItemDeviceDataHandler extends HandlerPrototype implements HttpHandler {
    public GetItemDeviceDataHandler(){
        requiredKeys = new String[] {"mid", "token"};
        handlerName = "GetItemDeviceDataHandler";
        initDb(DatabaseType.AppData);
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String mid = requestParams.getString("mid");
        ItemDataManager itemDataManager = new ItemDataManager(database);
        MaverickItem item = itemDataManager.getItem(mid);
        String fdaId = item.getFdaID();
        DeviceDataManager deviceDataManager = new DeviceDataManager(database);
        FDADevice thisDevice = deviceDataManager.getDeviceByFdaId(fdaId);
        this.response = deviceDataManager.serializeToJson(thisDevice).toString();
    }
}

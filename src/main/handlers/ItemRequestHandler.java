package handlers;

import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

public class ItemRequestHandler extends HandlerPrototype implements HttpHandler {
    /**
     * Generic request constructor
     */
    public ItemRequestHandler(){
        //TODO: get the request command parameter
    }

    /**
     * Item request with a command string defined
     * Specify the required keys for each branch
     * @param cmd
     */
    public ItemRequestHandler(String cmd){
        switch (cmd) {
            case "new":
                requiredKeys = new String[]{};
                break;
            case "get":
                requiredKeys = new String[]{};
                break;
            case "edit":
                requiredKeys = new String[]{};
                break;
            case "move":
                requiredKeys = new String[]{};
                break;
            case "delete":
                requiredKeys = new String[]{};
                break;
        }
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        //TODO: route the branch
    }
}

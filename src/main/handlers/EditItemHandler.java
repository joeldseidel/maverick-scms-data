package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.ItemDataManager;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.impl.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class for handling requests to edit user information, including username, password, and user deletion
 * UID should be the ID of the user to change
 * Field should be the field to change, or 'delete' if deleting a user
 * newvalue is the value to replace the old value of Field
 *
 * @author Joshua Famous
 */

public class EditItemHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public EditItemHandler(){
        //Set required keys in array inherited from HandlerPrototype super class
        requiredKeys = new String[] {"cid", "mid", "field", "newvalue", "token"};
        handlerName = "EditItemHandler";
    }

    /**
     * Parse the parameters from the client, verify the CID, determine the operation, perform the edit, and return result
     * @param requestParams
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();
        //Parse the parameters from the client
        String cid = requestParams.getString("cid");
        String mid = requestParams.getString("mid");
        String field = requestParams.getString("field");
        String newvalue = requestParams.getString("newvalue");

        ItemDataManager itemDataManager = new ItemDataManager();
        //Check that CID of User matches requesting CID
        if(!itemDataManager.getItemCID(mid).equals(cid)){
            responseObject.put("message","OutsideCompanyError");
            this.response = responseObject.toString();
        }
        else{
            //Perform requested operation
            switch(field){
                case "name":
                    if(newvalue.length() > 40 || newvalue.length() < 1){
                        responseObject.put("message","NameLengthError");
                        this.response = responseObject.toString();
                    } else{
                        itemDataManager.editName(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "category":
                    if(newvalue.length() > 40 || newvalue.length() < 1){
                        responseObject.put("message","CategoryLengthError");
                        this.response = responseObject.toString();
                    } else {
                        itemDataManager.editCategory(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "delete":
                    itemDataManager.removeItem(mid);
                    responseObject.put("message","Success");
                    this.response = responseObject.toString();
                    break;
                default:
                    responseObject.put("message","InvalidFieldError");
                    this.response = responseObject.toString();
                    break;
            }
        }
    }
}

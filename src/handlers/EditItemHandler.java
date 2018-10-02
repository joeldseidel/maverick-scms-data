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
    private String response = "";
    public EditItemHandler(){
        requiredKeys = new String[] {"cid", "mid", "field", "newvalue", "token"};
    }
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Enter item editing handler");
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            //Request was valid, fulfill the request with params
            fulfillRequest(requestParams);
        } else {
            //Request was invalid, set response to reflect this
            this.response = "invalid request";
        }
        //Create response to client
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to Add Pallet Request : " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }
    @Override
    protected void fulfillRequest(JSONObject requestParams){
        JSONObject responseObject = new JSONObject();

        String cid = requestParams.getString("cid");
        String mid = requestParams.getString("mid");
        String field = requestParams.getString("field");
        String newvalue = requestParams.getString("newvalue");

        //Check that CID of User matches requesting CID
        if(!ItemDataManager.getItemCID(mid).equals(cid)){
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
                        ItemDataManager.editName(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "category":
                    if(newvalue.length() > 40 || newvalue.length() < 1){
                        responseObject.put("message","CategoryLengthError");
                        this.response = responseObject.toString();
                    } else {
                        ItemDataManager.editCategory(mid, newvalue);
                        responseObject.put("message","Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "delete":
                    ItemDataManager.removeItem(mid);
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

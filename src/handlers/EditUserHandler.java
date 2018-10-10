package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserDataManager;

import org.json.JSONObject;

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

public class EditUserHandler extends HandlerPrototype implements HttpHandler {

    public EditUserHandler(){
        requiredKeys = new String[] {"cid", "uid", "field", "newvalue", "token"};
        handlerName = "EditUserHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){

        boolean isVerified;
        JSONObject responseObject = new JSONObject();

        String cid = requestParams.getString("cid");
        int uid = requestParams.getInt("uid");
        String field = requestParams.getString("field");
        String newvalue = requestParams.getString("newvalue");
        String token = requestParams.getString("token");

        //Check that CID of User matches requesting CID
        if(!UserDataManager.getUserCID(uid).equals(cid)){
            responseObject.put("message","OutsideCompanyError");
            this.response = responseObject.toString();
        }
        else {
            //Perform requested operation
            switch (field) {
                case "username":
                    if (newvalue.length() > 30 || newvalue.length() < 1) {
                        responseObject.put("message", "UsernameLengthError");
                        this.response = responseObject.toString();
                    } else {
                        UserDataManager.editUsername(uid, newvalue);
                        responseObject.put("message", "Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "password":
                    if (newvalue.length() > 30 || newvalue.length() < 1) {
                        responseObject.put("message", "PasswordLengthError");
                        this.response = responseObject.toString();
                    } else {
                        UserDataManager.editPassword(uid, newvalue);
                        responseObject.put("message", "Success");
                        this.response = responseObject.toString();
                    }
                    break;
                case "rank":
                    UserDataManager.editRank(uid, newvalue);
                    responseObject.put("message", "Success");
                    this.response = responseObject.toString();
                    break;
                case "delete":
                    UserDataManager.removeUser(uid);
                    responseObject.put("message", "Success");
                    this.response = responseObject.toString();
                    break;
                default:
                    responseObject.put("message", "InvalidFieldError");
                    this.response = responseObject.toString();
                    break;
            }
        }
    }
}

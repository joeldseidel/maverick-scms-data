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

    private String[] requiredKeys = {"cid", "mid", "field", "newvalue", "token"};
    private String response = "";

    public void handle(HttpExchange httpExchange) throws IOException{
        System.out.println("Enter item editing handler");
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        displayRequestValidity(isValidRequest);
        if(isValidRequest){
            fulfillRequest(requestParams);
        } else {
            this.response = "invalid request";
        }
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to user data query : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    @Override
    protected boolean isRequestValid(JSONObject requestParams){
        if(requestParams == null){
            //Request did not come with parameters, is invalid
            System.out.println("Request params invalid");
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                System.out.println("Request params missing key " + requiredKey);
                return false;
            }
        }
        //Request contains all required keys
        return true;
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){

        boolean isVerified;
        JSONObject responseObject = new JSONObject();

        String cid = requestParams.getString("cid");
        String mid = requestParams.getString("mid");
        String field = requestParams.getString("field");
        String newvalue = requestParams.getString("newvalue");
        String token = requestParams.getString("token");

        //Check that CID of User matches requesting CID
        if(!ItemDataManager.getItemCID(mid).equals(cid)){
            responseObject.put("message","OutsideCompanyError");
            this.response = responseObject.toString();
        }
        else{

            //VERIFY TOKEN
            try {

                Algorithm algorithm = Algorithm.HMAC256("secret");
                JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("localhost:6969")
                    .build(); //Reusable verifier instance
                DecodedJWT jwt = verifier.verify(token);
                isVerified = true;
                System.out.println("Token " + token + " was verified");

            } catch (Exception exception){
                //Invalid signature/claims
                isVerified = false;
                System.out.println("Token " + token + " was not verified");
            }

            if(isVerified){

                //Perform requested operation
                switch(field){
                    case "name":
                        if(newvalue.length() > 40 || newvalue.length() < 1){
                            responseObject.put("message","NameLengthError");
                            this.response = responseObject.toString();
                        }
                        else{
                            ItemDataManager.editName(mid, newvalue);
                            responseObject.put("message","Success");
                            this.response = responseObject.toString();
                        }
                        break;
                    case "category":
                        if(newvalue.length() > 40 || newvalue.length() < 1){
                            responseObject.put("message","CategoryLengthError");
                            this.response = responseObject.toString();
                        }
                        else{
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
            else{

                this.response = Boolean.toString(false);

            }

        }

    }
}

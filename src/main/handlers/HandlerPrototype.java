package handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.json.JSONObject;

import java.io.*;

public abstract class HandlerPrototype {
    protected String[] requiredKeys;
    protected String response;
    protected String handlerName;
    protected DatabaseInteraction database;

    private JSONObject GetParameterObject(HttpExchange httpExchange) throws IOException {
        //Fetch the parameter text from the request
        InputStream paramInStream = httpExchange.getRequestBody();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] inBuffer = new byte[2048];
        int readBytes;
        //Read the parameter text in to the byte array and convert to string
        while ((readBytes = paramInStream.read(inBuffer)) != -1) {
            byteArrayOutputStream.write(inBuffer, 0, readBytes);
        }
        String jsonString = byteArrayOutputStream.toString();
        if(!jsonString.equals("")){
            return new JSONObject(jsonString);
        } else {
            return null;
        }
    }

    private void displayRequestValidity(boolean isValidRequest){
        if(isValidRequest){
            System.out.println("Valid Request");
        } else {
            System.out.println("Invalid Request");
        }
    }

    private boolean isTokenValid(String token){
        try{
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("localhost:6969")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            System.out.println("Token " + token + " was verified");
            return true;
        } catch (UnsupportedEncodingException useEx){
            return false;
        }
    }

    protected boolean isRequestValid(JSONObject requestParams) {
        if (requestParams == null) {
            //Request did not come with parameters, is invalid
            System.out.println("Request Params Null");
            return false;
        }
        for (String requiredKey : requiredKeys) {
            if (!requestParams.has(requiredKey)) {
                //Missing a required key, request is invalid
                System.out.println("Request Params Missing Key " + requiredKey);
                return false;
            }
        }
        return !requestParams.has("token") || isTokenValid(requestParams.getString("token"));
    }

    /**
     * Entry point for handler. Get parameters, verify request validity, fulfill request, return response to client
     * @param httpExchange inherited from super class, set from client with params
     * @throws IOException thrown if there is an issue with writing response data to client
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        //Get parameters from client
        JSONObject requestParams = GetParameterObject(httpExchange);
        //Determine validity of request parameters and validate token
        boolean isValidRequest = isRequestValid(requestParams);
        //Display in server console validity of the request for testing purposes
        displayRequestValidity(isValidRequest);
        if (isValidRequest) {
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
        System.out.println("Response to " + handlerName + ": " + this.response);
        //Write response to the client
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    protected abstract void fulfillRequest(JSONObject requestParams);

    protected void initDb(DatabaseType databaseType){
        this.database = new DatabaseInteraction(databaseType);
    }

    protected void disposeDb(){
        this.database.closeConnection();
    }
}

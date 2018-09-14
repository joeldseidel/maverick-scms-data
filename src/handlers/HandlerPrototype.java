package handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.*;

public abstract class HandlerPrototype {
    JSONObject GetParameterObject(HttpExchange httpExchange) throws IOException {
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

    void displayRequestValidity(boolean isValidRequest){
        if(isValidRequest){
            System.out.println("Valid Request");
        } else {
            System.out.println("Invalid Request");
        }
    }

    boolean verifyToken(String token){
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

    protected abstract boolean isRequestValid(JSONObject requestParams);

    protected abstract void fulfillRequest(JSONObject requestParams);
}

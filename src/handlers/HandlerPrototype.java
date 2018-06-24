package handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.*;

public abstract class HandlerPrototype {
    protected JSONObject GetParameterObject(HttpExchange httpExchange) throws IOException {
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
    protected abstract boolean isRequestValid(JSONObject requestParams);

    protected abstract void fulfillRequest(JSONObject requestParams);
}

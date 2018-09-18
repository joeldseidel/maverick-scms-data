package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import maverick_data.DatabaseInteraction;
import maverick_data.Config;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.impl.*;
import com.auth0.jwt.interfaces.*;
import com.auth0.jwt.*;

import maverick_types.MaverickItem;
import managers.ItemDataManager;

public class AddItemHandler extends HandlerPrototype implements HttpHandler {
    private String response;
    public AddItemHandler(){
        requiredKeys = new String[] {"fdaid", "name", "category", "cid", "token"};
    }
    public void handle(HttpExchange httpExchange) throws IOException {
        JSONObject requestParams = GetParameterObject(httpExchange);
        boolean isValidRequest = isRequestValid(requestParams);
        displayRequestValidity(isValidRequest);
        if (isValidRequest) {
            fulfillRequest(requestParams);
        } else {
            this.response = "invalid request";
        }
        int responseCode = isValidRequest ? 200 : 400;
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(responseCode, this.response.length());
        System.out.println("Response to Add Item Request : " + this.response);
        OutputStream os = httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams) {

        boolean isVerified;

        String fdaid = requestParams.getString("fdaid");
        String cid = requestParams.getString("cid");
        String name = requestParams.getString("name");
        String category = requestParams.getString("category");
        String token = requestParams.getString("token");
        MaverickItem thisItem = new MaverickItem(fdaid, name, category, cid);
        ItemDataManager itemDataManager = new ItemDataManager();
        itemDataManager.addItem(thisItem);
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }
}
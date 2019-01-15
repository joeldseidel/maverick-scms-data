package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import maverick_data.DatabaseInteraction;
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

/**
 *  @author Joshua Famous, Joel Seidel
 *
 *  Handler class for adding items to the database
 */


public class AddItemHandler extends HandlerPrototype implements HttpHandler {

    /**
     * Constructor to set this handler's required keys on handler context creation
     */
    public AddItemHandler(){
        //Set required keys in array in inherited from HandlerPrototype super class
        requiredKeys = new String[] {"fdaid", "name", "category", "cid", "token"};
        handlerName = "addItemHandler";
    }

    /**
     * Fulfills valid request. Reads/parses params, performs request actions, and formats response
     * @param requestParams validated parameters sent by the client
     */
    @Override
    protected void fulfillRequest(JSONObject requestParams) {
        //Parse the request parameters
        String fdaid = requestParams.getString("fdaid");
        String cid = requestParams.getString("cid");
        String name = requestParams.getString("name");
        String category = requestParams.getString("category");
        //Instantiate an item object from data received within request parameters
        MaverickItem thisItem = new MaverickItem(fdaid, name, category, cid);
        //Instantiate item data manager to interact with item data
        ItemDataManager itemDataManager = new ItemDataManager();
        //Write created item to the database
        itemDataManager.addItem(thisItem);
        //Create response object
        JSONObject responseObject = new JSONObject();
        responseObject.put("message", "Success");
        this.response = responseObject.toString();
    }
}
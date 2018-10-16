package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * /*
 * @author Joshua Famous
 *
 * Handler class to return a listing of all pallets in their company to the client
 */

public class GetCompanyPalletsHandler extends HandlerPrototype implements HttpHandler {


    public GetCompanyPalletsHandler(){
        requiredKeys = new String[] {"cid", "token"};
        handlerName = "GetCompanyPalletsHandler";
    }

    @Override
    protected void fulfillRequest(JSONObject requestParams){
        String cid = requestParams.getString("cid");
        //Format item data into an object to return
        JSONObject itemDataObject = getItemDataByCompany(cid);
        //If item data fetched, return data, otherwise say no
        this.response = itemDataObject.toString();
    }

    private JSONObject getItemDataByCompany(String cid){
        System.out.println("Attempting to get item data for company : " + cid);
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String getItemDataSql = "SELECT mlot FROM table_pallets WHERE cid = ?";
        PreparedStatement getItemDataStatement = database.prepareStatement(getItemDataSql);
        JSONObject itemDataObject = new JSONObject();
        try{
            getItemDataStatement.setString(1, cid);
            ResultSet getItemDataResults = database.query(getItemDataStatement);
            try{
            itemDataObject.put("arrayResult",getItemDataFormattedResponse(getItemDataResults));
            }
            catch(Exception e){
                System.out.println("Failed to get Formatted Response");
                itemDataObject = null;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            itemDataObject = null;
        } finally {
            database.closeConnection();
        }
        System.out.println("Got Pallet Data Object : " + itemDataObject);
        return itemDataObject;
    }

    /**
     * Convert a result set into a JSON Array
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONArray getItemDataFormattedResponse(ResultSet itemDataResults) throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (itemDataResults.next()) {
            JSONObject obj = new JSONObject();
            int total_rows = itemDataResults.getMetaData().getColumnCount();
            for (int i = 0; i < total_rows; i++) {
                System.out.println("Got " + itemDataResults.getObject(i + 1) + " for " + itemDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase());
                obj.put(itemDataResults.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), itemDataResults.getObject(i + 1));
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }

}
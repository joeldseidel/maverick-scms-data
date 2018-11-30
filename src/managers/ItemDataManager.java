package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.LotType;
import maverick_types.MaverickItem;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstracts away all of the database interaction necessary to work with items in our databases
 */
public class ItemDataManager {

    /**
     * The database connection to use when working with data
     */
    private DatabaseInteraction database;

    /**
     * Constructor for the ItemDataManager class
     */
    public ItemDataManager() {
        this.database = new DatabaseInteraction(DatabaseType.AppData);
    }

    /**
     * addItem adds an item to the database
     */
    public void addItem(MaverickItem item) {
        String qryString = "INSERT INTO table_items (mid, fdaid, name, category, cid) " + "VALUES (\"" +
                item.getMaverickID() + "\", \"" +
                item.getFdaID() + "\", \"" +
                item.getItemName() + "\", \"" +
                item.getItemCategory() + "\", \"" +
                item.getCustomerID() + "\")";

        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        this.database.nonQuery(qryStatement);
    }

    public MaverickItem getItem(String mid){
        String getItemQuery = "SELECT * FROM table_items WHERE mid = ?";
        MaverickItem thisItem = null;
        try{
            PreparedStatement getItemStatement = database.prepareStatement(getItemQuery);
            getItemStatement.setString(1, mid);
            ResultSet getItemResults = database.query(getItemStatement);
            if(getItemResults.next()){
                String fdaid = getItemResults.getString("fdaid");
                String name = getItemResults.getString("name");
                String category = getItemResults.getString("category");
                String cid = getItemResults.getString("cid");
                thisItem = new MaverickItem(mid, fdaid, name, category, cid);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            thisItem = null;
        }
        return thisItem;
    }

    public static long generateItemLotNumber(){
        LotNumberManager lotNumber = new LotNumberManager();
        return lotNumber.generateLotNumber(LotType.Item);
    }

    public boolean itemExists(String mid){
        System.out.println("Attempting to get item count for item with mid : " + mid);
        String itemCountSQL = "SELECT rowid FROM table_items WHERE mid = ?";
        PreparedStatement itemCountStatement = database.prepareStatement(itemCountSQL);
        try{
            itemCountStatement.setString(1, mid);
            ResultSet getItemResults = database.query(itemCountStatement);
            if (getItemResults.next()){
                return true;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
        return false;
    }

    public String getItemCID(String mid){
        String cid = "notfound";
        String getItemCIDSql = "SELECT cid FROM table_items WHERE mid = ?";
        PreparedStatement getItemCIDStatement = database.prepareStatement(getItemCIDSql);
        try{
            getItemCIDStatement.setString(1, mid);
            ResultSet CIDResults = database.query(getItemCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        System.out.println("Got Item CID : " + cid);
        return cid;
    }

    /**
     *getItemDataByCompany returns getItemDataResults
     */

    public ResultSet getItemDataByCompany(String cid){

        String getItemDataSql = "SELECT table_items.mid, table_items.fdaid, table_items.name, table_items.category, table_itempalletmapping.mlot FROM table_items LEFT JOIN table_itempalletmapping ON table_items.mid = table_itempalletmapping.mid AND table_items.cid = ?";
        PreparedStatement getItemDataStatement = database.prepareStatement(getItemDataSql);

        JSONObject itemDataObject = new JSONObject();
        ResultSet getItemDataResults;
        try {
            getItemDataStatement.setString(1, cid);
            getItemDataResults = database.query(getItemDataStatement);

        }
          catch(SQLException sqlEx) {
              sqlEx.printStackTrace();
              getItemDataResults = null;

          }
        return getItemDataResults;
    }


    /**
     * editName changes an item's name
     */
    public void editName(String mid, String newname) {
        String qryString = "UPDATE table_items SET name = ? WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, newname);
            qryStatement.setString(2, mid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * editCategory changes an item's category
     */
    public void editCategory(String mid, String newcategory) {
        String qryString = "UPDATE table_items SET category = ? WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, newcategory);
            qryStatement.setString(2, mid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * removeItem removes an item from the database
     */
    public void removeItem(String mid) {
        String qryString = "DELETE FROM table_items WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * removeFromPallet removes an item from a pallet
     */
    public void removeFromPallet(String mid) {
        String qryString = "DELETE FROM table_itempalletmapping WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * updatePallet changes an item's pallet
     */
    public void updatePallet(String mid, String pallet) {
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) VALUES (?, ?) ON DUPLICATE KEY UPDATE mlot=?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            qryStatement.setString(2, pallet);
            qryStatement.setString(3, pallet);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * Query the item data based on a search term. Check for columns that contain term at any position and match current company
     * @param term the search string from client
     * @param cid the company within which to search the data
     * @return a list of items which match or contain search term
     */
    public List<MaverickItem> searchItemByTerm(String term, String cid){
        //Create the query string and statement
        String searchItemsSql = "SELECT * FROM table_items WHERE (cid = ?) AND (mid = ? OR fdaid = ? OR name LIKE ? OR category LIKE ?)";
        PreparedStatement searchItemsStatement = database.prepareStatement(searchItemsSql);
        List<MaverickItem> searchResultItems = new ArrayList<>();
        try{
            //Set the parameters of the query
            searchItemsStatement.setString(1, cid);
            searchItemsStatement.setString(2, term);
            searchItemsStatement.setString(3, term);
            //Concatenate in the % operator to specify the SQL like operator to look for the whole term at any position
            searchItemsStatement.setString(4, "%"+term+"%");
            searchItemsStatement.setString(5, "%"+term+"%");
            //Perform the query and hope for the best
            ResultSet searchItemsResults = database.query(searchItemsStatement);
            //Create a maverick item object out of each result row for returning
            while(searchItemsResults.next()){
                //Parse the result set to get data for object instantiation
                String mid = searchItemsResults.getString("mid");
                String fdaId = searchItemsResults.getString("fdaid");
                String name = searchItemsResults.getString("name");
                String category = searchItemsResults.getString("category");
                //Instantiate the maverick item object with data from the result row
                MaverickItem thisMitem = new MaverickItem(mid, fdaId, name, category, cid);
                //Add this item to the search result list for return
                searchResultItems.add(thisMitem);
            }
        } catch(SQLException sqlEx) {
            //RIP this query 2018-whenever this popped up
            sqlEx.printStackTrace();
        }
        return searchResultItems;
    }

    /**
     * Convert a list of Maverick items into a JSON array of the same maverick items but in json form
     * @param list the list of maverick items to convert in a json array
     * @return the json array with the converted data
     */
    public JSONArray convertListToJsonArray(List<MaverickItem> list){
        //Create the json array to contain the converted data
        JSONArray jsonArray = new JSONArray();
        //Loop through every item within the list and convert to json object which goes into array
        for(MaverickItem mItem : list){
            //Create JSON object for this item
            JSONObject mItemObj = new JSONObject();
            //Insert the properties from the object into the JSON object
            mItemObj.put("mid", mItem.getMaverickID());
            mItemObj.put("fdaid", mItem.getFdaID());
            mItemObj.put("itemname", mItem.getItemName());
            mItemObj.put("itemcategory", mItem.getItemCategory());
            mItemObj.put("cid", mItem.getCustomerID());
            //Put the created json object into the array
            jsonArray.put(mItemObj);
        }
        return jsonArray;
    }
}

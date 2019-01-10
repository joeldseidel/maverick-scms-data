package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import maverick_data.DatabaseInteraction;
import maverick_types.*;
import maverick_types.FDADeviceTypes.FDADevice;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstracts away all of the database interaction necessary to work with items in our databases
 * @author Joshua Famous, Joel Seidel
 */
public class ItemDataManager extends ManagerPrototype {
    /**
     * Constructor for the ItemDataManager class
     */
    public ItemDataManager() {
        initDb(DatabaseType.AppData);
    }

    /**
     * Add item record to the database
     * @param item item to add to the database
     */
    public void addItem(MaverickItem item) {
        //Create add item query
        String qryString = "INSERT INTO table_items (mid, fdaid, name, category, cid) " + "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement addItemStmt = database.prepareStatement(qryString);
        try{
            addItemStmt.setString(1, item.getMaverickID());
            addItemStmt.setString(2, item.getFdaID());
            addItemStmt.setString(3, item.getItemName());
            addItemStmt.setString(4, item.getItemCategory());
            addItemStmt.setString(5, item.getCustomerID());
        } catch (SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        //Perform add item query
        database.nonQuery(addItemStmt);
    }

    /**
     * Get item from the database by mlot
     * @param mid mlot of the item
     * @return maverick item instance of specified item
     */
    public MaverickItem getItem(String mid){
        //Create get item query
        String getItemQuery = "SELECT * FROM table_items WHERE mid = ?";
        MaverickItem thisItem = null;
        try{
            PreparedStatement getItemStatement = database.prepareStatement(getItemQuery);
            getItemStatement.setString(1, mid);
            ResultSet getItemResults = database.query(getItemStatement);
            if(getItemResults.next()){
                //Get fields for instantiation of maverick item
                String fdaid = getItemResults.getString("fdaid");
                String name = getItemResults.getString("name");
                String category = getItemResults.getString("category");
                String cid = getItemResults.getString("cid");
                thisItem = new MaverickItem(mid, fdaid, name, category, cid);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            //Couldn't get item
            thisItem = null;
        }
        return thisItem;
    }

    /**
     * Generate an mlot value for this item
     * @return generated mlot value
     */
    public static long generateItemLotNumber(){
        LotNumberManager lotNumber = new LotNumberManager();
        //Generate an return generated lot number
        return lotNumber.generateLotNumber(LotType.Item);
    }

    /**
     * Get if an item exists in the database with a specified mlot value
     * @param mid mlot value to check
     * @return boolean exists in data
     */
    public boolean itemExists(String mid){
        System.out.println("Attempting to get item count for item with mid : " + mid);
        //Create get item query
        String itemCountSQL = "SELECT rowid FROM table_items WHERE mid = ?";
        PreparedStatement itemCountStatement = database.prepareStatement(itemCountSQL);
        try{
            itemCountStatement.setString(1, mid);
            //Perform query
            ResultSet getItemResults = database.query(itemCountStatement);
            if (getItemResults.next()){
                //Results have at least one row, item exists
                return true;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
        //Item did not have more than one row, item does not exist
        return false;
    }

    /**
     * Get the company id associated with an item
     * @param mid lot of the device to lookup
     * @return company id of the item
     */
    public String getItemCID(String mid){
        String cid = "notfound";
        //Create the get company id query
        String getItemCIDSql = "SELECT cid FROM table_items WHERE mid = ?";
        PreparedStatement getItemCIDStatement = database.prepareStatement(getItemCIDSql);
        try{
            getItemCIDStatement.setString(1, mid);
            //Perform the query
            ResultSet CIDResults = database.query(getItemCIDStatement);
            //Advance result set cursor as there will only be one result
            //FIXME: remove the not found and change to a null return type
            CIDResults.next();
            //Get customer id field
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        System.out.println("Got Item CID : " + cid);
        return cid;
    }

    /**
     * Get item data related to a specific company
     * @param cid company id of the specified company
     * @return result set containing the item data for the company
     */
    public ResultSet getItemDataByCompany(String cid){
        //Create the get item data by company query
        String getItemDataSql = "SELECT table_items.mid, table_items.fdaid, table_items.name, table_items.category, table_itempalletmapping.mlot FROM table_items LEFT JOIN table_itempalletmapping ON table_items.mid = table_itempalletmapping.mid AND table_items.cid = ?";
        PreparedStatement getItemDataStatement = database.prepareStatement(getItemDataSql);

        //FIXME remove this unused variable
        JSONObject itemDataObject = new JSONObject();
        ResultSet getItemDataResults;
        try {
            //Perform query
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
     * Change the name of an item in the database
     * @param mid mlot of the item to edit
     * @param newname name to update the item to have
     */
    public void editName(String mid, String newname) {
        //Create the update item query
        String qryString = "UPDATE table_items SET name = ? WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, newname);
            qryStatement.setString(2, mid);
            //Perform the update nonquery
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            //FIXME db conn is closed on finalize, not on the completion of a query
            database.closeConnection();
        }
    }

    /**
     * Change the category of an item in the database
     * @param mid mlot of the item to edit
     * @param newcategory new category for the item
     */
    public void editCategory(String mid, String newcategory) {
        //Create the update category query
        String qryString = "UPDATE table_items SET category = ? WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, newcategory);
            qryStatement.setString(2, mid);
            //Perform the update nonquery
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            //FIXME db conn is closed on finalize, not on the completion of a query
            database.closeConnection();
        }
    }

    /**
     * Remove an item from the database
     * @param mid mlot of the item to remove from the database
     */
    public void removeItem(String mid) {
        //Create the delete query
        String qryString = "DELETE FROM table_items WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            //Perform the delete nonquery
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            //FIXME db conn is closed on finalize, not on the completion of a query
            database.closeConnection();
        }
    }

    /**
     * Remove an item from a pallet
     * @param mid mlot of the item to remove from its current pallet
     */
    public void removeFromPallet(String mid) {
        //Create delete from pallet mapping query
        String qryString = "DELETE FROM table_itempalletmapping WHERE mid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            //Perform delete nonquery
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            //FIXME db conn is closed on finalize, not on the completion of a query
            database.closeConnection();
        }
    }

    /**
     * Change an items pallet
     * @param mid mlot of the item to change the pallet for
     * @param pallet mlot of the pallet to change this item mapping to
     */
    public void updatePallet(String mid, String pallet) {
        //Create pallet mapping record insert query
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) VALUES (?, ?) ON DUPLICATE KEY UPDATE mlot=?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, mid);
            qryStatement.setString(2, pallet);
            qryStatement.setString(3, pallet);
            //Perform pallet mapping record insert query
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            //FIXME db conn is closed on finalize, not on the completion of a query
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

    /**
     * Create a record for each fda device that is imported into the Maverick system from FDA data
     * @param fdaDevices list of fda devices to import
     * @param cid company id to assign the company to
     */
    public void importFDADevices(List<FDADevice> fdaDevices, String cid){
        List<MaverickItem> importedMItems = new ArrayList<MaverickItem>();
        LotNumberManager lotNumberManager = new LotNumberManager();
        //Create a new maverick item record for each of the passed devices
        for(FDADevice device : fdaDevices){
            //Get the necessary fields for instantiation
            String mid = Long.toString(lotNumberManager.generateLotNumber(LotType.Item));
            String fda_id = device.getProperty("fda_id").getPropertyValue().toString();
            String name = device.getProperty("device_name").getPropertyValue().toString();
            String category = device.getProperty("medical_specialty_description").getPropertyValue().toString();
            //Instantiate a new maverick item to import
            MaverickItem thisItem = new MaverickItem(mid, fda_id, name, category, cid);
            //Add the new maverick item to the collection
            importedMItems.add(thisItem);
        }
        //Create item database records for each of the imported maverick items
        addItem(importedMItems);
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        //Create the cycle in movement for the imported devices
        deviceMovementEventManager.initializeItemMovement(importedMItems);
    }

    /**
     * Batch add items to the database
     * @param items list of items that to be imported into the data
     */
    private void addItem(List<MaverickItem> items){
        //Set auto commit to off to allow batch creation
        database.setAutoCommit(false);
        //Create add item query
        PreparedStatement addItemStmt = database.prepareStatement("INSERT INTO table_items(mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)");
        //Prepare a statement for each of the items
        for(MaverickItem item : items){
            try{
                //Create single item statement
                addItemStmt.setString(1, item.getMaverickID());
                addItemStmt.setString(2, item.getFdaID());
                addItemStmt.setString(3, item.getItemName());
                addItemStmt.setString(4, item.getItemCategory());
                addItemStmt.setString(5, item.getCustomerID());
                //Add single item statement to the query batch
                addItemStmt.addBatch();
            } catch(SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
        //Run batch
        database.batchNonQuery(addItemStmt);
        //Commit batch changes to the database
        database.commitBatches();
        //Set auto commit back to on as we are done batching
        database.setAutoCommit(true);
    }
}

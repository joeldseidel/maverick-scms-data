package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import maverick_data.DatabaseInteraction;
import maverick_types.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Manager class for creating pallets, and adding/removing items from them
 * @author Joshua Famous
 */

public class PalletDataManager extends ManagerPrototype {
    /**
     * Constructor for the PalletDataManager class
     */
    public PalletDataManager() {
        initDb(DatabaseType.AppData);
    }

    /**
     * Create a new pallet record
     * @param pallet pallet to add to the database
     */
    public void addPallet(MaverickPallet pallet) {
        System.out.println("Trying to create pallet");
        //Create insert pallet record query
        String qryString = "INSERT INTO table_pallets (cid, mlot) VALUES (?, ?)";
        try{
            //Perform insert pallet record query
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            System.out.println("Attempting add with " + pallet.getCustomerID() + " and " + pallet.getPalletID());
            qryStatement.setString(1, pallet.getCustomerID());
            qryStatement.setString(2, pallet.getPalletID());
            System.out.println("Trying to query with CID " + pallet.getCustomerID() + " and PID " + pallet.getPalletID());
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();
        //Initialize a pallet movement, essentially the cycle in of the apllet
        palletMovementEventManager.initializePalletMovement(pallet);
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        //Add all of the items within the pallet to the new pallet and initialize their cycle in
        for(MaverickItem item : pallet.getItems()){
            //Add the item to the pallet, create the mapping record
            this.addItemToPallet(item, pallet.getPalletID());
            //Cycle in each device within the pallet
            deviceMovementEventManager.initializeItemMovement(item);
        }
    }

    /**
     * Add an item to the pallet mapping by maverick item object
     * @param item item to add to the pallet
     * @param palletId id of the pallet to add to
     */
    private void addItemToPallet(MaverickItem item, String palletId){
        //Create a new mapping record for the item/pallet
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) " + "VALUES (?, ?)";
        try{
            //Perform the insert mapping record query
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, item.getMaverickID());
            qryStatement.setString(2, palletId);
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Add item to pallet by mlot number
     * @param mid mlot number of the item to add to the pallet
     * @param palletId id of the pallet to add to
     */
    public void addItemToPallet(int mid, String palletId){
        //Create a new mapping record for the item / pallet
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) " + "VALUES (?, ?)";
        try{
            //Perform new mapping record query
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, Integer.toString(mid));
            qryStatement.setString(2, palletId);
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Get the items of a specific pallet by pallet id
     * @param palletId id of the pallet to fetch
     * @return list of maverick items within the pallet
     */
    public List<MaverickItem> getPalletItems(String palletId){
        //Create get items by mlot query
        String getPalletItemsSql = "SELECT * FROM table_itempalletmapping WHERE mlot = ?";
        List<MaverickItem> palletItems = new ArrayList<>();
        try{
            //Perform get items by mlot query
            PreparedStatement palletItemsStatement = this.database.prepareStatement(getPalletItemsSql);
            palletItemsStatement.setString(1, palletId);
            ResultSet palletItemsResults = database.query(palletItemsStatement);
            ItemDataManager itemDataManager = new ItemDataManager();
            //Create a maverick item object instance for each of the resulting item records
            while(palletItemsResults.next()){
                //Get the mid from the pallet mapping
                String mid = palletItemsResults.getString("mid");
                MaverickItem thisItem = itemDataManager.getItem(mid);
                //Add this maverick item to the pallet collection
                palletItems.add(thisItem);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return palletItems;
    }

    /**
     * Determine if pallet exists
     * @param palletId id of the pallet to determine existance
     * @return if pallet exists boolean
     */
    public boolean palletExists(String palletId){
        System.out.println("Attempting to get pallet validity for pallet with id : " + palletId);
        //Create pallet to get query
        String palletCountSql = "SELECT * FROM table_pallets WHERE mlot = ?";
        PreparedStatement palletCountStatement = database.prepareStatement(palletCountSql);
        try{
            //Perform pallet to get query
            palletCountStatement.setString(1, "" + palletId);
            ResultSet getPalletResults = database.query(palletCountStatement);
            if(getPalletResults.next()){
                //Pallet count has at least one
                return true;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
        //pallet count does not exist
        return false;
    }

    /**
     * Get company name of pallet
     * @param palletId id of the pallet to get cid of
     * @return cid of the pallet
     */
    public String getPalletCID(String palletId){
        String cid = "notfound";
        //Create get pallet cid query
        String getPalletCIDSql = "SELECT cid FROM table_pallets WHERE mlot = ?";
        PreparedStatement getPalletCIDStatement = database.prepareStatement(getPalletCIDSql);
        try{
            //Perform get pallet cid query
            getPalletCIDStatement.setString(1, palletId);
            ResultSet CIDResults = database.query(getPalletCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        System.out.println("Got Pallet CID : " + cid);
        return cid;
    }

    /**
     * removePallet removes a pallet from the database
     */
    public void removePallet(String palletId) {
        //First, remove any items from the pallet
        String qryString = "DELETE FROM table_itempalletmapping WHERE mlot = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, palletId);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        //Finally, delete the pallet itself
        qryString = "DELETE FROM table_pallets WHERE mlot = ?";
        qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, palletId);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Convert pallet data json array to a list of maverick pallet objects
     * @param palletJsonArray the pallet json array to be converted
     * @return a list of maverickpallet objects
     */
    public static List<MaverickPallet> parseFromJsonArray(JSONArray palletJsonArray){
        List<MaverickPallet> palletList = new ArrayList<>();
        //Loop through each of the pallet data objects within the json array and convert each to a pallet, insert into the new array
        for(int i = 0; i < palletJsonArray.length(); i++){
            //Get the current object from the array
            JSONObject thisPalletObj = palletJsonArray.getJSONObject(i);
            //Get the necessary parameters from the data objects
            String cid = thisPalletObj.getString("cid");
            String mlot = thisPalletObj.getString("mlot");
            //Instantiate the pallet object with taken parameters
            MaverickPallet thisPallet = new MaverickPallet(cid, mlot);
            //Add the new pallet object to the list for return
            palletList.add(thisPallet);
        }
        return palletList;
    }
}

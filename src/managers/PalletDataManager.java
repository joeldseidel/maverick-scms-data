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
 * /*
 * @author Joshua Famous
 *
 * Manager class for creating pallets, and adding/removing items from them
 */

public class PalletDataManager {

    /**
     * The database connection to use when working with data
     */
    private DatabaseInteraction database;

    /**
     * Constructor for the PalletDataManager class
     */
    public PalletDataManager() {
        this.database = new DatabaseInteraction(DatabaseType.AppData);
    }

    /**
     * addPallet adds a pallet to the database
     */
    public void addPallet(MaverickPallet pallet) {
        System.out.println("Trying to create pallet");
        String qryString = "INSERT INTO table_pallets (cid, mlot) VALUES (?, ?)";
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, pallet.getCustomerID());
            qryStatement.setString(2, pallet.getPalletID());
            System.out.println("Trying to query with CID " + pallet.getCustomerID() + " and PID " + pallet.getPalletID());
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        /**PalletMovementEventManager palletMovementEventManager = new PalletMovementEventManager();
        palletMovementEventManager.initializePalletMovement(pallet);
        DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();
        for(MaverickItem item : pallet.getItems()){
            this.addItemToPallet(item, pallet.getPalletID());
            deviceMovementEventManager.initializeItemMovement(item);
        }**/
    }

    public void addItemToPallet(MaverickItem item, String palletId){
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) " + "VALUES (?, ?)";
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, item.getMaverickID());
            qryStatement.setString(2, palletId);
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    public void addItemToPallet(int mid, String palletId){
        String qryString = "INSERT INTO table_itempalletmapping (mid, mlot) " + "VALUES (?, ?)";
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, Integer.toString(mid));
            qryStatement.setString(2, palletId);
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    public List<MaverickItem> getPalletItems(String palletId){
        String getPalletItemsSql = "SELECT * FROM table_itempalletmapping WHERE mlot = ?";
        List<MaverickItem> palletItems = new ArrayList<>();
        try{
            PreparedStatement palletItemsStatement = this.database.prepareStatement(getPalletItemsSql);
            palletItemsStatement.setString(1, palletId);
            ResultSet palletItemsResults = database.query(palletItemsStatement);
            ItemDataManager itemDataManager = new ItemDataManager();
            while(palletItemsResults.next()){
                String mid = palletItemsResults.getString("mid");
                MaverickItem thisItem = itemDataManager.getItem(mid);
                palletItems.add(thisItem);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return palletItems;
    }

    public boolean palletExists(String palletId){
        System.out.println("Attempting to get pallet validity for pallet with id : " + palletId);
        String palletCountSql = "SELECT * FROM table_pallets WHERE mlot = ?";
        PreparedStatement palletCountStatement = database.prepareStatement(palletCountSql);
        try{
            palletCountStatement.setString(1, "" + palletId);
            ResultSet getPalletResults = database.query(palletCountStatement);
            if(getPalletResults.next()){
                return true;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
        return false;
    }

    public String getPalletCID(String palletId){
        String cid = "notfound";
        String getPalletCIDSql = "SELECT cid FROM table_pallets WHERE mlot = ?";
        PreparedStatement getPalletCIDStatement = database.prepareStatement(getPalletCIDSql);
        try{
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

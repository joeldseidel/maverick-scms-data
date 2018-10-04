package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.LotType;
import maverick_types.MaverickItem;

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

    public static boolean itemExists(String mid){
        System.out.println("Attempting to get item count for item with mid : " + mid);
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String itemCountSQL = "SELECT rowid FROM table_items WHERE mid = ?";
        PreparedStatement itemCountStatement = database.prepareStatement(itemCountSQL);
        try{
            itemCountStatement.setString(1, mid);
            ResultSet getItemResults = database.query(itemCountStatement);
            while(getItemResults.next()){
                return true;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        } finally {
            database.closeConnection();
        }
        return false;
    }

    public static String getItemCID(String mid){
        String cid = "notfound";
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String getItemCIDSql = "SELECT cid FROM table_items WHERE mid = ?";
        PreparedStatement getItemCIDStatement = database.prepareStatement(getItemCIDSql);
        try{
            getItemCIDStatement.setString(1, mid);
            ResultSet CIDResults = database.query(getItemCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
        System.out.println("Got Item CID : " + cid);
        return cid;
    }

    /**
     * editName changes an item's name
     */
    public static void editName(String mid, String newname) {
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
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
    public static void editCategory(String mid, String newcategory) {
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
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
    public static void removeItem(String mid) {
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
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
    public static void removeFromPallet(String mid) {
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
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
    public static void updatePallet(String mid, String pallet) {
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String qryString = "INSERT INTO table_itempalletmapping (mid, pallet) VALUES (?, ?) ON DUPLICATE KEY UPDATE pallet=?";
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

}

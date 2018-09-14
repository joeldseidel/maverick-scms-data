package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.MaverickItem;
import maverick_types.MaverickPallet;
import maverick_data.Config;

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
        String qryString = "INSERT INTO table_pallets (cid) VALUES (?)";
        int myid = -1;
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, pallet.getCustomerID());
            myid = this.database.nonQueryWithIdCallback(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        for(MaverickItem item : pallet.getItems()){
            this.addItemToPallet(item, myid);
        }
    }

    public void addItemToPallet(MaverickItem item, int palletid){
        String qryString = "INSERT INTO table_itempalletmapping (mid, pallet) " + "VALUES (?, ?)";
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, item.getMaverickID());
            qryStatement.setString(2, Integer.toString(palletid));
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    public void addItemToPallet(int mid, int palletid){
        String qryString = "INSERT INTO table_itempalletmapping (mid, pallet) " + "VALUES (?, ?)";
        try{
            PreparedStatement qryStatement = this.database.prepareStatement(qryString);
            qryStatement.setString(1, Integer.toString(mid));
            qryStatement.setString(2, Integer.toString(palletid));
            this.database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    public static boolean palletExists(int pallet){
        System.out.println("Attempting to get pallet validity for pallet with id : " + pallet);
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String palletCountSql = "SELECT * FROM table_pallets WHERE id = ?";
        PreparedStatement palletCountStatement = database.prepareStatement(palletCountSql);
        try{
            palletCountStatement.setString(1, ""+pallet);
            ResultSet getPalletResults = database.query(palletCountStatement);
            while(getPalletResults.next()){
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

    public static String getPalletCID(int pallet){
        String cid = "notfound";
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String getPalletCIDSql = "SELECT cid FROM table_pallets WHERE id = ?";
        PreparedStatement getPalletCIDStatement = database.prepareStatement(getPalletCIDSql);
        try{
            getPalletCIDStatement.setString(1, ""+pallet);
            ResultSet CIDResults = database.query(getPalletCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
        System.out.println("Got Pallet CID : " + cid);
        return cid;
    }

    /**
     * removePallet removes a pallet from the database
     */
    public static void removePallet(int pid) {
        //First, remove any items from the pallet
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String qryString = "DELETE FROM table_itempalletmapping WHERE pallet = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, ""+pid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        //Finally, delete the pallet itself
        qryString = "DELETE FROM table_pallets WHERE id = ?";
        qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, ""+pid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

}

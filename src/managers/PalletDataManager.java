package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import maverick_data.DatabaseInteraction;
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
        this.database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
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

}

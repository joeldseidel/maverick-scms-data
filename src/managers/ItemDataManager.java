package managers;

import java.sql.PreparedStatement;

import maverick_data.DatabaseInteraction;
import maverick_types.MaverickItem;
import maverick_data.Config;

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
        this.database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
    }

    /**
     * addItem adds an item to the database
     */
    public void addItem(MaverickItem item) {
        String qryString = "INSERT INTO table_items (mid, fdaid, name, category, cid) " + "VALUES (\"" +
                item.getMaverickID() + "\", \"" +
                Integer.toString(item.getFdaID()) + "\", \"" +
                item.getItemName() + "\", \"" +
                item.getItemCategory() + "\", \"" +
                item.getCustomerID() + "\")";

        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        this.database.nonQuery(qryStatement);
    }

    public void generateItemLotNumber(){

    }
}

package managers;

import java.sql.PreparedStatement;

import maverick_data.DatabaseInteraction;
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
        String host = "staging-itemdb.mavericksystems.us";
        int port = 3306;
        String user = "MavAdmin";
        String pass = "Lt7e^PV%6vRi5l4BcSw";
        this.database = new DatabaseInteraction(host, port, user, pass);
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

}

package managers;

import maverick_data.DatabaseInteraction;

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
        String pass = "CurrentPass";
        this.database = new DatabaseInteraction(host, port, user, pass);
    }

    /**
     * addItem adds an item to the database
     */
    public void addItem() {
        // TODO: generate a Maverick-ID for the item
        // TODO: grab the FDA-ID for the item
        // TODO: build a query against the DB
    }

}

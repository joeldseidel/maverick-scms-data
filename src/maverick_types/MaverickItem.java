package maverick_types;
/**
 * A single item
 */
public class MaverickItem {

    private String maverickID;
    private int fdaID;
    private String itemName;
    private String itemCategory;
    private String customerID;

    /**
     * Constructor for a new MaverickItem
     */
    public MaverickItem(int fdaID, String itemName, String itemCategory, String customerID) {
        // TODO: generate a Maverick ID from a utility function, or something else
        // ~Josh~ Using this for now so I can add more than one item to the database for testing
        this.maverickID = "M-" + (int)((Math.random() * 10000) - 1);
        this.fdaID = fdaID;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.customerID = customerID;
    }

    /**
     * Setter for the maverickID
     * @param maverickID the new Maverick-ID to use
     */
    public void setMaverickID(String maverickID) {
        this.maverickID = maverickID;
    }

    /**
     * Getter for maverickID
     * @return maverickID
     */
    public String getMaverickID() {
        return this.maverickID;
    }

    /**
     * Getter for fdaID
     * @return fdaID
     */
    public int getFdaID() {
        return this.fdaID;
    }

    /**
     * Getter for itemName
     * @return itemName
     */
    public String getItemName() {
        return this.itemName;
    }

    /**
     * Getter for itemCategory
     * @return itemCategory
     */
    public String getItemCategory() {
        return this.itemCategory;
    }

    /**
     * Getter for customerID
     * @return customerID
     */
    public String getCustomerID() {
        return this.customerID;
    }
}

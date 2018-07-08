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
        this.maverickID = "M-1111";
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
}

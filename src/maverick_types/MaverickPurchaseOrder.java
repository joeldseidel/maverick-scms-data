package maverick_types;

import java.util.ArrayList;

/**
 * A single item
 */
public class MaverickPurchaseOrder {

    private String number;
    private String datePlaced;
    private String placingCompany;
    private String customerID;
    private ArrayList<MaverickPurchaseOrderLine> lines;

    /**
     * Constructor for a new MaverickItem
     */
    public MaverickPurchaseOrder(String number, String datePlaced, String placingCompany, String customerID) {
        this.number = number;
        this.datePlaced = datePlaced;
        this.placingCompany = placingCompany;
        this.customerID = customerID;
        this.lines = new ArrayList<MaverickPurchaseOrderLine>();
    }

    /**
     * Setter for the number
     * @param number the new number to use
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Getter for number
     * @return number
     */
    public String getNumber() {
        return this.number;
    }

    /**
     * Setter for the date placed
     * @param datePlaced the new date placed to use
     */
    public void setDatePlaced(String datePlaced) {
        this.datePlaced = datePlaced;
    }

    /**
     * Getter for date placed
     * @return date placed
     */
    public String getDatePlaced() {
        return this.datePlaced;
    }

    /**
     * Setter for the placing company
     * @param company the new placing company to use
     */
    public void setCompany(String placingCompany) {
        this.placingCompany = placingCompany;
    }

    /**
     * Getter for placing company
     * @return placing company
     */
    public String getCompany() {
        return this.placingCompany;
    }
    
    /**
     * Getter for company ID
     * @return customer ID
     */
    public String getCustomer() {
        return this.customerID;
    }

    /**
     * Function to add a new line to this purchase order
     * @param the purchase order line object to add
     */
    public void addLine(MaverickPurchaseOrderLine newLine){
        this.lines.add(newLine);
    }

    /**
     * Function to get all purchase order lines
     * @return an arraylist of MaverickPurchaseOrderLine items
     */
    public ArrayList<MaverickPurchaseOrderLine> getLines(){
        return this.lines;
    }

}

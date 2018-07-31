package maverick_types;

import java.util.ArrayList;

/**
 * A pallet - a grouping of MaverickItems
 */
public class MaverickPallet {

	private ArrayList<MaverickItem> items;
    private String customerID;
	
	/**
	 * @param customerID
	 * @param palletid
	 */
	public MaverickPallet(String customerID) {
		this.customerID = customerID;
	}

	/**
	 * @return the items
	 */
	public ArrayList<MaverickItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(ArrayList<MaverickItem> items) {
		this.items = items;
	}

	/**
	 * @return the customerID
	 */
	public String getCustomerID() {
		return customerID;
	}

	/**
	 * @param customerID the customerID to set
	 */
	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	/**
	 * @param item the MaverickItem to add
	 */
	public void addItem(MaverickItem item) {
		this.items.add(item);
	}
    
}

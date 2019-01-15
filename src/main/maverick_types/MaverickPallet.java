package maverick_types;

import managers.LotNumberManager;

import java.util.ArrayList;

/**
 * A pallet - a grouping of MaverickItems
 */
public class MaverickPallet {

	private ArrayList<MaverickItem> items;
    private String customerID;
    private String palletID;
	
	/**
	 * @param customerID
	 * @param palletid
	 */
	public MaverickPallet(String customerID) {
		this.customerID = customerID;
		items = new ArrayList<MaverickItem>();
		LotNumberManager lotNumberManager = new LotNumberManager();
		this.palletID = Long.toString(lotNumberManager.generateLotNumber(LotType.Pallet));
	}

	public MaverickPallet(String customerID, String palletID){
		this.customerID = customerID;
		items = new ArrayList<MaverickItem>();
		this.palletID = palletID;
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

	public String getPalletID() { return this.palletID; }
    
}

package maverick_types;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A single item
 */
public class MaverickPurchaseOrderLine {

    private int lineNumber;
    private String supplierPartNumber;
    private String partDescription;
    private Date deliveryDate;
    private double quantity;
    private double price;
    
	/**
	 * @param lineNumber
	 * @param supplierPartNumber
	 * @param quantity
	 * @param partDescription
	 * @param deliveryDate
	 * @param price
	 */
	public MaverickPurchaseOrderLine(int lineNumber, String supplierPartNumber, double quantity, String partDescription,
			Date deliveryDate, double price) {
		this.lineNumber = lineNumber;
		this.supplierPartNumber = supplierPartNumber;
		this.quantity = quantity;
		this.partDescription = partDescription;
		this.deliveryDate = deliveryDate;
		this.price = price;
	}
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	/**
	 * @return the supplierPartNumber
	 */
	public String getSupplierPartNumber() {
		return supplierPartNumber;
	}
	/**
	 * @param supplierPartNumber the supplierPartNumber to set
	 */
	public void setSupplierPartNumber(String supplierPartNumber) {
		this.supplierPartNumber = supplierPartNumber;
	}
	/**
	 * @return the partDescription
	 */
	public String getPartDescription() {
		return partDescription;
	}
	/**
	 * @param partDescription the partDescription to set
	 */
	public void setPartDescription(String partDescription) {
		this.partDescription = partDescription;
	}
	/**
	 * @return the deliveryDate
	 */
	public Date getDeliveryDate() {
		return deliveryDate;
	}
	/**
	 * @param deliveryDate the deliveryDate to set
	 */
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}
	/**
	 * @return the quantity
	 */
	public double getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}
    
    

}

package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.MaverickPallet;
import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;

/**
 * Abstracts away all of the database interaction necessary to work with items in our databases
 */
public class PurchaseOrderDataManager {

    /**
     * The database connection to use when working with data
     */
    private DatabaseInteraction database;

    /**
     * Constructor for the PurchaseOrderDataManager class
     */
    public PurchaseOrderDataManager() {
        this.database = new DatabaseInteraction(DatabaseType.AppData);
    }

    /**
     * addPurchaseOrder adds a purchase order to the database
     */
    public void addPurchaseOrder(MaverickPurchaseOrder po) {
        String qryString = "INSERT INTO table_po (cid, number, dateplaced, placingcompany) " + "VALUES (\"" +
                po.getCustomer() + "\", \"" +
                po.getNumber() + "\", \"" +
                po.getDatePlaced() + "\", \"" +
                po.getCompany() + "\")";
        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        int poid = this.database.nonQueryWithIdCallback(qryStatement);
        for(MaverickPurchaseOrderLine line : po.getLines()){
            this.addPurchaseOrderLine(line, poid);
        }
    }

    public void addPurchaseOrderLine(MaverickPurchaseOrderLine line, int poid){
        String qryString = "INSERT INTO table_polines (poid, line, supplierpartnum, partdesc, deliverydate, quantity, price) " + "VALUES (\"" + 
                poid + "\", \"" +
                line.getLineNumber() + "\", \"" +
                line.getSupplierPartNumber() + "\", \"" +
                line.getPartDescription() + "\", \"" +
                line.getDeliveryDate() + "\", \"" +
                line.getQuantity() + "\", \"" +
                line.getPrice() + "\")";

        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        this.database.nonQuery(qryStatement);
    }

    public List<MaverickPurchaseOrderLine> getPurchaseOrderLines(MaverickPurchaseOrder po){
        String getPurchaseOrderLinesSql = "SELECT * FROM table_polines WHERE poid = ?";
        PreparedStatement getPurchaseOrderLines = database.prepareStatement(getPurchaseOrderLinesSql);
        List<MaverickPurchaseOrderLine> purchaseOrderLines = new ArrayList<>();
        try{
            getPurchaseOrderLines.setInt(1, po.getId());
            ResultSet purchaseOrderLineResults = database.query(getPurchaseOrderLines);
            while(purchaseOrderLineResults.next()){
                int poid = purchaseOrderLineResults.getInt("poid");
                int lineNo = purchaseOrderLineResults.getInt("line");
                String supplierPartNo = purchaseOrderLineResults.getString("supplierpartnum");
                String partDesc = purchaseOrderLineResults.getString("partdesc");
                Date deliveryDate = purchaseOrderLineResults.getDate("deliverydate");
                double quantity = purchaseOrderLineResults.getDouble("quantity");
                double price = purchaseOrderLineResults.getDouble("price");
                MaverickPurchaseOrderLine thisMpol = new MaverickPurchaseOrderLine(poid, lineNo, supplierPartNo, partDesc, deliveryDate.toString(), quantity, price);
                purchaseOrderLines.add(thisMpol);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return purchaseOrderLines;
    }

    public List<MaverickPurchaseOrderLine> getPurchaseOrderLines(MaverickPurchaseOrder po, int lineNumber){
        String getPurchaseOrderLineSql = "SELECT * FROM table_polines WHERE poid = ? AND line = ?";
        PreparedStatement getPurchaseOrderLineStatement = database.prepareStatement(getPurchaseOrderLineSql);
        List<MaverickPurchaseOrderLine> purchaseOrderLines = new ArrayList<>();
        try{
            getPurchaseOrderLineStatement.setInt(1, po.getId());
            getPurchaseOrderLineStatement.setInt(2, lineNumber);
            ResultSet purchaseOrderLineResults = database.query(getPurchaseOrderLineStatement);
            while(purchaseOrderLineResults.next()){
                int poid = purchaseOrderLineResults.getInt("poid");
                int lineNo = purchaseOrderLineResults.getInt("line");
                String supplierPartNo = purchaseOrderLineResults.getString("supplierpartnum");
                String partDesc = purchaseOrderLineResults.getString("partdesc");
                Date deliveryDate = purchaseOrderLineResults.getDate("deliverydate");
                double quantity = purchaseOrderLineResults.getDouble("quantity");
                double price = purchaseOrderLineResults.getDouble("price");
                MaverickPurchaseOrderLine thisMpol = new MaverickPurchaseOrderLine(poid, lineNo, supplierPartNo, partDesc, deliveryDate.toString(), quantity, price);
                purchaseOrderLines.add(thisMpol);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return purchaseOrderLines;
    }

    public void bindPallet(int poid, int poline, MaverickPallet pallet){
        String insertBindingRecordSql = "INSERT INTO po_pallet_mapping (mlot, poid, poline, bindtimestamp) VALUES (?, ?, ?, NOW())";
        PreparedStatement insertBindingRecordStatement = database.prepareStatement(insertBindingRecordSql);
        try{
            insertBindingRecordStatement.setString(1, pallet.getPalletID());
            insertBindingRecordStatement.setInt(2, poid);
            insertBindingRecordStatement.setInt(3, poline);
            database.nonQuery(insertBindingRecordStatement);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }
}

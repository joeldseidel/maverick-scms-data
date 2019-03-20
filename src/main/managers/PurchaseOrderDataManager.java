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
public class PurchaseOrderDataManager extends ManagerPrototype {
    /**
     * Default constructor to initialize the database connection for the manager
     */
    public PurchaseOrderDataManager(DatabaseInteraction database) {
        this.database = database;
    }

    /**
     * Create a new purchase order and insert record into database
     * @param po purchase order to create
     */
    public void addPurchaseOrder(MaverickPurchaseOrder po) {
        //Create new purchase order insert query
        String qryString = "INSERT INTO table_po (cid, number, dateplaced, placingcompany) " + "VALUES (?, ?, ?, ?)";
        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        try {
            //Perform query preparation
            qryStatement.setString(1, po.getCustomer());
            qryStatement.setString(2, po.getNumber());
            qryStatement.setString(3, po.getDatePlaced());
            qryStatement.setString(4, po.getCompany());
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        //Perform the new purchase order insert query
        int poid = this.database.nonQueryWithIdCallback(qryStatement);
        //Iterate through each of the related purchase order lines and add as records
        for(MaverickPurchaseOrderLine line : po.getLines()){
            //Add this purchase order line to the database
            this.addPurchaseOrderLine(line, poid);
        }
    }

    /**
     * Add a new line to a purchase order and write to database
     * @param line purchase order line to add to the purchase order
     * @param poid id of the purchase order to add to
     */
    public void addPurchaseOrderLine(MaverickPurchaseOrderLine line, int poid){
        //Create a new po line record query
        String qryString = "INSERT INTO table_polines (poid, line, supplierpartnum, partdesc, deliverydate, quantity, price) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        try {
            //Perform new po line query
            qryStatement.setInt(1, poid);
            qryStatement.setInt(2, line.getLineNumber());
            qryStatement.setString(3, line.getSupplierPartNumber());
            qryStatement.setString(4, line.getPartDescription());
            qryStatement.setString(5, line.getDeliveryDate());
            qryStatement.setDouble(6, line.getQuantity());
            qryStatement.setDouble(7, line.getPrice());
            this.database.nonQuery(qryStatement);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    /**
     * Get the all of the lines from a defined purchase order
     * @param po the purchase order whose lines you prefer
     * @return List of puchase order line options from query
     */
    public List<MaverickPurchaseOrderLine> getPurchaseOrderLines(MaverickPurchaseOrder po){
        //Create the po line query
        String getPurchaseOrderLinesSql = "SELECT * FROM table_polines WHERE poid = ?";
        PreparedStatement getPurchaseOrderLines = database.prepareStatement(getPurchaseOrderLinesSql);
        List<MaverickPurchaseOrderLine> purchaseOrderLines = new ArrayList<>();
        try{
            //Prepare the statement, insert arguments
            getPurchaseOrderLines.setInt(1, po.getId());
            //Perform the query from database
            ResultSet purchaseOrderLineResults = database.query(getPurchaseOrderLines);
            //Loop through each of the result lines
            while(purchaseOrderLineResults.next()){
                //Parse each result line to instantiate a Maverick pallet item
                int poid = purchaseOrderLineResults.getInt("poid");
                int lineNo = purchaseOrderLineResults.getInt("line");
                String supplierPartNo = purchaseOrderLineResults.getString("supplierpartnum");
                String partDesc = purchaseOrderLineResults.getString("partdesc");
                Date deliveryDate = purchaseOrderLineResults.getDate("deliverydate");
                double quantity = purchaseOrderLineResults.getDouble("quantity");
                double price = purchaseOrderLineResults.getDouble("price");
                //Instantiate a Maverick pallet object using parased data from the query
                MaverickPurchaseOrderLine thisMpol = new MaverickPurchaseOrderLine(poid, lineNo, supplierPartNo, partDesc, deliveryDate.toString(), quantity, price);
                //Add the object to the list for return
                purchaseOrderLines.add(thisMpol);
            }
        } catch(SQLException sqlEx){
            //utried.png
            sqlEx.printStackTrace();
        }
        return purchaseOrderLines;
    }

    /**
     * Get a specific purchase order line from the database by po id and line number
     * @param po purchase order which contains the line to be retrieved
     * @param lineNumber purchase order line number to retrieve
     * @return the requested purchase order line
     */
    public MaverickPurchaseOrderLine getPurchaseOrderLines(MaverickPurchaseOrder po, int lineNumber){
        //Create the query
        String getPurchaseOrderLineSql = "SELECT * FROM table_polines WHERE poid = ? AND line = ?";
        PreparedStatement getPurchaseOrderLineStatement = database.prepareStatement(getPurchaseOrderLineSql);
        MaverickPurchaseOrderLine thisMpol;
        try{
            //Prepare the statement, set query arguments
            getPurchaseOrderLineStatement.setInt(1, po.getId());
            getPurchaseOrderLineStatement.setInt(2, lineNumber);
            //Perform the query on database
            ResultSet purchaseOrderLineResults = database.query(getPurchaseOrderLineStatement);
            if(purchaseOrderLineResults.next()){
                //Parse the result line of the query
                int poid = purchaseOrderLineResults.getInt("poid");
                int lineNo = purchaseOrderLineResults.getInt("line");
                String supplierPartNo = purchaseOrderLineResults.getString("supplierpartnum");
                String partDesc = purchaseOrderLineResults.getString("partdesc");
                Date deliveryDate = purchaseOrderLineResults.getDate("deliverydate");
                double quantity = purchaseOrderLineResults.getDouble("quantity");
                double price = purchaseOrderLineResults.getDouble("price");
                //Instantiate a purchase order line object using parsed line
                thisMpol = new MaverickPurchaseOrderLine(poid, lineNo, supplierPartNo, partDesc, deliveryDate.toString(), quantity, price);
            } else { thisMpol = null; }
        } catch(SQLException sqlEx){
            //:(
            thisMpol = null;
            sqlEx.printStackTrace();
        }
        return thisMpol;
    }

    /**
     * Write a pallet binding to the database provided with a po id, line number, and pallet
     * @param poid the id of the purchase order containing the line
     * @param poline this line number of the po line to bind to
     * @param pallet the pallet to bind to the specified po line
     */
    public void bindPallet(int poid, int poline, MaverickPallet pallet){
        //Create the insert query for the binding record
        String insertBindingRecordSql = "INSERT INTO po_pallet_mapping (mlot, poid, poline, bindtimestamp) VALUES (?, ?, ?, NOW())";
        PreparedStatement insertBindingRecordStatement = database.prepareStatement(insertBindingRecordSql);
        try{
            //Prepare the statement, set the arguments from method parameters
            insertBindingRecordStatement.setString(1, pallet.getPalletID());
            insertBindingRecordStatement.setInt(2, poid);
            insertBindingRecordStatement.setInt(3, poline);
            //Perform the insert query, hope for the best
            database.nonQuery(insertBindingRecordStatement);
        } catch(SQLException sqlEx){
            // sux 2 succc
            sqlEx.printStackTrace();
        }
    }
}

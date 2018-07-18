package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Date;

import maverick_data.DatabaseInteraction;
import maverick_types.MaverickItem;
import maverick_data.Config;

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
        this.database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
    }

    /**
     * addPurchaseOrder adds a purchase order to the database
     */
    public void addPurchaseOrder(MaverickPurchaseOrder po) {
        String qryString = "INSERT INTO table_po (cid, number, dateplaced, placingcompany) " + "VALUES (\"" +
                po.getCustomer() + "\", \"" +=
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
                poid + "\", \"" +=
                line.getLineNumber() + "\", \"" +
                line.getSupplierPartNumber() + "\", \"" +
                line.getPartDescription() + "\", \"" +
                line.getDeliveryDate() + "\", \"" +
                line.getQuantity() + "\", \"" +
                line.getPrice() + "\")";

        PreparedStatement qryStatement = this.database.prepareStatement(qryString);
        this.database.nonQuery(qryStatement);
    }

}

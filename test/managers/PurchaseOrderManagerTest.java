package managers;

import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import org.junit.Test;


import java.util.Date;

public class PurchaseOrderManagerTest {

    @Test
    public void addPurchaseOrderTest() {
<<<<<<< HEAD
        MaverickPurchaseOrder testOrder = new MaverickPurchaseOrder("S23143DS334", new Date(), "Symplex Solutions LLC", "C-1");
        testOrder.addLine(new MaverickPurchaseOrderLine(1, "ADSN123j21jnDAS", 15, "A really cool device", new Date(), 1.52));
        testOrder.addLine(new MaverickPurchaseOrderLine(2, "SJ1240923490123", 8000, "A plastic tube", new Date(), 0.2));
        testOrder.addLine(new MaverickPurchaseOrderLine(3, "a123ads032190as", 0.5, "A medical device", new Date(), 1250.49));
=======
        MaverickPurchaseOrder testOrder = new MaverickPurchaseOrder("S23143DS334", "2018-07-20 05:30:00", "Symplex Solutions LLC", "C-1");
        testOrder.addLine(new MaverickPurchaseOrderLine(1, "ADSN123j21jnDAS", 15, "A really cool device", "2018-08-20", 1.52));
        testOrder.addLine(new MaverickPurchaseOrderLine(2, "SJ1240923490123", 8000, "A plastic tube", "2018-08-15", 0.2));
        testOrder.addLine(new MaverickPurchaseOrderLine(3, "a123ads032190as", 0.5, "A medical device", "2018-08-02", 1250.49));
>>>>>>> 25fe3896d7458e90bde8e873042616fc744d7ddd
        PurchaseOrderDataManager orderManager = new PurchaseOrderDataManager();
        orderManager.addPurchaseOrder(testOrder);
    }

}
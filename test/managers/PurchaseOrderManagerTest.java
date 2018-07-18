package managers;

import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import org.junit.Test;


import java.util.Date;

public class PurchaseOrderManagerTest {

    @Test
    public void addPurchaseOrderTest() {
        MaverickPurchaseOrder testOrder = new MaverickPurchaseOrder("S23143DS334", new Date(), "Symplex Solutions LLC", "C-1");
        testOrder.addLine(new MaverickPurchaseOrderLine(1, "ADSN123j21jnDAS", 15, "A really cool device", new Date(), 1.52));
        testOrder.addLine(new MaverickPurchaseOrderLine(2, "SJ1240923490123", 8000, "A plastic tube", new Date(), 0.2));
        testOrder.addLine(new MaverickPurchaseOrderLine(3, "a123ads032190as", 0.5, "A medical device", new Date(), 1250.49));
        PurchaseOrderDataManager orderManager = new PurchaseOrderDataManager();
        orderManager.addPurchaseOrder(testOrder);
    }

}
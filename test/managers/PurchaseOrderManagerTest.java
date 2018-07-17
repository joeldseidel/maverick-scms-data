package managers;

import maverick_types.MaverickPurchaseOrder;
import maverick_types.MaverickPurchaseOrderLine;
import org.junit.Test;

import static org.junit.Assert.*;

public class PurchaseOrderDataManagerTest {

    @Test
    public void addPurchaseOrderTest() {
        MaverickPurchaseOrder testOrder = new MaverickPurchaseOrder("S23143DS334", Date.now(), "Symplex Solutions LLC", "C-1");
        testOrder.addLine(new MaverickPurchaseOrderLine(1, "ADSN123j21jnDAS", 15, "A really cool device", Date.now(), 1.52));
        testOrder.addLine(new MaverickPurchaseOrderLine(2, "SJ1240923490123", 8000, "A plastic tube", Date.now(), 0.2));
        testOrder.addLine(new MaverickPurchaseOrderLine(3, "a123ads032190as", 0.5, "A medical device", Date.now(), 1250.49));
        PurchaseOrderDataManager orderManager = new PurchaseOrderDataManager();
        orderManager.addPurchaseOrder(testOrder);
    }

}
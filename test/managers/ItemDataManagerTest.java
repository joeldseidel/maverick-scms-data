package managers;

import maverick_types.MaverickItem;
import org.junit.Test;

import static org.junit.Assert.*;

public class ItemDataManagerTest {

    @Test
    public void addItemTest() {
        MaverickItem testItem = new MaverickItem(123, "Medical Item", "Devices", "C-1");
        ItemDataManager itemDataManager = new ItemDataManager();
        itemDataManager.addItem(testItem);
    }

}
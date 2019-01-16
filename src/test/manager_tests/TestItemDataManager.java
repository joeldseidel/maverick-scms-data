package manager_tests;

import com.joelseidel.java_datatable.DataTable;
import managers.ItemDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.MaverickItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class TestItemDataManager {
    private ItemDataManager itemDataManager = new ItemDataManager();

    private static final String testMlot = "1";

    @BeforeClass
    public static void initTest(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        //Create item record for edit name and category test
        database.nonQuery(database.prepareStatement("INSERT INTO table_items (mid, fdaid, name, category, cid) VALUES ('1', '2', 'itemname', 'category', 'C-0')"));
        //Create item record for remove item test
        database.nonQuery(database.prepareStatement("INSERT INTO table_items (mid, fdaid, name, category, cid) VALUES ('2', '3', 'itemname', 'category', 'C-0')"));
    }

    private static final MaverickItem testMItem = new MaverickItem("000000000", "0000000000000", "itemname", "itemcat", "C-0");

    @Test
    public void testAddItem(){
        //Test add item
        System.out.println("\nTesting add item");
        assertTrue(itemDataManager.addItem(testMItem));
    }

    private static final String mlot = "94180632";
    private static final String testFdaId = "e124c470-a338-4033-b23c-48863b11367f";

    @Test
    public void testGetItem(){
        //Test get item
        System.out.println("\nTesting get item");
        MaverickItem testMItem = itemDataManager.getItem(mlot);
        assertNotNull(testMItem);
        assertEquals(mlot, testMItem.getMaverickID());
        assertEquals(testFdaId, testMItem.getFdaID());
    }

    @Test
    public void testGenerateLotNumber(){
        System.out.println("\nTesting generate lot number");
        long generatedLotNumber = ItemDataManager.generateItemLotNumber();
        String generatedLotNumberString = Long.toString(generatedLotNumber);
        assertEquals(8, generatedLotNumberString.length());
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        PreparedStatement isUniqueLotStmt = database.prepareStatement("SELECT mid FROM table_items WHERE mid = ?");
        try {
            isUniqueLotStmt.setString(1, generatedLotNumberString);
            DataTable isUniqueLotResult = new DataTable(database.query(isUniqueLotStmt));
            assertEquals(0, isUniqueLotResult.getRowCount());
        } catch (SQLException sqlEx) {
            System.out.println("Get is generated lot number unique failed: " + sqlEx.getMessage());
        }
    }

    @Test
    public void testItemExists(){
        System.out.println("\nTesting item exists");
        assertTrue(itemDataManager.itemExists(mlot));
        assertFalse(itemDataManager.itemExists("14232524524524"));
    }

    @Test
    public void testGetItemCid(){
        System.out.println("\nTesting get item cid");
        assertEquals("C-1", itemDataManager.getItemCID(mlot));
    }

    private static final String testNewName = "testnamesuccess";

    @Test
    public void testEditName(){
        System.out.println("\nTesting edit item name");
        try{
            itemDataManager.editName(testMlot, testNewName);
        } catch (Exception ex) {
            fail("Exception raised on editing name: " + ex.getMessage());
        }
    }

    private static final String testNewCategory = "testnewcategory";

    @Test
    public void testEditCategory(){
        System.out.println("\nTesting edit item category");
        try {
            itemDataManager.editCategory(testMlot, testNewCategory);
        } catch (Exception ex) {
            fail("Exception raised on editing category: " + ex.getMessage());
        }
    }

    private static String testRemoveMLot = "2";

    @Test
    public void testRemoveItem(){
        System.out.println("\nTesting remove item");
        try {
            itemDataManager.removeItem(testRemoveMLot);
        } catch (Exception ex) {
            fail("Exception raised on removing item: " + ex.getMessage());
        }
        assertFalse(itemDataManager.itemExists(testRemoveMLot));
    }

    @AfterClass
    public static void cleanUpTests(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        database.nonQuery(database.prepareStatement("DELETE FROM table_items WHERE cid = 'C-0'"));
    }
}

import com.joelseidel.java_datatable.DataTable;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class TestDatabaseInteraction {
    @Test
    public void testDatabaseConnection(){
        DatabaseInteraction database;
        //Test database connection creation
        System.out.println("Test create database connection");
        database = new DatabaseInteraction(DatabaseType.AppData);
        assertNotNull(database);
        assertTrue(database.closeConnection());
        database = new DatabaseInteraction(DatabaseType.Devices);
        assertNotNull(database);
        //Test can close connection success
        assertTrue(database.closeConnection());
    }

    @Test
    public void testPrepareStatement(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.Devices);
        try {
            //Test database prepared statement
            System.out.println("\nTesting prepare statement");
            String testPreparedStatementSql = "SELECT * FROM devices WHERE fda_id = ?";
            PreparedStatement testPreparedStatement = database.prepareStatement(testPreparedStatementSql);
            //Prepared statement will be null if error caught
            assertNotNull(testPreparedStatement);
            testPreparedStatement.setString(1, "1234567890");
        } catch(SQLException sqlEx){
            //If an error occurs here, this is an issue
            fail("SQLException occurs on prepare statement");
        }
        assertTrue(database.closeConnection());
    }

    @Test
    public void testNonQuery(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        //Test successful database query
        System.out.println("\nTesting successful nonquery");
        String testSuccessfulDbQuery = "INSERT INTO table_items (mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement testSuccessfulDbQueryStmt = database.prepareStatement(testSuccessfulDbQuery);
        try {
            testSuccessfulDbQueryStmt.setString(1, "87654321");
            testSuccessfulDbQueryStmt.setString(2, "12345678");
            testSuccessfulDbQueryStmt.setString(3, "testitem");
            testSuccessfulDbQueryStmt.setString(4, "test");
            testSuccessfulDbQueryStmt.setString(5, "C-0");
            assertTrue(database.nonQuery(testSuccessfulDbQueryStmt));
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on successful test query: " + sqlEx.getMessage());
        }
        //Test unsuccessful database query
        System.out.println("\nTesting unsuccessful nonquery");
        String testUnsuccessfulDbQuery = "INSERT INTO table_testnoexist (mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement testUnsuccessfulDbStmt = database.prepareStatement(testUnsuccessfulDbQuery);
        try {
            testUnsuccessfulDbStmt.setString(1, "00000000");
            testUnsuccessfulDbStmt.setString(2, "00000000");
            testUnsuccessfulDbStmt.setString(3, "testitem");
            testUnsuccessfulDbStmt.setString(4, "test");
            testUnsuccessfulDbStmt.setString(5, "C-0");
            assertFalse(database.nonQuery(testUnsuccessfulDbStmt));
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on unsuccessful test query: " + sqlEx.getMessage());
        }
        assertTrue(database.closeConnection());
    }

    @Test
    public void testQuery(){
        System.out.println("\nTesting successful query");
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String testSuccessfulQuery = "SELECT * FROM table_items WHERE mid = ?";
        PreparedStatement testSuccessfulStmt = database.prepareStatement(testSuccessfulQuery);
        try {
            testSuccessfulStmt.setString(1, "94180632");
            DataTable successfulResult = new DataTable(database.query(testSuccessfulStmt));
            assertNotNull(successfulResult);
            assertEquals(1, successfulResult.getRowCount());
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on successful test query: " + sqlEx.getMessage());
        }
        System.out.println("\nTesting unsuccessful query");
        String testUnsuccessfulQuery = "SELECT * FROM table_testnoexist WHERE fda_id = ?";
        PreparedStatement testUnsuccessfulStmt = database.prepareStatement(testUnsuccessfulQuery);
        try {
            testUnsuccessfulStmt.setString(1, "00000000");
            assertNull(database.query(testUnsuccessfulStmt));
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on unsuccessful test query: " + sqlEx.getMessage());
        }
        assertTrue(database.closeConnection());
    }

    @Test
    public void testBatchNonQuery(){
        System.out.println("\nTesting successful batch creation");
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        database.setAutoCommit(false);
        String testSuccessfulBatchSql = "INSERT INTO table_items (mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement testSuccessfulBatchStmt = database.prepareStatement(testSuccessfulBatchSql);
        try {
            for(int i = 0; i < 5; i++) {
                testSuccessfulBatchStmt.setString(1, Integer.toString(i));
                testSuccessfulBatchStmt.setString(2, Integer.toString(i));
                testSuccessfulBatchStmt.setString(3, "testitem");
                testSuccessfulBatchStmt.setString(4, "test");
                testSuccessfulBatchStmt.setString(5, "C-0");
                testSuccessfulBatchStmt.addBatch();
            }
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on successful batch creation: " + sqlEx.getMessage());
        }
        assertTrue(database.batchNonQuery(testSuccessfulBatchStmt));
        database.commitBatches();
        database.setAutoCommit(true);
        System.out.println("\nTesting unsuccessful batch creation");
        database.setAutoCommit(false);
        String testUnsuccessfulBatchSql = "INSERT INTO table_testnoexist (mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement testUnsuccessfulBatchStmt = database.prepareStatement(testUnsuccessfulBatchSql);
        try {
            for(int i = 0; i < 5; i++){
                testUnsuccessfulBatchStmt.setString(1, Integer.toString(i));
                testUnsuccessfulBatchStmt.setString(2, Integer.toString(i));
                testUnsuccessfulBatchStmt.setString(3, "testitem");
                testUnsuccessfulBatchStmt.setString(4, "test");
                testUnsuccessfulBatchStmt.setString(5, "C-0");
                testUnsuccessfulBatchStmt.addBatch();
            }
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on unsuccessful batch creation: " + sqlEx.getMessage());
        }
        assertFalse(database.batchNonQuery(testUnsuccessfulBatchStmt));
        database.commitBatches();
        database.setAutoCommit(true);
    }

    @AfterClass
    public static void testCleanUp(){
        System.out.println("Cleaning up database interaction tests");
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        //Remove the successful database query
        String removeTestSuccessfulDbQuery = "DELETE FROM table_items WHERE name = ? AND category = ?";
        PreparedStatement removeTestSuccessfulDbQueryStmt = database.prepareStatement(removeTestSuccessfulDbQuery);
        try {
            removeTestSuccessfulDbQueryStmt.setString(1, "testitem");
            removeTestSuccessfulDbQueryStmt.setString(2, "test");
            database.nonQuery(removeTestSuccessfulDbQueryStmt);
        } catch(SQLException sqlEx){
            System.out.println("Could not clean up test: " + sqlEx.getMessage());
        }
    }
}

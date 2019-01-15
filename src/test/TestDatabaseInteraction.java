import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class TestDatabaseInteraction {
    @Test
    public void testDatabaseConnection(){
        DatabaseInteraction database;
        //Test database connection creation
        database = new DatabaseInteraction(DatabaseType.AppData);
        assertNotNull(database);
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
            String testPreparedStatementSql = "SELECT * FROM devices WHERE fda_id = ?";
            PreparedStatement testPreparedStatement = database.prepareStatement(testPreparedStatementSql);
            //Prepared statement will be null if error caught
            assertNotNull(testPreparedStatement);
            testPreparedStatement.setString(0, "1234567890");
        } catch(SQLException sqlEx){
            //If an error occurs here, this is an issue
            fail("SQLException occurs on prepare statement");
        }
    }

    @Test
    public void testNonQuery(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        //Test successful database query
        String testSuccessfulDbQuery = "INSERT INTO table_items (mid, fdaid, name, category, cid) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement testSuccessfulDbQueryStmt = database.prepareStatement(testSuccessfulDbQuery);
        try {
            testSuccessfulDbQueryStmt.setString(1, "00000000");
            testSuccessfulDbQueryStmt.setString(2, "00000000");
            testSuccessfulDbQueryStmt.setString(3, "test");
            testSuccessfulDbQueryStmt.setString(4, "test");
            testSuccessfulDbQueryStmt.setString(5, "C-0");
            assertTrue(database.nonQuery(testSuccessfulDbQueryStmt));
        } catch (SQLException sqlEx) {
            fail("SQLException occurs on successful test query");
        }
    }
}

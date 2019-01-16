package manager_tests;

import managers.DeviceMovementEventManager;
import managers.ItemDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.*;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class TestDeviceMovementEventManager {
    private DeviceMovementEventManager deviceMovementEventManager = new DeviceMovementEventManager();

    private static final String mlot = "94180632";
    private static final MovementStatus deviceMovementStatus = MovementStatus.InStorage;

    @Test
    public void testGetCurrentStatus(){
        System.out.println("\nTesting get current status");
        ItemDataManager itemDataManager = new ItemDataManager();
        MaverickItem mItem = itemDataManager.getItem(mlot);
        assertNotNull(mItem);
        MovementStatus currentStatus = deviceMovementEventManager.getCurrentStatus(mItem);
        assertNotNull(currentStatus);
        assertEquals(deviceMovementStatus, currentStatus);
    }

    private static final String testmLot = "09876543211";
    private static final String testCid = "C-0";
    private static final MovementType movementType = MovementType.CycleIn;

    @Test
    public void testCommitMovement(){
        System.out.println("\nTesting commmit movement");
        DeviceMovementEvent deviceMovementEvent = new DeviceMovementEvent(testmLot, testCid, movementType);
        assertTrue(deviceMovementEventManager.commitMovement(deviceMovementEvent));
    }

    @Test
    public void testGetMovements(){
        ItemDataManager itemDataManager = new ItemDataManager();
        MaverickItem mItem = itemDataManager.getItem(mlot);
        assertNotNull(mItem);
        List<DeviceMovementEvent> deviceMovementEvents = deviceMovementEventManager.getMovements(mItem);
        assertNotNull(deviceMovementEvents);
        assertEquals(6, deviceMovementEvents.size());
        assertEquals(mlot, deviceMovementEvents.get(0).getItemId());
    }

    @AfterClass
    public static void cleanUpTests(){
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        String removeTestCommit = "DELETE FROM device_movements WHERE cid = ?";
        PreparedStatement removeTestCommitStmt = database.prepareStatement(removeTestCommit);
        try {
            removeTestCommitStmt.setString(1, testCid);
            database.nonQuery(removeTestCommitStmt);
        } catch(SQLException sqlEx) {
            fail("Could not clean up device movement event manager: " + sqlEx.getMessage());
        }
    }
}

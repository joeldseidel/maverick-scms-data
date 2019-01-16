import manager_tests.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestDeviceDataManager.class,
        TestDeviceMovementEventManager.class,
        TestItemDataManager.class
})

public class ManagerTestSuite { }
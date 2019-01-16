import manager_tests.TestDeviceDataManager;
import manager_tests.TestDeviceMovementEventManager;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestDeviceDataManager.class,
        TestDeviceMovementEventManager.class
})

public class ManagerTestSuite { }
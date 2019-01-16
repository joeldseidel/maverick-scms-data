import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
    public static void main(String args[]){
        System.out.println("----------DatabaseInteraction----------\n\n");
        Result result = JUnitCore.runClasses(TestDatabaseInteraction.class);
        displayTestResults(result);
        System.out.println("----------------Managers---------------\n\n");
        result = JUnitCore.runClasses(ManagerTestSuite.class);
        displayTestResults(result);
    }

    private static void displayTestResults(Result result){
        System.out.println("\n\nFailures:");
        for(Failure failure : result.getFailures()){
            System.out.println(failure.toString());
        }
        System.out.println("\n\n\n-------------------Result----------------------------\n\n\nAll tests passed: " +result.wasSuccessful() + "\n\n\n\n");
    }
}

package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;

import java.util.Date;

/**
 * Prototype for manager objects to manage database connection open and closes
 * @author Joel Seidel
 */
public abstract class ManagerPrototype {
    protected DatabaseInteraction database;
    private Date invokedManagerTime;

    /**
     * Create a new database interaction instance
     * @param dbType database to connect to
     */
    void initDb(DatabaseType dbType){
        database = new DatabaseInteraction(dbType);
        invokedManagerTime = new Date();
    }

    /**
     * Overridden finalize method to close the database connection
     * It is here we pray for a speedy garbage collection process
     */
    @Override
    protected void finalize(){
        //Try to finalize the super
        try {
            //Invoke the super finalize method
            super.finalize();
        } catch (Throwable ex) {
            //Could not finalize the super for some reason?
            //This shouldn't happen because the object super is irrelevant
            ex.printStackTrace();
        }
        long dbConnTime = new Date().getTime() - invokedManagerTime.getTime();
        System.out.println("Closed database connection on manager finalization. Connection was open for " + Long.toString(dbConnTime));
        //Close the database connection
        database.closeConnection();
        /*        _____________
         *      /               \
         *      |     RIP       |
         *      |  This Manager |
         *      |   2019-2019   |
         *      |               |
         *      |   Beloved DB  |
         *      |  Interaction, |
         *      |   caring dad  |
         *      |               |
         * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ */
    }
}

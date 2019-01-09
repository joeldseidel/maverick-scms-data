package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;

/**
 * Prototype for manager objects to manage database connection open and closes
 * @author Joel Seidel
 */
public abstract class ManagerPrototype {
    protected DatabaseInteraction database;

    /**
     * Create a new database interaction instance
     * @param dbType database to connect to
     */
    void initDb(DatabaseType dbType){
        database = new DatabaseInteraction(dbType);
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

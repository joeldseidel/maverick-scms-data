package maverick_data;

import maverick_types.DatabaseType;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * A class for interacting with a MySQL database
 *
 * @author Joel Seidel
 */
public class DatabaseInteraction {

    /**
     * The connection to use when running queries
     */
    private Connection dbConn;

    /**
     * Create a new DatabaseInteraction object by fetching the database connection properties and creating a connection
     * @param databaseType the database type which needs to have its specific name fetched from the properties
     */
    public DatabaseInteraction(DatabaseType databaseType) {
        Properties properties = new Properties();
        try{
            InputStream input = getClass().getResourceAsStream("database.properties");
            properties.load(input);
            String host = properties.getProperty("host");
            int port = Integer.parseInt(properties.getProperty("port"));
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            String database;
            if(databaseType == DatabaseType.AppData){
                database = properties.getProperty("maverick.dbName");
            } else {
                database = properties.getProperty("devices.dbName");
            }
            this.dbConn = this.createConnection(host, port, username, password, database);
        } catch(IOException ioEx){
            //Just loading the properties file no big deal
            ioEx.printStackTrace();
        }
    }

    /**
     * createConnection establishes a connection to a MySQL DB with the given parameters
     *
     * @param host the host to connect to
     * @param port the port to connect on
     * @param username the username to use
     * @param password the password to use
     * @return the created Connection object
     */
    private Connection createConnection(String host, int port, String username, String password, String database){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            // build host string
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            // debug attempted connection
            System.out.println("Attempting connection to " + url);
            // create connection
            return DriverManager.getConnection(url, username, password);
        }
        catch(ClassNotFoundException cnfE){
            // Mysql driver is not present on the server (this shouldn't happen because it will be installed
            cnfE.printStackTrace();
        }
        catch(SQLException sqlE) {
            // Could not create connection
            System.out.println("Data connection failed with exception : " + sqlE);
        }
        return null;
    }

    /**
     * closeConnection closes the current DB connection
     * @return success / fail boolean
     */
    public boolean closeConnection(){
        try{
            dbConn.close();
            return true;
        } catch(SQLException sqlE){
            System.out.println("Could not close data connection");
            return false;
        }
    }

    /**
     * prepareStatement prepares a String SQL statement for use with the connected DB
     * @param sql the unprepared statement to parse
     * @return the prepared statement
     */
    public PreparedStatement prepareStatement(String sql){
        PreparedStatement preparedStatement;
        try{
            preparedStatement = dbConn.prepareStatement(sql);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            preparedStatement = null;
        }
        return preparedStatement;
    }

    /**
     * query runs a PreparedStatement against the connected database
     * @param queryStatement the statement to execute
     * @return the results of the query
     */
    public ResultSet query(PreparedStatement queryStatement){
        try{
            return queryStatement.executeQuery();
        } catch(SQLException sqlException){
            return null;
        }
        catch(NullPointerException nex){
            System.out.println(nex.getMessage());
            return null;
        }
    }

    /**
     * nonQuery runs a non-query PreparedStatement against the connected database
     * @param nonQueryStatement the statement to execute
     * @return success / fail boolean
     */
    public boolean nonQuery(PreparedStatement nonQueryStatement){
        try{
            nonQueryStatement.executeUpdate();
            return true;
        } catch(SQLException sqlException){
            System.out.println(sqlException.getMessage());
            return false;
        }
    }

    //FIXME Remove this method
    public int nonQueryWithIdCallback(PreparedStatement nonQueryIdCallbackStatement){
        try{
            nonQueryIdCallbackStatement.executeUpdate();
            String getLastIDSql = "SELECT LAST_INSERT_ID() AS thisid";
            PreparedStatement getLastIDStatement = this.prepareStatement(getLastIDSql);
            ResultSet getLastIdResult = this.query(getLastIDStatement);
            getLastIdResult.next();
            return getLastIdResult.getInt("thisid");
        } catch(SQLException sqlException){
            System.out.println(sqlException.getMessage());
            return -1;
        }
    }

    /**
     * Run a batch non query using a prepared statement built as a batch
     * @param batchNonQueryStatement batch statement to run as batch
     * @return success / fail boolean
     */
    public boolean batchNonQuery(PreparedStatement batchNonQueryStatement){
        try{
            //Execute batch non query
            batchNonQueryStatement.executeBatch();
            return true;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            return false;
        }
    }

    /**
     * Commit performed batch to the database
     */
    public void commitBatches(){
        try{
            //Commit batch to database
            dbConn.commit();
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Toggle auto commit property of the database connection
     * This is typically used before and after creating a batch
     * Default value for auto commit is true
     * @param isAutoCommit desired auto commit state
     */
    public void setAutoCommit(boolean isAutoCommit) {
        try{
            //Change auto commit to desired state
            dbConn.setAutoCommit(isAutoCommit);
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }
}
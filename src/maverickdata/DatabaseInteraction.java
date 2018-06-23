package maverickdata;

import java.sql.*;

public class DatabaseInteraction {
    Connection dbConn = CreateConnection();
    private Connection CreateConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/device_udi", "joel", "password");
            return dbConn;
        }
        catch(ClassNotFoundException cnfE){
            //Mysql driver is not present on the server (this shouldn't happen because it will be installed
            System.out.println("Data driver not present");
        }
        catch(SQLException sqlE) {
            //Could not create connection
            System.out.println("Data connection failed");
        }
        return null;
    }
    public void CloseConnection(){
        try{
            dbConn.close();
        } catch(SQLException sqlE){
            System.out.println("Could not close data connection");
        }
    }
    public ResultSet Query(String sql){
        try{
            Statement thisQuery = dbConn.createStatement();
            return thisQuery.executeQuery(sql);
        } catch(SQLException sqlException){
            return null;
        }
        catch(NullPointerException nex){
            System.out.println(nex.getMessage());
            return null;
        }
    }
    public void NonQuery(String sql){
        try{
            Statement thisNonQuery = dbConn.createStatement();
            thisNonQuery.executeUpdate(sql);
        } catch(SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }
}
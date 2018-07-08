package maverick_data;

import java.sql.*;


public class DatabaseInteraction {

    Connection dbConn = createConnection();
    public Connection createConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection dbConn = DriverManager.getConnection("jdbc:mysql://staging-itemdb.mavericksystems.us:3306",
                    "MavAdmin", "Lt7e^PV%6vRi5l4BcSw\\t");
            return dbConn;
        }
        catch(ClassNotFoundException cnfE){
            //Mysql driver is not present on the server (this shouldn't happen because it will be installed
            cnfE.printStackTrace();
        }
        catch(SQLException sqlE) {
            //Could not create connection
            System.out.println("Data connection failed");
        }
        return null;
    }

    public void closeConnection(){
        try{
            dbConn.close();
        } catch(SQLException sqlE){
            System.out.println("Could not close data connection");
        }
    }

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

    public void nonQuery(PreparedStatement nonQueryStatement){
        try{
            nonQueryStatement.executeUpdate();
        } catch(SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

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

}
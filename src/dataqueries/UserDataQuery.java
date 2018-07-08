package dataqueries;

import maverick_data.DatabaseInteraction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDataQuery {

    public static int getUserCount(String username){
        DatabaseInteraction database = new DatabaseInteraction();
        database.createConnection();
        String isUserValidSql = "SELECT COUNT(*) AS userCount FROM users WHERE Username = ?";
        PreparedStatement isUserValidStatement = database.prepareStatement(isUserValidSql);
        int userCount = 0;
        try{
            isUserValidStatement.setString(1, username);
            ResultSet getUserResults = database.query(isUserValidStatement);
            while(getUserResults.next()){
                userCount++;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            userCount = -1;
        } finally {
            database.closeConnection();
        }
        return userCount;
    }

    public static long getUserUUID(String username){
        DatabaseInteraction database = new DatabaseInteraction();
        database.createConnection();
        String getUserUUIDSql = "SELECT UUID FROM user_data WHERE Username = ?";
        PreparedStatement getUUIDStatement = database.prepareStatement(getUserUUIDSql);
        long userUUID = 0;
        try{
            getUUIDStatement.setString(1, username);
            ResultSet UUIDResults = database.query(getUUIDStatement);
            UUIDResults.next();
            userUUID = UUIDResults.getLong("UUID");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
        return userUUID;
    }

}

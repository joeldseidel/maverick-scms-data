package managers;

import maverick_data.DatabaseInteraction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDataManager {

    public static int getUserCount(String username){
        String host = "staging-itemdb.mavericksystems.us";
        int port = 3306;
        String user = "MavAdmin";
        String pass = "CurrentPass";
        DatabaseInteraction database = new DatabaseInteraction(host, port, user, pass);
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
        String host = "staging-itemdb.mavericksystems.us";
        int port = 3306;
        String user = "MavAdmin";
        String pass = "CurrentPass";
        DatabaseInteraction database = new DatabaseInteraction(host, port, user, pass);
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

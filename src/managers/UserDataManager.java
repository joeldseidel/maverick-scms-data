package managers;

import maverick_data.DatabaseInteraction;
import maverick_data.Config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDataManager {

    public static int getUserCount(String username){
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
        String isUserValidSql = "SELECT COUNT(*) AS userCount FROM table_users WHERE username = ?";
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
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
        String getUserUUIDSql = "SELECT uid FROM table_users WHERE username = ?";
        PreparedStatement getUUIDStatement = database.prepareStatement(getUserUUIDSql);
        long userUUID = 0;
        try{
            getUUIDStatement.setString(1, username);
            ResultSet UUIDResults = database.query(getUUIDStatement);
            UUIDResults.next();
            userUUID = UUIDResults.getLong("uid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
        return userUUID;
    }

}

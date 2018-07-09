package managers;

import maverick_data.DatabaseInteraction;
import maverick_data.Config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class UserDataManager {

    private static MessageDigest messageDigestSHA;

    public static int getUserCount(String username){
        System.out.println("Attempting to get user count for username : " + username);
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
        String isUserValidSql = "SELECT uid FROM table_users WHERE username = ?";
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
        System.out.println("Got User Count : " + userCount);
        return userCount;
    }

    public static boolean checkPasswordMatch(long uid, String password){
        System.out.println("Checking password authentication");
        boolean isMatch;
        try{
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass);
        String isPasswordMatchSql = "SELECT password FROM table_users WHERE uid = ?";
        PreparedStatement matchPasswordStatement = database.prepareStatement(isPasswordMatchSql);
        try{
            matchPasswordStatement.setString(1, ""+uid);
            ResultSet matchPasswordResults = database.query(matchPasswordStatement);
            matchPasswordResults.next();
            String gotPassword = matchPasswordResults.getString("password");
            String thisPassword = new String(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8)));
            isMatch = (gotPassword.equals(thisPassword));
            System.out.println("Got Password Match : " + isMatch);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            isMatch = false;
        } finally {
            database.closeConnection();
        }
        return isMatch;
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
        System.out.println("Got User UUID : " + userUUID);
        return userUUID;
    }

}

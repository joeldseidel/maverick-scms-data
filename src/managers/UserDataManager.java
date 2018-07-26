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
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
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

    public static boolean isUniqueUsername(String username){
        return getUserCount(username) == 1;
    }

    public static boolean checkPasswordMatch(long uid, String password){
        boolean isMatch;
        try{
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String isPasswordMatchSql = "SELECT password FROM table_users WHERE uid = ?";
        PreparedStatement matchPasswordStatement = database.prepareStatement(isPasswordMatchSql);
        try{
            matchPasswordStatement.setString(1, ""+uid);
            ResultSet matchPasswordResults = database.query(matchPasswordStatement);
            matchPasswordResults.next();
            String gotPassword = matchPasswordResults.getString("password").toLowerCase();
            System.out.println("Got Password " + gotPassword);
            String thisPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
            System.out.println("This Password " + thisPassword);
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
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
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

    public static String getUserCID(int uid){
        String cid;
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String getUserCIDSql = "SELECT cid FROM table_users WHERE uid = ?";
        PreparedStatement getUserCIDStatement = database.prepareStatement(getUserCIDSql);
        try{
            getUserCIDStatement.setString(1, uid);
            ResultSet CIDResults = database.query(getUserCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
        System.out.println("Got User CID : " + cid);
        return cid;
    }

    //Method to take byte array returned from hashing, and turn it into one string to compare with one from database
    private static String byteArrayToString(byte[] hash){
        StringBuffer outString = new StringBuffer();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) outString.append('0');
            outString.append(hex);
        }

        return outString.toString();
    }

    /**
     * addUser adds a user to the database
     */
    public static void addUser(String cid, String username, String password) {
        try{
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        String hashedPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String qryString = "INSERT INTO table_users (cid, username, password) VALUES (?, ?, ?)";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, cid);
            qryStatement.setString(2, username);
            qryStatement.setString(3, hashedPassword);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * removeUser removes a user from the database
     */
    public static void removeUser(int uid) {
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String qryString = "DELETE FROM table_users WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * editUsername changes a user's username
     */
    public static void editUsername(int uid, String username) {
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String qryString = "UPDATE table_users SET username = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, username);
            qryStatement.setString(2, uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * editPassword changes a user's password
     */
    public static void editPassword(int uid, String password) {
        try{
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        String hashedPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String qryString = "UPDATE table_users SET password = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, hashedPassword);
            qryStatement.setString(2, uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    /**
     * editRank changes a user's rank
     */
    public static void editRank(int uid, String rank) {
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        String qryString = "UPDATE table_users SET rank = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            qryStatement.setString(1, rank);
            qryStatement.setString(2, uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

}

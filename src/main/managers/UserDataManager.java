package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Data manager for user data
 * @author Joel Seidel
 */
public class UserDataManager extends ManagerPrototype {

    private static MessageDigest messageDigestSHA;

    /**
     * Default constructor to initialize the database connection
     */
    public UserDataManager(DatabaseInteraction database) { this.database = database; }

    /**
     * Get the number of user accounts which relate to the specified username
     * @param username username to get count of
     * @return number of user account records matching the specified username
     */
    public int getUserCount(String username){
        System.out.println("Attempting to get user count for username : " + username);
        //Create select user count query
        String isUserValidSql = "SELECT uid FROM table_users WHERE username = ?";
        PreparedStatement isUserValidStatement = database.prepareStatement(isUserValidSql);
        int userCount = 0;
        try{
            isUserValidStatement.setString(1, username);
            //Perform select user count query
            ResultSet getUserResults = database.query(isUserValidStatement);
            while(getUserResults.next()){
                //Increment user account for a new row
                userCount++;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            //Error in query, return invalid count
            userCount = -1;
        }
        System.out.println("Got User Count : " + userCount);
        return userCount;
    }

    /**
     * Determines if specified username is unique
     * @param username username to determine uniqueness of
     * @return boolean if username is unique
     */
    public boolean isUniqueUsername(String username){
        return getUserCount(username) == 1;
    }

    /**
     * Determine if specified password matches user account
     * @param uid user id to determine password for
     * @param password password to match
     * @return if password matches user account on file
     */
    public boolean checkPasswordMatch(long uid, String password){
        boolean isMatch;
        try{
            //Get SHA-256 message digest
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        //Create get password query
        String isPasswordMatchSql = "SELECT password FROM table_users WHERE uid = ?";
        PreparedStatement matchPasswordStatement = database.prepareStatement(isPasswordMatchSql);
        try{
            //Perform get password query
            matchPasswordStatement.setString(1, ""+uid);
            ResultSet matchPasswordResults = database.query(matchPasswordStatement);
            matchPasswordResults.next();
            //Determine password matching
            String gotPassword = matchPasswordResults.getString("password").toLowerCase();
            System.out.println("Got Password " + gotPassword);
            String thisPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
            System.out.println("This Password " + thisPassword);
            isMatch = (gotPassword.equals(thisPassword));
            System.out.println("Got Password Match : " + isMatch);
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            isMatch = false;
        }
        return isMatch;
    }

    /**
     * Get user id from database by username
     * @param username username to get uid of
     * @return unique user id
     */
    public long getUserUUID(String username){
        //Create get user id by username query
        String getUserUUIDSql = "SELECT uid FROM table_users WHERE username = ?";
        PreparedStatement getUUIDStatement = database.prepareStatement(getUserUUIDSql);
        long userUUID = 0;
        try{
            //Perform get user id query
            getUUIDStatement.setString(1, username);
            ResultSet UUIDResults = database.query(getUUIDStatement);
            UUIDResults.next();
            //Get user id field from the result set
            userUUID = UUIDResults.getLong("uid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        System.out.println("Got User UUID : " + userUUID);
        return userUUID;
    }

    /**
     * Get user company id by user id
     * @param uid user id of user to retrieve
     * @return user company id
     */
    public static String getUserCID(int uid){
        String cid = "notfound";
        //Create database instance since this is a static class
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        //Create get user cid by user id
        String getUserCIDSql = "SELECT cid FROM table_users WHERE uid = ?";
        PreparedStatement getUserCIDStatement = database.prepareStatement(getUserCIDSql);
        try{
            //Perform get user cid query
            getUserCIDStatement.setString(1, ""+uid);
            ResultSet CIDResults = database.query(getUserCIDStatement);
            CIDResults.next();
            cid = CIDResults.getString("cid");
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        System.out.println("Got User CID : " + cid);
        return cid;
    }

    /**
     * Get result set containing user data by username
     * @param username username of the user to retrieve
     * @return result of the get user by username
     */
    public ResultSet getUserData(String username) {
        //FIXME this method returns a result set, for some reason. this is not a good thing
        //Create get user by username query
        String getUserDataSql = "SELECT * FROM table_users WHERE username = ?";
        PreparedStatement getUserDataStatement = database.prepareStatement(getUserDataSql);
        ResultSet getUserDataResults;
        try {
            //Perform get user by username query
            getUserDataStatement.setString(1, username);
            getUserDataResults = database.query(getUserDataStatement);
        }
        catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            getUserDataResults = null;
        }
     return getUserDataResults;
    }

    /**
     * Method to take byte array returned from hashing, and turn it into one string to compare with one from database
     * @param hash byte array returned from hashin
     * @return string to compare to database
     */
    private static String byteArrayToString(byte[] hash){
        StringBuffer outString = new StringBuffer();
        //Convert to hex string and append
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) outString.append('0');
            outString.append(hex);
        }

        return outString.toString();
    }

    /**
     * addUser adds a user to the database
     * @param cid company id of the user to add
     * @param username username of the user to add
     * @param password password of the user to add
     */
    public void addUser(String cid, String username, String password) {
        try{
            //Get SHA256 message digest
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        //Create hashed password
        String hashedPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
        //Create insert user query
        String qryString = "INSERT INTO table_users (cid, username, password) VALUES (?, ?, ?)";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            //Perform insert user query
            qryStatement.setString(1, cid);
            qryStatement.setString(2, username);
            qryStatement.setString(3, hashedPassword);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Remove user from database
     * @param uid user id to remove from database
     */
    public void removeUser(int uid) {
        //Create remove user query
        String qryString = "DELETE FROM table_users WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            //Perform remove user query
            qryStatement.setString(1, ""+uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Change a username within the database
     * @param uid user id to change username of
     * @param username new username
     */
    public void editUsername(int uid, String username) {
        //Create update username query
        String qryString = "UPDATE table_users SET username = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            //Perform update username query
            qryStatement.setString(1, username);
            qryStatement.setString(2, ""+uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Change a user password within the database
     * @param uid user id to change the password of
     * @param password new password
     */
    public void editPassword(int uid, String password) {
        try{
            //Get SHA-256 hash
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("Message Digest failed for : " + e);
        }
        //Get hashed password
        String hashedPassword = byteArrayToString(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8))).toLowerCase();
        //Create update user password query
        String qryString = "UPDATE table_users SET password = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            //Perform update user query
            qryStatement.setString(1, hashedPassword);
            qryStatement.setString(2, ""+uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Edit the rank of a user
     * @param uid user id to change the rank of
     * @param rank rank to change the user to
     */
    public void editRank(int uid, String rank) {
        //Create update user rank query
        String qryString = "UPDATE table_users SET rank = ? WHERE uid = ?";
        PreparedStatement qryStatement = database.prepareStatement(qryString);
        try{
            //Perform update user rank query
            qryStatement.setString(1, rank);
            qryStatement.setString(2, ""+uid);
            database.nonQuery(qryStatement);
        }catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

}

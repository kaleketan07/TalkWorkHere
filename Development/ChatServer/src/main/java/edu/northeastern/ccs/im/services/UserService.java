package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.db.IDBConnection;
import edu.northeastern.ccs.im.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used for performing all user related services, which include all DAO services for a
 * user. This class contains methods to add, update, delete and get users from the database.
 *
 * @author Kunal
 */
public class UserService implements UserDao {

    private Set<User> userSet = new HashSet<>();
    private IDBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils;
    private ResultSet result;
    private static UserService userServiceInstance;
    Properties userProperties = new Properties();

    // Columns for user_profile
    private static final String USER_NAME = "username";
    private static final String USER_PSWD = "user_password";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String LOGGED_IN = "logged_in";

    /**
     * Instantiates an user object for UserServices. This constructor will initialize
     * and establish the connection to the database for the user_profile table for each
     * user.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the sql exception
     */
    private UserService() throws ClassNotFoundException, SQLException, IOException {
        conn = new DBConnection();
        utils = new DBUtils();
        result = null;
        userProperties = conn.getQueryProperties();
    }

    public static UserService getInstance() throws SQLException, IOException, ClassNotFoundException {
        if (userServiceInstance == null)
            userServiceInstance = new UserService();
        return userServiceInstance;
    }

    /**
     * This functions adds all the available users in the database to a HashSet.
     *
     * @return The Set with all the users available in it
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public Set<User> getAllUsers() throws SQLException {
        final String GET_ALL_USERS = userProperties.getProperty("GET_ALL_USERS");
        pstmt = conn.getPreparedStatement(GET_ALL_USERS);
        result = pstmt.executeQuery();
        while (result.next()) {
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            String uName = result.getString(USER_NAME);
            String uPwd = result.getString(USER_PSWD);
            boolean loggedInStatus = result.getBoolean(LOGGED_IN);
            userSet.add(new User(fName, lName, uName, uPwd, loggedInStatus));
        }
        pstmt.close();
        return userSet;
    }


    /**
     * This function returns the user details of a particular user when given their username
     * and password.
     *
     * @param username of the User
     * @param password of the User
     * @return the User objects with all the details of the user
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public User getUserByUserNameAndPassword(String username, String password) throws SQLException {
        User user = null;
        final String GET_USER_USERNAME_PSWD =
        		userProperties.getProperty("GET_USER_USERNAME_PSWD");
        pstmt = conn.getPreparedStatement(GET_USER_USERNAME_PSWD);
        pstmt = utils.setPreparedStatementArgs(pstmt, username, password);
        result = pstmt.executeQuery();
        if (result.first()) {
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            boolean loggedIn = result.getBoolean(LOGGED_IN);
            user = new User(fName, lName, username, password, loggedIn);
        }
        pstmt.close();
        return user;
    }

    /**
     * Gets all the user details of the user, given the username
     *
     * @param username the String username of the user used for logging in
     * @return A new user object with all the required details initialized.
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public User getUserByUserName(String username) throws SQLException {
        User user = null;
        final String GET_USER_BY_USER_NAME = userProperties.getProperty("GET_USERS_BY_USERNAME");
        pstmt = conn.getPreparedStatement(GET_USER_BY_USER_NAME);
        pstmt = utils.setPreparedStatementArgs(pstmt, username);
        result = pstmt.executeQuery();
        if (result.first()) {
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            String uPwd = result.getString(USER_PSWD);
            boolean loggedIn = result.getBoolean(LOGGED_IN);
            user = new User(fName, lName, username, uPwd, loggedIn);
        }
        pstmt.close();
        return user;
    }

    /**
     * Creates a new user and inserts these user details in the database
     *
     * @param u is the User object with all the required fields initialized
     * @return True if the mysql query is successfully run and user is added to the database
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public boolean createUser(User u) throws SQLException {
        final String CREATE_USER = userProperties.getProperty("CREATE_USER");
        pstmt = conn.getPreparedStatement(CREATE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, u.getFirstName(), u.getLastName(),
                u.getUserName(), u.getUserPassword(), u.isLoggedIn());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * This method updates the user's profile attributes, assuming that the user has sent correct attribute name
     * This will check if the attributeName is "user_searchable" and will prepare a different statement
     * for such attribute. The assumption here is that the value corresponding to this attribute is either 1/True or
     * 0/False.
     * Other attributes assume Strings are passed.
     *
     * @param uname          the username
     * @param attributeName  the attribute to be updated
     * @param attributeValue the value of the attribute that is to be set
     * @return the boolean
     * @throws SQLException the sql exception
     */
    @Override
    public boolean updateUserAttributes(String uname, String attributeName, String attributeValue) throws SQLException {
        final String UPDATE_USER = "UPDATE user_profile SET " + attributeName + "  = ? WHERE username = ?";
        String trueOrFalse;
        pstmt = conn.getPreparedStatement(UPDATE_USER);
        if (attributeName.compareTo("user_searchable") == 0) {
            if (attributeValue.compareTo(Integer.toString(0)) == 0 || attributeValue.equalsIgnoreCase("false")) {
                trueOrFalse = "0";
                pstmt = utils.setPreparedStatementArgs(pstmt, trueOrFalse, uname);
            } else if (attributeValue.compareTo(Integer.toString(1)) == 0 || attributeValue.equalsIgnoreCase("true")) {
                trueOrFalse = "1";
                pstmt = utils.setPreparedStatementArgs(pstmt, trueOrFalse, uname);
            } else
                ChatLogger.error("Searchable values should be boolean (1/0 True/False)");
        } else {
            pstmt = utils.setPreparedStatementArgs(pstmt, attributeValue, uname);
        }
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }


    /**
     * Deletes the user details from the database.
     * NOTE: This basically means that the user is inactive and this function only sets is_deleted
     * column in the database of the user to true
     *
     * @param u The user object, that needs to be deleted
     * @return True, if the deletion operation was successful, false otherwise
     */
    @Override
    public boolean deleteUser(User u) throws SQLException {
        final String DELETE_USER =
        		userProperties.getProperty("DELETE_USER");
        pstmt = conn.getPreparedStatement(DELETE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, u.getUserName());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }


    /**
     * add a entry for a user following other user
     *
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was inserted successfully
     * @throws SQLException the sql exception
     */
    @Override
    public boolean followUser(User followee, User follower) throws SQLException {
        final String FOLLOW_USER =
        		userProperties.getProperty("FOLLOW_USER");
        pstmt = conn.getPreparedStatement(FOLLOW_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, followee.getUserName(), follower.getUserName());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * delete a entry for a user following other user
     *
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was deleted successfully
     * @throws SQLException the sql exception
     */
    @Override
    public boolean unfollowUser(User followee, User follower) throws SQLException {
        final String UNFOLLOW_USER =
        		userProperties.getProperty("UNFOLLOW_USER");
        pstmt = conn.getPreparedStatement(UNFOLLOW_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, followee.getUserName(), follower.getUserName());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Search users who have set their searchable attribute to True
     * This returns all the users whose usernames or first names start with
     * the given search string
     *
     * @param searchString the search string
     * @return the hash map containing the usernames mapped to the respective full names
     * @throws SQLException the sql exception
     */
    @Override
    public Map<String, String> searchUser(String searchString) throws SQLException {
        Map<String, String> resultUsers = new HashMap<>();
        final String SEARCH_USER = userProperties.getProperty("SEARCH_USER");                
        pstmt = conn.getPreparedStatement(SEARCH_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, searchString, searchString);
        result = pstmt.executeQuery();
        while (result.next()) {
            String username = result.getString(USER_NAME);
            String fullName = result.getString(FIRST_NAME) + " " + result.getString(LAST_NAME);
            resultUsers.put(username, fullName);
        }
        pstmt.close();
        return resultUsers;
    }

    /**
     * Returns a Map<String, String> which contains username  and fullname of all the followers of a given user
     *
     * @param followee user who is the followee
     * @return a Map<String, String> which contains username and fullname
     * @throws SQLException the sql exception
     */
    @Override
    public Map<String, String> getFollowers(User followee) throws SQLException {
        Map<String, String> resultUsers = new HashMap<>();
        final String GET_FOLLOWERS = userProperties.getProperty("GET_FOLLOWERS");                
        pstmt = conn.getPreparedStatement(GET_FOLLOWERS);
        pstmt = utils.setPreparedStatementArgs(pstmt, followee.getUserName());
        result = pstmt.executeQuery();
        while (result.next()) {
            String username = result.getString(USER_NAME);
            String fullName = result.getString(FIRST_NAME) + " " + result.getString(LAST_NAME);
            resultUsers.put(username, fullName);
        }
        pstmt.close();
        return resultUsers;
    }

    /**
     * Returns a Map<String, String> which contains username and fullname of all the followee of a given user
     *
     * @param follower user who is the follower
     * @return a Map<String, String> which contains username and fullname
     * @throws SQLException the sql exception
     */
    @Override
    public Map<String, String> getFollowees(User follower) throws SQLException {
        Map<String, String> resultUsers = new HashMap<>();
        final String GET_FOLLOWEES = userProperties.getProperty("GET_FOLLOWEES");
        pstmt = conn.getPreparedStatement(GET_FOLLOWEES);
        pstmt = utils.setPreparedStatementArgs(pstmt, follower.getUserName());
        result = pstmt.executeQuery();
        while (result.next()) {
            String username = result.getString(USER_NAME);
            String fullName = result.getString(FIRST_NAME) + " " + result.getString(LAST_NAME);
            resultUsers.put(username, fullName);
        }
        pstmt.close();
        return resultUsers;
    }


    /**
     * Returns a Map<String, String> which contains username and fullname of all the user who are
     * online from the list of followees of the given user
     *
     * @param follower user who is the follower
     * @return a Map<String, String> which contains username and fullname
     * @throws SQLException the sql exception
     */
    @Override
    public Map<String, String> getOnlineUsers(User follower) throws SQLException {
        Map<String, String> resultUsers = new HashMap<>();
        final String GET_ONLINE_USER =
        		userProperties.getProperty("GET_ONLINE_USER");
        pstmt = conn.getPreparedStatement(GET_ONLINE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, follower.getUserName());
        result = pstmt.executeQuery();
        while (result.next()) {
            String username = result.getString(USER_NAME);
            String fullName = result.getString(FIRST_NAME) + " " + result.getString(LAST_NAME);
            resultUsers.put(username, fullName);
        }
        pstmt.close();
        return resultUsers;
    }

}


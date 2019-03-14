package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.models.User;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used for performing all user related services, which include all DAO services for a
 * user. This class contains methods to add, update, delete and get users from the database.
 *
 * @author Kunal
 */
public class UserService implements UserDao {

    private Set<User> userSet = new HashSet<>();
    private DBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils;
    private ResultSet result;
    private static UserService userServiceInstance;

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
        final String GET_ALL_USERS = "SELECT * FROM user_profile";
        pstmt = conn.getPreparedStatement(GET_ALL_USERS);
        try {
            result = pstmt.executeQuery();
            while (result.next()) {
                String fName = result.getString(FIRST_NAME);
                String lName = result.getString(LAST_NAME);
                String uName = result.getString(USER_NAME);
                String uPwd = result.getString(USER_PSWD);
                boolean loggedInStatus = result.getBoolean(LOGGED_IN);
                userSet.add(new User(fName, lName, uName, uPwd, loggedInStatus));
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        pstmt.close();
        return userSet;
    }


    /**
     * This function returns the user details of a particular user when given their username
     * and password. For now, it throws a NullPointerException if the user does not exist.
     *
     * @param username of the User
     * @param password of the User
     * @return the User objects with all the details of the user
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public User getUserByUserNameAndPassword(String username, String password) throws SQLException {
        User user;
        final String GET_USER_USERNAME_PSWD =
                "SELECT * FROM user_profile WHERE username = ? AND user_password = ?";
        pstmt = conn.getPreparedStatement(GET_USER_USERNAME_PSWD);
        pstmt = utils.setPreparedStatementArgs(pstmt, username, password);
        try {
            result = pstmt.executeQuery();
            if (!result.first()) {
                throw new SQLException();
            }
            result.first();
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            boolean loggedIn = result.getBoolean(LOGGED_IN);
            user = new User(fName, lName, username, password, loggedIn);
        } catch (Exception e) {
            throw new SQLException();
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
        User user;
        final String GET_USER_BY_USER_NAME = "SELECT * FROM user_profile WHERE username = ?";
        pstmt = conn.getPreparedStatement(GET_USER_BY_USER_NAME);
        pstmt = utils.setPreparedStatementArgs(pstmt, username);
        result = pstmt.executeQuery();
        if (result.first()) {
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            String uPwd = result.getString(USER_PSWD);
            boolean loggedIn = result.getBoolean(LOGGED_IN);
            user = new User(fName, lName, username, uPwd, loggedIn);
        } else {
            user = null;
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
        final String CREATE_USER =
                "INSERT INTO user_profile (first_name, last_name, username, user_password, logged_in) VALUES (?,?,?,?,?)";
        pstmt = conn.getPreparedStatement(CREATE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, u.getFirstName(), u.getLastName(),
                u.getUserName(), u.getUserPassword(), u.isLoggedIn());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }


    /**
     * This function takes in a user object whose fields have new values, but the username
     * and the user_id should match of a previous old user. If it does not, it throws a SQLException
     * The function overwrites all other overwrite-able fields of the user.
     * The SQLException is thrown when a request is made to update the username and that username
     * is not found in the database OR when the new username matches another username in the database.
     * NOTE : We might not need this yet, will delete if this turns out to be useless in the further sprints
     *
     * @param u The user object with new values in the fields
     * @return True if the update was successful, false otherwise
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    @Override
    public boolean updateUser(User u) throws SQLException {
        User user = getUserByUserName(u.getUserName());
        final String UPDATE_USER = "UPDATE user_profile SET first_name = ?," +
                "last_name = ?, user_password = ?, logged_in = ? WHERE username = ? ";
        pstmt = conn.getPreparedStatement(UPDATE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, u.getFirstName(), u.getLastName(), u.getUserPassword(),
                u.isLoggedIn(), user.getUserName());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }

    /**
     * This method will only update the user's first name and last name (for this sprint).
     *
     * @param uname         the username
     * @param firstName     the first name of the user
     * @param lastName      the last name of the user
     * @return the boolean
     * @throws SQLException the sql exception
     */
    public boolean updateUserAttributes(String uname, String firstName, String lastName) throws SQLException{
        final String UPDATE_USER = "UPDATE user_profile SET first_name = ? , last_name = ? WHERE username = ?";
        pstmt = conn.getPreparedStatement(UPDATE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt,firstName,lastName,uname);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult>0;
    }


    /**
     * Deletes the user details from the database.
     * NOTE: For the time being, let the user get deleted from the database, till we decide to add another column
     * that will make the user "Inactive"
     *
     * @param u The user object, that needs to be deleted
     * @return True, if the deletion operation was successful, false otherwise
     */
    @Override
    public boolean deleteUser(User u) throws SQLException {
        final String DELETE_USER =
                "UPDATE user_profile SET user_deleted = 1 WHERE username = ?";
        pstmt = conn.getPreparedStatement(DELETE_USER);
        pstmt = utils.setPreparedStatementArgs(pstmt, u.getUserName());
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }
}

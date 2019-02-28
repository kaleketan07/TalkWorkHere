package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.models.User;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used for performing all user related services, which include all DAO services for a
 * user. This class contains methods to add, update, delete and get users from the database.
 */
public class UserServices implements UserDao {

    private User user;
    private Set<User> userSet = new HashSet<>();

    // DB Connection Parameters:
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://chat-server.cntkoqkaxigr.us-east-1.rds.amazonaws.com/prattle?autoReconnect=true&useSSL=false";
    private static final  String MYSQL_USER_NAME ="root_master";
    private static final String MYSQL_PWD = "password_master";
    private Connection conn;
    private PreparedStatement pstmt = null;

    private static final String CREATE_USER =
            "INSERT INTO user_profile (first_name, last_name, username, user_password) VALUES (?,?,?,?)";
    private static final String DELETE_USER =
            "DELETE FROM user_profile WHERE username = ?";
    private static final String GET_USER_BY_USER_NAME =
            "SELECT * FROM user_profile WHERE username = ?";
    private static final String GET_USER_BY_ID =
            "SELECT * FROM user_profile WHERE user_id = ?";
    private static final String GET_ALL_USERS =
            "SELECT * FROM user_profile";


    /**
     * Instantiates an user object for UserServices. This constructor will initialize
     * and establish the connection to the database for the user_profile table for each
     * user.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the sql exception
     */
    UserServices() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(URL,MYSQL_USER_NAME,MYSQL_PWD);
    }

    /**
     * Gets all the user details of the user, given the user ID.
     * @param userId the ID of the user
     * @return A new user object with all the required details initialized.
     * @throws SQLException
     */
    @Override
    public User getUser(int userId) throws SQLException{
        pstmt = conn.prepareStatement(GET_USER_BY_ID);
        pstmt.setInt(1,userId);
        try(ResultSet result = pstmt.executeQuery()) {
            result.first();
            String fName = result.getString("first_name");
            String lName = result.getString("last_name");
            String uPwd = result.getString("user_password");
            String uName = result.getString("username");
            user = new User(userId,fName,lName,uName,uPwd);
        }catch(Exception e){
            throw new SQLException(e);
        }
        return user;

    }

    /**
     * This functions adds all the available users in the database to a HashSet.
     *
     * @return The Set with all the users available in it
     * @throws SQLException
     */
    @Override
    public Set<User> getAllUsers() throws SQLException {
        pstmt = conn.prepareStatement(GET_ALL_USERS);
        try(ResultSet result = pstmt.executeQuery()) {
            while(result.next()) {
                int id = result.getInt("user_id");
                String fName = result.getString("first_name");
                String lName = result.getString("last_name");
                String uName = result.getString("username");
                String uPwd = result.getString("user_password");
                userSet.add(new User(id, fName, lName, uName, uPwd));
            }
        }catch(Exception e){
            throw new SQLException(e);
        }
        return userSet;
    }

    //Do we need this?
    @Override
    public User getUserByUserNameAndPassword(String username, String password) {
        return null;
    }

    /**
     * Gets all the user details of the user, given the username
     * @param username the String username of the user used for logging in
     * @return A new user object with all the required details initialized.
     * @throws SQLException
     */
    @Override
    public User getUserByUserName(String username) throws SQLException{
        pstmt = conn.prepareStatement(GET_USER_BY_USER_NAME);
        pstmt.setString(1,username);
        try(ResultSet result = pstmt.executeQuery()) {
            result.first();
            int id = result.getInt("user_id");
            String fName = result.getString("first_name");
            String lName = result.getString("last_name");
            String uPwd = result.getString("user_password");
            user = new User(id,fName,lName,username,uPwd);
        }catch(Exception e){
            throw new SQLException(e);
        }
        return user;
    }

    /**
     * Creates a new user and inserts these user details in the database
     * @param u is the User object with all the required fields initialized
     * @return True if the mysql query is successfully run and user is added to the database
     * @throws SQLException
     */
    @Override
    public boolean createUser(User u) throws SQLException {
        boolean status = false;
        pstmt = conn.prepareStatement(CREATE_USER);
        pstmt.setString(1, u.getFirstName());
        pstmt.setString(2, u.getLastName());
        pstmt.setString(3,u.getUserName());
        pstmt.setString(4,u.getUserPassword());
        int result = pstmt.executeUpdate();
        if(result>0)
            status = true;
        pstmt.close();
        return status;
    }

    /*
    Following function has been commented in the interface, see the interface comments for
    details:

    @Override
    public boolean insertUser(User u) {
        return false;
    }
    */


    /*
    Kunal Note: How do we plan to get the details that need to be updated by the user?
    Should this method contain those fields that need to be updated (With the updated values)
    Or do we plan to send multiple questions to the user?
     */
    @Override
    public boolean updateUser(User u) {
        return false;
    }


    /**
     *  Deletes the user details from the database.
     *  Kunal NOTE: For the time being, let the user get deleted from the database, till we decide to add another column
     *  that will make the user "Inactive"
     * @param u The user object, that needs to be deleted
     * @return True, if the deletion operation was successful, false otherwise
     */
    @Override
    public boolean deleteUser(User u) throws SQLException{
        boolean status = false;
        pstmt = conn.prepareStatement(DELETE_USER);
        pstmt.setString(1,u.getUserName());
        int result = pstmt.executeUpdate();
        if (result!=0)
            status = true;
        return status;
    }

}

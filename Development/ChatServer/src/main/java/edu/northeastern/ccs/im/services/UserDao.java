package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import edu.northeastern.ccs.im.models.User;

/**
 * Interface for User Data Access Object
 *
 * @author rahul
 */
public interface UserDao {

    /**
     * This functions adds all the available users in the database to a HashSet.
     *
     * @return The Set with all the users available in it
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    Set<User> getAllUsers() throws SQLException;

    /**
     * This function returns the user details of a particular user when given their username
     * and password.
     *
     * @param username of the User
     * @param password of the User
     * @return the User objects with all the details of the user
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    User getUserByUserNameAndPassword(String username, String password) throws SQLException;

    /**
     * Gets all the user details of the user, given the username
     *
     * @param username the String username of the user used for logging in
     * @return A new user object with all the required details initialized.
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    User getUserByUserName(String username) throws SQLException;

    /**
     * Creates a new user and inserts these user details in the database
     *
     * @param u is the User object with all the required fields initialized
     * @return True if the mysql query is successfully run and user is added to the database
     * @throws SQLException returns the vendor specific error code for a wrong sql query
     */
    boolean createUser(User u) throws SQLException;

    /**
     * This method updates the user's profile attributes, assuming that the user has sent correct attribute name
     * This will check if the attributeName is "user_searchable" and will prepare a different statement
     * for such attribute. The assumption here is that the value corresponding to this attribute is either 1/True or
     * 0/False.
     * Other attributes assume Strings are passed.
     *
     * @param username       the username
     * @param attributeName  the attribute to be updated
     * @param attributeValue the value of the attribute that is to be set
     * @return the boolean
     * @throws SQLException the sql exception
     */
    boolean updateUserAttributes(String username, String attributeName, String attributeValue) throws SQLException;

    /**
     * Deletes the user details from the database.
     * NOTE: This basically means that the user is inactive and this function only sets another the is_deleted
     * attribute of the user to true
     *
     * @param u The user object, that needs to be deleted
     * @return True, if the deletion operation was successful, false otherwise
     */
    boolean deleteUser(User u) throws SQLException;

    /**
     * add a entry for a user following other user
     *
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was inserted successfully
     * @throws SQLException the sql exception
     */
    boolean followUser(User followee, User follower) throws SQLException;

    /**
     * delete a entry for a user following other user
     *
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was deleted successfully
     * @throws SQLException the sql exception
     */
	boolean unfollowUser(User followee, User follower) throws SQLException;
	
	/**
     * Returns a string which contains username of all the followers of a given user
     * @param followee user who is the followee
     * @return String which contains username of all the followers
     * @throws SQLException  the sql exception
     */
	String getFollowers(User followee) throws SQLException;
	

    /**
     * Search users who have set their searchable attribute to True
     * This returns all the users whose usernames or first names start with
     * the given search string
     *
     * @param searchString the search string
     * @return the hash map containing the usernames mapped to the respective full names
     * @throws SQLException the sql exception
     */
    Map<String, String> searchUser(String searchString) throws SQLException;

    /**
     * Returns a string which contains username of all the followees of a given user
     * @param follower user who is the follower
     * @return String which contains username of all the followees
     * @throws SQLException  the sql exception
     */
  String getFollowees(User follower) throws SQLException;
	

}

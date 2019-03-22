package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
import java.util.Set;

import edu.northeastern.ccs.im.models.User;

/**
 * Interface for User Data Access Object
 *
 * @author rahul
 */
public interface UserDao {

    //Get all the users for the system
    Set<User> getAllUsers() throws SQLException;

    //Get a specific user based on username and password
    User getUserByUserNameAndPassword(String username, String password) throws SQLException;

    //Get specific user by username
    User getUserByUserName(String username) throws SQLException;

    //Create a User entry in database
    boolean createUser(User u) throws SQLException;

    // update a User details in database
    boolean updateUserAttributes(String username, String attributeName, String attributeValue) throws SQLException;

    // delete a User from database
    boolean deleteUser(User u) throws SQLException;

    /**
     * add a entry for a user following other user
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was inserted successfully
     * @throws SQLException  the sql exception
     */
	boolean followUser(User followee, User follower) throws SQLException;
	
	/**
     * delete a entry for a user following other user
     * @param followee user who is the followee
     * @param follower user who is the follower
     * @return true if the relation was deleted successfully
     * @throws SQLException  the sql exception
     */
	boolean unfollowUser(User followee, User follower) throws SQLException;
	
	/**
     * Returns a string which contains username of all the followers of a given user
     * @param followee user who is the followee
     * @return String which contains username of all the followers
     * @throws SQLException  the sql exception
     */
	String getFollower(User followee) throws SQLException;
	
	/**
     * Returns a string which contains username of all the followee of a given user
     * @param followee user who is the followee
     * @return String which contains username of all the followers
     * @throws SQLException  the sql exception
     */
	String getFollowee(User follower) throws SQLException;
}

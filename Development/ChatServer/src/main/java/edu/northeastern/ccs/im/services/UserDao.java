package edu.northeastern.ccs.im.services;

import java.util.Set;

import edu.northeastern.ccs.im.models.User;

/**
 * Interface for User Data Access Object
 * @author rahul
 *
 */
public interface UserDao {
	
	//Get user by userId integer value
	User getUser(int userId);
	
	//Get all the users for the system
    Set<User> getAllUsers();
    
    //Get a specific user based on username and password
    User getUserByUserNameAndPassword(String username, String password);
    
    // insert a User into database
    boolean insertUser(User u);
    
    // update a User details in database
    boolean updateUser(User u);
    
    // delete a User from database
    boolean deleteUser(User u);
}

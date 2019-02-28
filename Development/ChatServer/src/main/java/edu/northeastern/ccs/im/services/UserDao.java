package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
import java.util.Set;

import edu.northeastern.ccs.im.models.User;

/**
 * Interface for User Data Access Object
 * @author rahul
 *
 */
public interface UserDao {
	
	
	//Get user by userId integer value
	User getUser(int userId) throws SQLException;
	
	//Get all the users for the system
    Set<User> getAllUsers() throws SQLException;
    
    //Get a specific user based on username and password
    User getUserByUserNameAndPassword(String username, String password);
    
    //Get specific user by username
    User getUserByUserName(String username) throws SQLException;
    
    //Create a User entry in database
    boolean createUser(User u) throws SQLException;
    
    // insert a User into database
    /*
        Kunal Note: Commenting insertUser because there doesn't seem to be
                    a difference between createUser and insertUser from a
                    DBMS point of view
     */
    //boolean insertUser(User u);
    
    // update a User details in database
    boolean updateUser(User u);
    
    // delete a User from database
    boolean deleteUser(User u) throws SQLException;
}

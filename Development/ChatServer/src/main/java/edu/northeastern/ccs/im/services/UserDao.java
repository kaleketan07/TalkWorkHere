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
  boolean updateUser(User u) throws SQLException;

  // delete a User from database
  boolean deleteUser(User u) throws SQLException;
}

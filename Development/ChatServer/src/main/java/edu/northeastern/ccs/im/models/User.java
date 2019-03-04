package edu.northeastern.ccs.im.models;

/**
 * Class for User object with its data members
 *
 * @author rahul
 */
public class User {
	
	private String firstName;
	private String lastName;
	private String userName;
	private String userPassword;
	private boolean loggedIn;
	
	/**
	 * 
	 * @param firstName to have the first name of the user
	 * @param lastName to have the last name of the user
	 * @param userName to have the user name of the user
	 * @param userPassword to have the user password name of the user
	 * @param loggedInStatus to set the loggedIn status of the user
	 */
	public User(String firstName, String lastName, String userName, String userPassword, boolean loggedInStatus) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.userPassword = userPassword;
		this.loggedIn = loggedInStatus;
	}

	/**
	 * 
	 * @return firstName of the user
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * 
	 * @param firstName value to set the firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * 
	 * @return lastName of the user
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * 
	 * @param lastName value to set the firssttName
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * 
	 * @return userName of the user
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 
	 * @param userName value to set the userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * 
	 * @return the userPassword of the user
	 */
	public String getUserPassword() {
		return userPassword;
	}
	
	/**
	 * 
	 * @param userPassword value to set the userPassword
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * set the loggedIn flag to the given parameter value
	 * @param status - the true or false status of the user's loggedIn attribute
	 */
	public void setLoggedIn(boolean status) {
		loggedIn = status;
	}

	/**
	 * @return the loggedIn status of the user
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}

	/**
	 * Overrides the toString method for the User object.
	 *
	 * @return - string representation of username, firstName and lastName.
	 *
	 */
	public String toString(){
		return getUserName()+" : "+getFirstName()+" "+getLastName();
	}
}


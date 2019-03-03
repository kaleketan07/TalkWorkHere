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
	
	/**
	 * 
	 * @param firstName to have the first name of the user
	 * @param lastName to have the last name of the user
	 * @param userName to have the user name of the user
	 * @param userPassword to have the user password name of the user
	 */
	public User(String firstName, String lastName, String userName, String userPassword) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.userPassword = userPassword;
		
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
	 * @param string value to set the firstName 
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
	 * @param string value to set the firssttName 
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
	 * @param string value to set the userName 
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
	 * @param string value to set the userPassword 
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String toString(){
		return getUserName()+" : "+getFirstName()+" "+getLastName();
	}
}


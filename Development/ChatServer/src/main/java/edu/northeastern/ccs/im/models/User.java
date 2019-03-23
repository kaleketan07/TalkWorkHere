package edu.northeastern.ccs.im.models;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.services.ConversationalMessageService;

import java.io.IOException;
import java.sql.SQLException;

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
    private static ConversationalMessageService cms;
    private boolean searchable;

    static {
        try {
            cms = ConversationalMessageService.getInstance();
        } catch (ClassNotFoundException | IOException | SQLException e) {
            ChatLogger.error("Conversational Message Service failed to initialize: " + e);
        }
    }

    private ClientRunnable clientRunnable;

    /**
     * @param firstName      to have the first name of the user
     * @param lastName       to have the last name of the user
     * @param userName       to have the user name of the user
     * @param userPassword   to have the user password name of the user
     * @param loggedInStatus to set the loggedIn status of the user
     */
    public User(String firstName, String lastName, String userName, String userPassword, boolean loggedInStatus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.userPassword = userPassword;
        this.loggedIn = loggedInStatus;
        this.clientRunnable = null;
        this.searchable = true;
    }

    /**
     * @return firstName of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName value to set the firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return lastName of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName value to set the firstName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return userName of the user
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName value to set the userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userPassword of the user
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @param userPassword value to set the userPassword
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * set the loggedIn flag to the given parameter value
     *
     * @param status - the true or false status of the user's loggedIn attribute
     */
    public void setLoggedIn(boolean status) {
        loggedIn = status;
    }

    /**
     * Set the searchable flag of the user according to their preference
     *
     * @param searchableStatus - true or false status of the user's searchable preference
     */
    public void setSearchable(boolean searchableStatus) {
        searchable = searchableStatus;
    }

    /**
     * @return the loggedIn status of the user
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * @return the searchable status of the user
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Overrides the toString method for the User object.
     *
     * @return - string representation of username, firstName and lastName.
     */
    public String toString() {
        return getUserName() + " : " + getFirstName() + " " + getLastName();
    }

    /**
     * Send the received message to the user who is supposed to receive it. This method will
     * first check if the user is online by checking if there is a ClientRunnable present for
     * this instance and then enqueue the message if present accordingly.
     *
     * @param msg The message to be sent to this user
     */
    public String userSendMessage(Message msg) throws SQLException {
        String src = msg.getName();
        String msgText = msg.getTextOrPassword();
        String uniqueKey = null;
        clientRunnable = ClientRunnable.getClientByUsername(this.getUserName());
        if (clientRunnable != null && clientRunnable.isInitialized()) {
            uniqueKey = cms.insertConversationalMessage(src, this.getUserName(), msgText, true);
            if (msg.isGroupMessage()) {
            	clientRunnable.enqueueMessage(Message.addUniqueKeyToMsg(msg, msg.getTextOrPassword() +
                        System.lineSeparator() + "MessageKey of above message is : " + uniqueKey + System.lineSeparator() + "this message was sent on: " + msg.getReceiverOrPassword()));
            } else {
            	clientRunnable.enqueueMessage(Message.addUniqueKeyToMsg(msg, msg.getTextOrPassword() +
                        System.lineSeparator() + "MessageKey of above message is : " + uniqueKey));
            }
            
            return uniqueKey;
        }

        return cms.insertConversationalMessage(src, this.getUserName(), msgText, false);
    }
}


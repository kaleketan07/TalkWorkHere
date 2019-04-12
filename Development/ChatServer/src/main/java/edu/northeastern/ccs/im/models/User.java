/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

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
    private boolean isTapped;

    static {
        try {
            cms = ConversationalMessageService.getInstance();
        } catch (IOException | SQLException e) {
            ChatLogger.error("Conversational Message Service failed to initialize " + e.toString());
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
        this.isTapped = false;
    }

    /**
     * @return boolean      true, if the user that this object represents is tapped, else returns false
     */
    public boolean isTapped() {
        return isTapped;
    }

    /**
     * Sets the tapped flag for the user that this object represents.
     *
     * @param isTapped the value that denotes the user is tapped or not
     */
    public void setTapped(boolean isTapped) {
        this.isTapped = isTapped;
    }

    /**
     * @return String    first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName value to set for the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return String     lastName of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName value to set fir the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return String      userName of the user
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName value to set for the username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return String       the userPassword of the user
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @param userPassword value to set for the userPassword
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * set the loggedIn flag to the given parameter value
     *
     * @param status the true or false status of the user's loggedIn attribute
     */
    public void setLoggedIn(boolean status) {
        loggedIn = status;
    }

    /**
     * Set the searchable flag of the user according to their preference
     *
     * @param searchableStatus true or false status of the user's searchable preference
     */
    public void setSearchable(boolean searchableStatus) {
        searchable = searchableStatus;
    }

    /**
     * @return boolean      the loggedIn status of the user
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * @return boolean      the searchable status of the user
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Overrides the toString method for the User object.
     *
     * @return String       the string representation of username, firstName and lastName.
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
     * @return String       the unique key of the message sent
     * @throws SQLException the sql exception thrown in case of error in jdbc's interaction with the data source
     */
    public String userSendMessage(Message msg) throws SQLException {
        String src = msg.getName();
        String msgText = msg.getTextOrPassword();
        clientRunnable = ClientRunnable.getClientByUsername(this.getUserName());
        if (clientRunnable != null && clientRunnable.isInitialized()) {
            String uniqueKey = cms.insertConversationalMessage(src, this.getUserName(), msgText, true);
            enqueueMessageToUser(msg, uniqueKey);
            return uniqueKey;
        }

        return cms.insertConversationalMessage(src, this.getUserName(), msgText, false);
    }

    /**
     * Enqueue message to user depending on the type of the message when the user is logged in.
     *
     * @param msg       the message object
     * @param uniqueKey the unique key of the message
     */
    public void enqueueMessageToUser(Message msg, String uniqueKey) {
        clientRunnable = ClientRunnable.getClientByUsername(this.getUserName());
        if (msg.isGroupMessage()) {
            clientRunnable.enqueueMessage(Message.addUniqueKeyToMsg(msg, "Sent on group: " + msg.getReceiverOrPassword() + ": \n" + msg.getTextOrPassword() +
                    System.lineSeparator() + "MessageKey of above message is : " + uniqueKey + System.lineSeparator()));
        } else {
            clientRunnable.enqueueMessage(Message.addUniqueKeyToMsg(msg, msg.getTextOrPassword() +
                    System.lineSeparator() + "MessageKey of above message is : " + uniqueKey));
        }
    }


    /**
     * The overriden equals method to check if the two user objects are equal, based on the username of the users
     *
     * @param obj The object to be checked for equality with the current object
     * @return boolean  true if the current and given objects have same username, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return this.userName.equals(((User) obj).getUserName());
        }
        return false;
    }

    /**
     * Overridden method to generate unique hashcode for every User object
     *
     * @return int     a unique integer for every user object
     */
    @Override
    public int hashCode() {
        return (this.userName != null) ? 31 * userName.hashCode() : 0;
    }
}


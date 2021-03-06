/*
 ***************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ***************************************************************************************
 */

package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.NetworkConnection;
import edu.northeastern.ccs.im.models.ConversationalMessage;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.ConversationalMessageService;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.UserService;

/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class ClientRunnable implements Runnable {
    /**
     * Utility class which we will use to send and receive communication to this
     * client.
     */
    private NetworkConnection connection;

    /**
     * Id for the user for whom we use this ClientRunnable to communicate.
     */
    private int userId;

    /**
     * Name that the client used when connecting to the server.
     */
    private String name;

    /**
     * private final string for a government user
     */
    private static final String GOVERNMENT = "government";

    /**
     * Whether this client has been initialized, set its user name, and is ready to
     * receive messages.
     */
    private boolean initialized;

    /**
     * Whether this client has been terminated, either because he quit or due to
     * prolonged inactivity.
     */
    private boolean terminate;

    /**
     * The timer that keeps track of the clients activity.
     */
    private ClientTimer timer;

    /**
     * The future that is used to schedule the client for execution in the thread
     * pool.
     */
    private ScheduledFuture<?> runnableMe;

    /**
     * Collection of messages queued up to be sent to this client.
     */
    private Queue<Message> waitingList;

    /**
     * Stores the userService instance to be used across multiple conditions.
     */
    private UserService userService;

    /**
     * Stores the groupService instance to be used across multiple conditions.
     */
    private GroupService groupService;

    /**
     * Store instance of ConversationalMessage to be used to retrive messages
     */
    private ConversationalMessageService conversationalMessagesService;

    /**
     * Stores the invitationService instance.
     */
    private InvitationService invitationService;

    /**
     * This static data structure stores the client runnable instances
     * associated with their usernames for easy lookup during messaging.
     */
    private static Map<String, ClientRunnable> userClients = new HashMap<>();

    /**
     * This static counter is going to be used for making every "invalid"
     * connection unique, so we can add all invalid connections of a user to
     * the hashmap and send out error messages together
     */
    private static int invalidCounter = 0;

    /**
     * Constant to store invalid id
     */
    private static final int INVALID_USER_ID = -1;

    /**
     * Constant to store a common error message denoting a syntax error
     */
    private static final String CHECK_SYNTAX_ERROR_MESSAGE = "Something went wrong while retrieving data. " +
            "Please check your syntax";


    /**
     * Used for incrementing the invalidCounter, defined a separate method here
     * on account of invalidCounter being a static field
     *
     * @param invalidCounter the static counter
     */
    private static void incrementInvalidCounter(int invalidCounter) {
        ClientRunnable.invalidCounter = invalidCounter + 1;
    }

    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param network NetworkConnection used by this new client
     */
    ClientRunnable(NetworkConnection network) {
        // Create the class we will use to send and receive communication
        connection = network;
        // Mark that we are not initialized
        initialized = false;
        // Mark that we are not terminated
        terminate = false;
        // Create the queue of messages to be sent
        waitingList = new ConcurrentLinkedQueue<>();
        // Mark that the client is active now and start the timer until we
        // terminate for inactivity.
        timer = new ClientTimer();

        // create user Service instance
        try {
            userService = UserService.getInstance();
            groupService = GroupService.getGroupServiceInstance();
            conversationalMessagesService = ConversationalMessageService.getInstance();
            invitationService = InvitationService.getInstance();
        } catch (SQLException | IOException e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - ClientRunnable() : " + ChatLogger.getTrace(e));
        }
    }

    /**
     * Check to see for an initialization attempt and process the message sent.
     */
    private void checkForInitialization() {
        // Check if there are any input messages to read
        Iterator<Message> messageIter = connection.iterator();
        if (messageIter.hasNext()) {
            // If a message exists, try to use it to initialize the connection
            Message msg = messageIter.next();
            if (setUserName(msg.getName())) {
                // Update the time until we terminate this client due to inactivity.
                timer.updateAfterInitialization();
                // Set that the client is initialized.
                initialized = true;
            } else {
                initialized = false;
            }
        }
    }

    /**
     * Check if the message is properly formed. At the moment, this means checking
     * that the identifier is set properly.
     *
     * @param msg Message to be checked
     * @return boolean  True if message is correct; false otherwise
     */
    private boolean messageChecks(Message msg) {
        // Check that the message name matches.
        return (msg.getName() != null) && (msg.getName().equalsIgnoreCase(getName()));
    }

    /**
     * Sending response message from prattle to client
     */
    private void enqueuePrattleResponseMessage(String responseMessage) {
        this.enqueueMessage(Message.makePrattleMessage(responseMessage));
    }

    /**
     * Immediately send this message to the client. This returns if we were
     * successful or not in our attempt to send the message.
     *
     * @param message Message to be sent immediately.
     * @return boolean  True if we sent the message successfully; false otherwise.
     */
    private boolean sendMessage(Message message) {
        ChatLogger.info("\t" + message);
        return connection.sendMessage(message);
    }

    /**
     * Try allowing this user to set his/her user name to the given username.
     *
     * @param userName The new value to which we will try to set userName.
     * @return boolean      True if the username is deemed acceptable; false otherwise
     */
    private boolean setUserName(String userName) {
        boolean result = false;
        // Now make sure this name is legal.
        if (isValidUserName(userName)) {
            if (userClients.getOrDefault(userName, null) == null) {
                // Optimistically set this users ID number.
                setName(userName);
                userId = hashCode();
                result = true;
                userClients.put(userName, this);
            } else {
                incrementInvalidCounter(invalidCounter);
                setName("invalid-" + userName + "-" + invalidCounter);
                userId = INVALID_USER_ID;
                result = true;
                this.enqueuePrattleResponseMessage("There is already a user connected with this username. Please " +
                        "type BYE and try logging in with another username.");
            }
        } else {
            // Clear this name; we cannot use it. *sigh*
            userId = INVALID_USER_ID;
        }
        return result;
    }

    /**
     * Add the given message to this client to the queue of message to be sent to
     * the client.
     *
     * @param message Complete message to be sent.
     */
    public void enqueueMessage(Message message) {
        waitingList.add(message);
    }

    /**
     * Get the name of the user for which this ClientRunnable was created.
     *
     * @return String   Returns the name of this client.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the user for which this ClientRunnable was created.
     *
     * @param name The name for which this ClientRunnable.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the user for which this ClientRunnable was created.
     *
     * @return int      Returns the current value of userName.
     */
    int getUserId() {
        return userId;
    }

    /**
     * Return if this thread has completed the initialization process with its
     * client and is read to receive messages.
     *
     * @return boolean  True if this thread's client should be considered; false otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Perform the periodic actions needed to work with this client.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        try {
            // The client must be initialized before we can do anything else
            if (!initialized) {
                checkForInitialization();
            } else {
                handleIncomingMessages();
                handleOutgoingMessages();
            }
            // Finally, check if this client have been inactive for too long and,
            // when they have, terminate the client.
            if (timer.isBehind()) {
                ChatLogger.error("Timing out or forcing off a user " + name);
                this.enqueuePrattleResponseMessage("Timeout occurred. Logging you out. Apologies.\n");
                terminate = true;
            }
            if (terminate) {
                terminateClient();
            }
        } catch (SQLException e) {
            ChatLogger.error("SQL Exception occurred - ClientRunnable.java - run() : " + ChatLogger.getTrace(e));
        }
    }

    /**
     * Checks incoming messages and performs appropriate actions based on the type
     * of message.
     */
    private void handleIncomingMessages() throws SQLException {
        // Client has already been initialized, so we should first check
        // if there are any input
        // messages.
        Iterator<Message> messageIter = connection.iterator();
        if (messageIter.hasNext()) {
            // Get the next message
            Message msg = messageIter.next();
            User user = userService.getUserByUserName(msg.getName());
            // If the user does not exist, then maybe they are trying to register
            if (user == null && !(msg.isRegisterMessage() || msg.isLoginMessage()))
                ChatLogger.error("User does not exist");
            else {
                if (msg.terminate()) {
                    // Stop sending the poor client message.
                    terminate = true;
                    // Reply with a quit message.
                    enqueueMessage(Message.makeQuitMessage(name));
                } else if (msg.isLoginMessage() || msg.isRegisterMessage() || (user != null && user.isLoggedIn())) {
                    processMessage(msg);
                } else {
                    this.enqueuePrattleResponseMessage("Sorry, you are not logged in to use Prattle. Please log in " +
                            "and try again.");
                }
            }
        }
    }

    /**
     * Handles the register message
     *
     * @param msg the incoming register message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleRegisterMessage(Message msg) throws SQLException {
        // Register the user after checking whether the user already exists or no
        User currentUser = userService.getUserByUserName(msg.getName());
        if (currentUser != null) {
            this.enqueuePrattleResponseMessage("Username already exists.");
        } else {
            // since the user was not found, a new user with this name may be created after password validation checks
            String password = msg.getTextOrPassword();
            String confirmPassword = msg.getReceiverOrPassword();
            if (!isValidPassword(password)) {
                this.enqueuePrattleResponseMessage("Password not strong enough. Make sure the password:\n"
                        + "1. is 5 to 12 characters long.\n"
                        + "2. contains atleast one uppercase and one lowercase characters.\n"
                        + "3. contains atleast one digit from 0-9.\n"
                        + "4. contains atleast one character from @, #, $, %, ^, &, +, =.\n"
                        + "5. contains no spaces");
            } else if (password.equals(confirmPassword)) {
                userService.createUser(new User(null, null, msg.getName(), password, true));
                this.enqueuePrattleResponseMessage("User " + msg.getName() + " registered!");
            } else {
                this.enqueuePrattleResponseMessage("Password and confirm password do not match");
            }
        }
    }

    /**
     * Checks if passed userName is valid.
     *
     * @param userName the user name of the user
     * @return boolean      true, if user name passes the checks of validity, else returns false
     */
    private boolean isValidUserName(String userName) {
        return (userName != null && userName.length() < 12);
    }

    /**
     * Checks if is valid password.
     *
     * @param password the password of the user
     * @see https://stackoverflow.com/a/3802238 for the regular expression used in this method
     * @return boolean      true, if the password satisfies all valid password checks
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+!_])(?=\\S+$).{5,12}$");
    }

    /**
     * Handles private message
     *
     * @param msg the incoming message_user type of message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handlePrivateMessage(Message msg) throws SQLException {
        User destUser = userService.getUserByUserName(msg.getReceiverOrPassword());
        if (destUser == null) {
            this.enqueuePrattleResponseMessage("Destination username does not exist.");
        } else {
            this.enqueuePrattleResponseMessage("the unique key for the message that you just sent is: " +
                    destUser.userSendMessage(msg));
        }
    }

    /**
     * Handles the login message
     *
     * @param msg the incoming login message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleLoginMessage(Message msg) throws SQLException {
        // Login the user after checking in the user with this username-password combo exists
        User currentUser = userService.getUserByUserNameAndPassword(msg.getName(), msg.getTextOrPassword());
        if (currentUser == null) {
            this.enqueuePrattleResponseMessage("Incorrect username and password");
        } else if (!userService.updateUserAttributes(
                currentUser.getUserName(),
                "logged_in",
                "1")
                ) {
            this.enqueuePrattleResponseMessage("The profile details for " + currentUser.getUserName()
                    + " was not updated.");
        } else {
            handleSuccessfulLogin(msg, currentUser);
        }
    }

    /**
     * Handles successful login and does all activity happening after a successful login.
     *
     * @param msg         the message object that was sent
     * @param currentUser the object representing the user under consideration
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleSuccessfulLogin(Message msg, User currentUser) throws SQLException {
        this.enqueuePrattleResponseMessage("Welcome " + msg.getName() + "!! Here's what you missed::\n");
        if (currentUser.isTapped()) {
            notifyGovernment(currentUser);
        }
        sendMessagesToUser(currentUser);
        sendInvitationsToUser(msg);
        sendInvitationsToModerator(msg);
    }


    /**
     * Notify government that this user logged in.
     *
     * @param currentUser the user under consideration
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void notifyGovernment(User currentUser) throws SQLException {
        Timestamp notificationTimestamp = new Timestamp(System.currentTimeMillis());
        String notification = "\nUser with user name: " + currentUser.getUserName() + " logged in at: " +
                notificationTimestamp + "\n";
        User govtUser = userService.getUserByUserName(GOVERNMENT);
        govtUser.userSendMessage(Message.makePrattleMessage(notification));
    }

    /**
     * sends unsent messages to user on successful login
     *
     * @param currentUser the user who has logged on
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void sendMessagesToUser(User currentUser) throws SQLException {
        List<ConversationalMessage> unsentMessages
                = conversationalMessagesService.getMessagesForUser(currentUser.getUserName(), true);
        for (ConversationalMessage m : unsentMessages) {
            Message resultMessage = createMessageFromConversationalMessage(m);
            currentUser.enqueueMessageToUser(resultMessage, m.getMessageUniquekey());
            conversationalMessagesService.markMessageAsSent(m.getMessageUniquekey());
        }
    }

    /**
     * sends unsent invitations to user on successful login
     *
     * @param msg the login message received which triggers this action
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void sendInvitationsToModerator(Message msg) throws SQLException {
        //send unsent invitations to the moderator for each group
        Set<String> groups = groupService.getGroupsByModerator(msg.getName());
        for (String groupName : groups) {
            Set<Message> invitationsGroup = invitationService.getInvitationsForGroup(groupName);
            for (Message invitation : invitationsGroup) {
                this.enqueuePrattleResponseMessage(invitation.getName() + " has invited " +
                        invitation.getTextOrPassword() + " to join the group " + groupName);
                invitationService.setInvitationIsSentToModerator(
                        invitation.getTextOrPassword(),
                        invitation.getReceiverOrPassword()
                );
            }
        }
    }

    /**
     * sends unsent invitations to moderator on successful login
     *
     * @param msg the login message received which triggers this action
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void sendInvitationsToUser(Message msg) throws SQLException {
        // send unsent invitations to the user
        Set<Message> invitations = invitationService.getInvitationsForInvitee(msg.getName());
        for (Message invitation : invitations) {
            this.enqueuePrattleResponseMessage("You have been invited to join the group " +
                    invitation.getReceiverOrPassword() + " by user " + invitation.getName());
            invitationService.setInvitationIsSentToInvitee(
                    invitation.getTextOrPassword(),
                    invitation.getReceiverOrPassword()
            );
        }
    }

    /**
     * Creates the message from conversational message.
     *
     * @param m the conversational message object returned by the conversational message
     *          service for the unsent message
     * @return Message  the message object of type Message for depending upon if the message is a
     * group message of a private user message
     */
    private Message createMessageFromConversationalMessage(ConversationalMessage m) {
        Message resultMessage;
        if (m.getGroupUniqueKey() == null) {
            resultMessage = Message.makePrivateUserMessage(m.getSourceName(), m.getMessageText(), m.getDestinationName());
        } else {
            resultMessage = Message.makeGroupMessage(
                    m.getSourceName(),
                    m.getMessageText(),
                    m.getGroupUniqueKey().split("::")[1]
            );
        }
        return resultMessage;
    }

    /**
     * Handles the createGroupMessage
     *
     * @param msg the incoming createGroupMessage
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleCreateGroupMessage(Message msg) throws SQLException {
        // Create a group with the specified name with the sender as the moderator,
        // if a group with the same name does not already exists
        Group existingGroup = groupService.getGroup(msg.getTextOrPassword());
        if (existingGroup != null) {
            this.enqueuePrattleResponseMessage("Groupname already exists! Please use a different group name.");
        } else {
            groupService.createGroup(msg.getTextOrPassword(), msg.getName());
            groupService.addUserToGroup(msg.getTextOrPassword(), msg.getName());
            this.enqueuePrattleResponseMessage("Successfully created group: " + msg.getTextOrPassword());
        }
    }

    /**
     * Handles the GetGroupMessage
     *
     * @param msg the incoming getGroup message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleGetGroupMessage(Message msg) throws SQLException {
        // Create a group with the specified name with the sender as the moderator,
        // if a group with the same name does not already exists
        Group existingGroup = groupService.getGroup(msg.getTextOrPassword());
        if (existingGroup == null) {
            this.enqueuePrattleResponseMessage("Groupname does not exist. So no details can be provided");
        } else {
            this.enqueuePrattleResponseMessage("Groupname: " + existingGroup.getGroupName() + " Moderator: " +
                    existingGroup.getModeratorName());
        }
    }

    /**
     * Handles the handleDeleteGroupMessage
     *
     * @param msg the incoming login message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDeleteGroupMessage(Message msg) throws SQLException {
        // Delete the group after getting the valid moderator name and valid group name
        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getTextOrPassword());
        // if group does not exist
        if (currentGroup == null) {
            this.enqueuePrattleResponseMessage("Group does not exist.");
        } else {
            // if the user is in fact the moderator of the group only then delete the group
            if (groupService.isModerator(currentGroup.getGroupName(), currentUser.getUserName())) {
                groupService.deleteGroup(currentGroup.getGroupName());
                this.enqueuePrattleResponseMessage("Group deleted successfully.");
            } else {
                this.enqueuePrattleResponseMessage("CurrentUser is not the moderator of the group.");
            }
        }
    }

    /**
     * Handles the handleDeleteUserMessage
     *
     * @param msg the incoming delete user message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDeleteUserMessage(Message msg) throws SQLException {
        // Delete the user after getting the user
        User currentUser = userService.getUserByUserName(msg.getName());
        boolean result = userService.deleteUser(currentUser);
        if (!result) {
            this.enqueuePrattleResponseMessage("User could not be deleted");
        } else {
            this.enqueuePrattleResponseMessage("User profile set to inactive.");
            this.terminate = true;
        }
    }

    /**
     * Helper for checking the validity of input before calling the handlers for add or remove user to group message
     *
     * @param currentUser  the current user requesting the service
     * @param currentGroup the group from which the user needs to be removed or added to.
     * @param guestUser    the user to be added or removed.
     * @return boolean      true or false value based on the required checks passed or failed respectively.
     */
    private boolean helperAddRemoveUserToGroupMessage(User currentUser, Group currentGroup, User guestUser) {
        if (currentGroup == null)
            this.enqueuePrattleResponseMessage("The group you are trying to add to does not exist!");
        else if (!currentGroup.getModeratorName().equals(currentUser.getUserName()))
            this.enqueuePrattleResponseMessage("You do not have the permissions to perform this operation");
        else if (guestUser == null)
            this.enqueuePrattleResponseMessage("The user you are trying to add/remove does not exist");
        else
            return true;
        return false;
    }

    /**
     * Helper for checking the validity of the input before calling handlers for add or remove group to group message.
     *
     * @param currentUser  the current user
     * @param currentGroup the current group
     * @param guestGroup   the guest group
     * @return boolean      true, if successful
     */
    private boolean helperAddRemoveGroupToGroupMessage(User currentUser, Group currentGroup, Group guestGroup) {
        if (currentGroup == null)
            this.enqueuePrattleResponseMessage("The group you are trying to add to does not exist!");
        else if (!currentGroup.getModeratorName().equals(currentUser.getUserName()))
            this.enqueuePrattleResponseMessage("You do not have the permissions to perform this operation");
        else if (guestGroup == null)
            this.enqueuePrattleResponseMessage("The group you are trying to add/remove does not exist");
        else
            return true;
        return false;
    }


    /**
     * Handle PrivateReplyMessage to group message.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handlePrivateReplyMessage(Message msg) throws SQLException {
        String destName = conversationalMessagesService.getSender(msg.getReceiverOrPassword());
        if (destName != null) {
            User destUser = userService.getUserByUserName(destName);
            if (destUser == null) {
                this.enqueuePrattleResponseMessage("msg_UniqueKey provided is wrong");
            } else {
                this.enqueuePrattleResponseMessage("the unique key for the message that you just sent is: " +
                        destUser.userSendMessage(msg));
            }
        } else
            this.enqueuePrattleResponseMessage("The msg_uniqueKey provided was wrong."
                    + " Please try again with the right msg_uniqueKey");
    }

    /**
     * Handle add user to group message.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleAddUserToGroupMessage(Message msg) throws SQLException {

        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
        User guestUser = userService.getUserByUserName(msg.getTextOrPassword());
        if (helperAddRemoveUserToGroupMessage(currentUser, currentGroup, guestUser)) {
            if (groupService.addUserToGroup(currentGroup.getGroupName(), guestUser.getUserName())) {
                this.enqueuePrattleResponseMessage("User was added successfully");
            } else {
                this.enqueuePrattleResponseMessage("User was not added as the user was already there");
            }
        }
    }

    /**
     * Handle remove user from group message.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleRemoveUserFromGroupMessage(Message msg) throws SQLException {
        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
        User guestUser = userService.getUserByUserName(msg.getTextOrPassword());
        if (helperAddRemoveUserToGroupMessage(currentUser, currentGroup, guestUser)) {
            if (groupService.removeUserFromGroup(currentGroup.getGroupName(), guestUser.getUserName()))
                this.enqueuePrattleResponseMessage("User was removed successfully");
            else
                this.enqueuePrattleResponseMessage("user was not removed as the user was not in the group");
        }
    }

    /**
     * Handle the update message sent by the user. Check which number value was sent,
     * 1 is first name, 2 is second name, 3 is password, 4 is searchability and call updateUserAttributes accordingly
     * Handle the update message sent by the user. This just updates the first name and
     * last name for the time being.
     *
     * @param msg The incoming user profile update message
     */
    private void handleUserProfileUpdateMessage(Message msg) {
        try {
            String mappedAttributeName = helperUserProfileUpdateMessage(msg.getTextOrPassword());
            if (userService.updateUserAttributes(msg.getName(), mappedAttributeName, msg.getReceiverOrPassword()))
                this.enqueuePrattleResponseMessage("Updated the value: " + mappedAttributeName + " successfully.");
            else
                this.enqueuePrattleResponseMessage("Failed updating the value:" + mappedAttributeName);
        } catch (SQLException e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleUserProfileUpdateMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage("Failed updating the attribute. Please note the syntax for UPU " +
                    "messages using HELP UPU");
        }
    }

    /**
     * Handle a user following other user of the other user exists in the database
     *
     * @param msg The incoming follow user message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleFollowUserMessage(Message msg) throws SQLException {
        User followeeUser = userService.getUserByUserName(msg.getTextOrPassword());
        User followerUser = userService.getUserByUserName(msg.getName());

        if (followeeUser == null) {
            this.enqueuePrattleResponseMessage("The user you are trying to follow does not exist");
        } else {
            try {
                userService.followUser(followeeUser, followerUser);
                this.enqueuePrattleResponseMessage("You are now following : " + followeeUser.getUserName());
            } catch (SQLException e) {
                ChatLogger.error("Exception occurred - ClientRunnable.java - handleFollowUserMessage() : " + ChatLogger.getTrace(e));
                this.enqueuePrattleResponseMessage("You are already following : " + followeeUser.getUserName());
            }
        }
    }

    /**
     * Handle a user unfollowing other user if the other user exists in the database
     *
     * @param msg The incoming follow user message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleUnfollowUserMessage(Message msg) throws SQLException {
        User followeeUser = userService.getUserByUserName(msg.getTextOrPassword());
        User followerUser = userService.getUserByUserName(msg.getName());

        if (followeeUser == null)
            this.enqueuePrattleResponseMessage("The user you are trying to unfollow does not exist");
        else if (userService.unfollowUser(followeeUser, followerUser))
            this.enqueuePrattleResponseMessage("You have stopped following : " + followeeUser.getUserName());
        else
            this.enqueuePrattleResponseMessage("Aborted ..! You were not following : " + followeeUser.getUserName());
    }


    /**
     * Helper function to help map the attribute name
     * to the number value sent by the user
     *
     * @param attributeNumber The number of the attribute to be mapped
     * @return String           the mapped attribute name to be updated
     */
    private String helperUserProfileUpdateMessage(String attributeNumber) throws SQLException {
        String mappedAttribute;
        if (attributeNumber.equals("1"))
            mappedAttribute = "first_name";
        else if (attributeNumber.equals("2"))
            mappedAttribute = "last_name";
        else if (attributeNumber.equals("3"))
            mappedAttribute = "user_password";
        else if (attributeNumber.equals("4"))
            mappedAttribute = "user_searchable";
        else
            throw new SQLException("Number not in bounds");
        return mappedAttribute;
    }

    /**
     * Handles the messages get followers.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleGetFollowersMessage(Message msg) throws SQLException {
        User currUser = userService.getUserByUserName(msg.getName());
        Map<String, String> resultantSet;
        try {
            resultantSet = userService.getFollowers(currUser);
            if (resultantSet.isEmpty())
                this.enqueuePrattleResponseMessage("Sorry, did not find any followers");
            else
                helperForBuildingAndSendingSearchMessage(resultantSet, "User");
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleGetFollowersMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE +
                    " using HELP GFR.");
        }
    }

    /**
     * Handles the messages get online users.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleGetOnlineUserMessage(Message msg) throws SQLException {
        User currUser = userService.getUserByUserName(msg.getName());
        Map<String, String> resultantSet;
        try {
            resultantSet = userService.getOnlineUsers(currUser);
            if (resultantSet.isEmpty())
                this.enqueuePrattleResponseMessage("Sorry, did not find any online users");
            else
                helperForBuildingAndSendingSearchMessage(resultantSet, "User");
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleGetOnlineUserMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE +
                    " using HELP GOU.");
        }
    }


    /**
     * Handles the messages get followees.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleGetFolloweesMessage(Message msg) throws SQLException {
        User currUser = userService.getUserByUserName(msg.getName());
        Map<String, String> resultantSet;
        try {
            resultantSet = userService.getFollowees(currUser);
            if (resultantSet.isEmpty())
                this.enqueuePrattleResponseMessage("Sorry, did not find any followees");
            else
                helperForBuildingAndSendingSearchMessage(resultantSet, "User");
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleGetFolloweesMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE + " using HELP GFE.");
        }
    }

    /**
     * Handles the messages sent on Groups.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleGroupMessage(Message msg) throws SQLException {
        User currUser = userService.getUserByUserName(msg.getName());
        Group currGroup = groupService.getGroup(msg.getReceiverOrPassword());
        if (currGroup == null) {
            this.enqueuePrattleResponseMessage("The destination group does not exist");
        } else if (!groupService.isUserMemberOfTheGroup(currGroup.getGroupName(), currUser.getUserName())) {
            this.enqueuePrattleResponseMessage("Please join group " + currGroup.getGroupName() +
                    " to send a message on it");
        } else {
            this.enqueuePrattleResponseMessage("message sent successfully");
            // generate the group key to mark all individual messages sent as a result of this group message
            long time = System.currentTimeMillis();
            Timestamp sqlTimestamp = new Timestamp(time);
            String uniqueGroupKey = currUser.getUserName() + "::" + currGroup.getGroupName() + "::" + sqlTimestamp;
            currGroup.groupSendMessage(msg, uniqueGroupKey);
            this.enqueuePrattleResponseMessage("The group message key for the message you just sent is: " +
                    uniqueGroupKey);
        }
    }

    /**
     * The helper for invitation messages to check if the pre conditions are met
     *
     * @param inviter   The inviter who is sending a group message
     * @param invitee   The invitee who will receive a group message
     * @param groupName The group which for which the message is being sent
     * @return boolean          true if all the checks pass else false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean checkInvitationMessageFromUserHelper(String inviter, String invitee, String groupName)
            throws SQLException {
        User userInviter = (inviter == null) ? null : userService.getUserByUserName(inviter);
        User userInvitee = userService.getUserByUserName(invitee);
        Group group = groupService.getGroup(groupName);
        Set<User> groupUsers = groupService.getMemberUsers(groupName);

        boolean result = true;
        if (group == null) {
            this.enqueuePrattleResponseMessage("Cannot perform operation: Invalid group name provided.");
            result = false;
        } else if (userInvitee == null) {
            this.enqueuePrattleResponseMessage("Cannot perform operation: Invalid user name provided.");
            result = false;
        } else if (groupUsers.contains(userInvitee)) {
            this.enqueuePrattleResponseMessage("The invitee " + invitee + " is already a member of group " + groupName);
            result = false;
        } else if (userInviter != null && !groupUsers.contains(userInviter)) {
            this.enqueuePrattleResponseMessage("Since you are not a member of group " + groupName +
                    "; you cannot perform this operation.");
            result = false;
        }
        return result;
    }

    /**
     * The handle for messages of type Create Invitation
     * where a user sends an invite to another user to join a group
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleCreateInvitationMessage(Message msg) throws SQLException {
        String inviter = msg.getName();
        String invitee = msg.getTextOrPassword();
        String groupName = msg.getReceiverOrPassword();
        if (checkInvitationMessageFromUserHelper(inviter, invitee, groupName)) {
            if (invitationService.getInvitation(invitee, groupName) != null)
                this.enqueuePrattleResponseMessage("An invitation has already been sent to user " + invitee +
                        " for the group " + groupName);
            else if (invitationService.createInvitation(inviter, invitee, groupName)) {
                ClientRunnable inviteeClient = userClients.getOrDefault(invitee, null);
                if (inviteeClient != null) {
                    inviteeClient.enqueuePrattleResponseMessage("You have been invited to join group " +
                            groupName + " by user " + inviter);
                    invitationService.setInvitationIsSentToInvitee(invitee, groupName);
                }
                String moderator = groupService.getGroup(groupName).getModeratorName();
                ClientRunnable moderatorClient = userClients.getOrDefault(moderator, null);
                if (moderatorClient != null) {
                    moderatorClient.enqueuePrattleResponseMessage(inviter + " has invited user " + invitee +
                            " to join the group " + groupName);
                    invitationService.setInvitationIsSentToModerator(invitee, groupName);
                }
                this.enqueuePrattleResponseMessage("The invitation was successfully sent.");
            } else
                this.enqueuePrattleResponseMessage("Unable to send invitation.");
        }
    }

    /**
     * The handle for messages of type Delete Invitation where an inviter
     * who has sent an invitation to an invitee wants to delete the invitation
     * so that it is unavailable for acceptance by the user
     * or approval by the moderator after deletion.
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDeleteInvitationUserMessage(Message msg) throws SQLException {
        String inviter = msg.getName();
        String invitee = msg.getTextOrPassword();
        String groupName = msg.getReceiverOrPassword();

        if (checkInvitationMessageFromUserHelper(inviter, invitee, groupName)) {
            Message invitation = invitationService.getInvitation(invitee, groupName);
            if (invitation == null)
                this.enqueuePrattleResponseMessage("No invitation exists for the given user and group.");
            else if (invitation.isInvitationDeleted())
                this.enqueuePrattleResponseMessage("Invitation is already deleted.");
            else if (!invitation.getName().equals(inviter))
                this.enqueuePrattleResponseMessage("Since you did not initiate this invitation you cannot delete it");
            else if (invitationService.deleteInvitation(inviter, invitee, groupName))
                this.enqueuePrattleResponseMessage("The invitation was successfully deleted.");
            else
                this.enqueuePrattleResponseMessage("Unable to delete invitation.");
        }
    }

    /**
     * The handle for messages of type Accept Invitation where a user
     * accepts their invitation to join a group
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleAcceptInvitationUserMessage(Message msg) throws SQLException {
        String invitee = msg.getName();
        String groupName = msg.getTextOrPassword();

        if (checkInvitationMessageFromUserHelper(null, invitee, groupName)) {
            Message invitation = invitationService.getInvitation(invitee, groupName);
            if (invitation == null)
                this.enqueuePrattleResponseMessage("There is no invitation in your name for the group " + groupName);
            else if (invitation.isInvitationAccepted())
                this.enqueuePrattleResponseMessage("You have already accepted this invitation; you may have been " +
                        "removed from the group");
            else if (invitation.isInvitationDenied())
                this.enqueuePrattleResponseMessage("You have already denied this invitation; you cannot accept it now.");
            else if (invitation.isInvitationRejected())
                this.enqueuePrattleResponseMessage("This invitation was rejected by the group moderator, you " +
                        "will have to wait for an invite from another user");
            else if (invitation.isInvitationDeleted())
                this.enqueuePrattleResponseMessage("This invitation was deleted by the sender, you will have " +
                        "to wait for another invite.");
            else if (invitationService.acceptDenyInvitation(invitee, groupName, true)) {
                this.enqueuePrattleResponseMessage("The invitation was successfully accepted.");
                if (invitation.isInvitationApproved() && groupService.addUserToGroup(groupName, invitee)) {
                    this.enqueuePrattleResponseMessage("Since your invitation approved by the moderator you " +
                            "have been added to the group " + groupName);
                }
            } else
                this.enqueuePrattleResponseMessage("Unable to accept invitation.");
        }
    }

    /**
     * The handle for messages of type Deny Invitation where a user
     * denies their invitation to join a group
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDenyInvitationUserMessage(Message msg) throws SQLException {
        String invitee = msg.getName();
        String groupName = msg.getTextOrPassword();
        if (checkInvitationMessageFromUserHelper(null, invitee, groupName)) {
            Message invitation = invitationService.getInvitation(invitee, groupName);
            if (invitation == null)
                this.enqueuePrattleResponseMessage("There is no invitation in your name for the group " + groupName);
            else if (invitation.isInvitationRejected())
                this.enqueuePrattleResponseMessage("This invitation is already rejected by moderator; " +
                        "you do not need to deny it anymore");
            else if (invitation.isInvitationAccepted())
                this.enqueuePrattleResponseMessage("You have already accepted this invitation; you cannot deny it now");
            else if (invitation.isInvitationDenied())
                this.enqueuePrattleResponseMessage("You have already denied this invitation; you cannot deny it now");
            else if (invitation.isInvitationDeleted())
                this.enqueuePrattleResponseMessage("This invitation was deleted by the sender, hence cannot be denied");
            else if (invitationService.acceptDenyInvitation(invitee, groupName, false))
                this.enqueuePrattleResponseMessage("The invitation was denied successfully.");
            else
                this.enqueuePrattleResponseMessage("Unable to deny invitation.");
        }
    }

    /**
     * The handle for messages of type Approve Invitation where a moderator
     * approves an invitation for a user to join their group
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleApproveInvitationModeratorMessage(Message msg) throws SQLException {
        String moderator = msg.getName();
        String invitee = msg.getTextOrPassword();
        String groupName = msg.getReceiverOrPassword();
        User userInvitee = userService.getUserByUserName(invitee);
        Set<User> groupUsers = groupService.getMemberUsers(groupName);
        Message invitation = invitationService.getInvitation(invitee, groupName);
        Group group = groupService.getGroup(groupName);

        if (group == null)
            this.enqueuePrattleResponseMessage("Invalid group name " + groupName);
        else if (!groupService.isModerator(groupName, moderator))
            this.enqueuePrattleResponseMessage("You cannot approve this invitation since you are " +
                    "not a moderator of this group");
        else if (groupUsers.contains(userInvitee))
            this.enqueuePrattleResponseMessage("The user " + invitee + " is already a member of the group " + groupName);
        else if (invitation == null)
            this.enqueuePrattleResponseMessage("The invitation for invitee " + invitee + " group " +
                    groupName + " does not exist");
        else if (invitation.isInvitationRejected())
            this.enqueuePrattleResponseMessage("You have already rejected this invitation, you cannot approve it now.");
        else if (invitation.isInvitationDeleted())
            this.enqueuePrattleResponseMessage("This invitation was deleted by the sender, hence cannot be approved");
        else if (invitationService.approveRejectInvitation(invitee, groupName, true)) {
            this.enqueuePrattleResponseMessage("The invitation was successfully approved.");
            if (invitation.isInvitationAccepted() && groupService.addUserToGroup(groupName, invitee)) {
                this.enqueuePrattleResponseMessage("Since this invitation is already accepted, " +
                        "the user " + invitee + " was added to " + groupName);
            }
        } else
            this.enqueuePrattleResponseMessage("Unable to approve invitation.");
    }

    /**
     * The handle for messages of type Reject Invitation where a moderator
     * reject an invitation for a user to not allow them to join their group
     *
     * @param msg The message to be handled
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleRejectInvitationModeratorMessage(Message msg) throws SQLException {
        String moderator = msg.getName();
        String invitee = msg.getTextOrPassword();
        String groupName = msg.getReceiverOrPassword();
        User userInvitee = userService.getUserByUserName(invitee);
        Set<User> groupUsers = groupService.getMemberUsers(groupName);
        Message invitation = invitationService.getInvitation(invitee, groupName);
        Group group = groupService.getGroup(groupName);

        if (group == null)
            this.enqueuePrattleResponseMessage("Invalid Group name " + groupName);
        if (!groupService.isModerator(groupName, moderator))
            this.enqueuePrattleResponseMessage("You cannot reject this invitation since you are " +
                    "not a moderator of this group");
        else if (groupUsers.contains(userInvitee))
            this.enqueuePrattleResponseMessage("The user " + invitee + " is already a member of the group " + groupName);
        else if (invitation == null)
            this.enqueuePrattleResponseMessage("This invitation does not exist" + groupName);
        else if (invitation.isInvitationApproved())
            this.enqueuePrattleResponseMessage("You have already approved this invitation, you cannot reject it now.");
        else if (invitation.isInvitationDeleted())
            this.enqueuePrattleResponseMessage("This invitation was deleted by the sender, hence cannot be rejected");
        else if (invitationService.approveRejectInvitation(invitee, groupName, false))
            this.enqueuePrattleResponseMessage("The invitation was successfully rejected.");
        else
            this.enqueuePrattleResponseMessage("Unable to reject invitation.");
    }

    /**
     * Handle the update group message method
     *
     * @param msg the message sent by the client
     */
    private void handleUpdateGroupMessage(Message msg) {
        try {
            User user = userService.getUserByUserName(msg.getName());
            Group group = groupService.getGroup(msg.getTextOrPassword());
            if (group == null) {
                this.enqueuePrattleResponseMessage("This group does not exist yet.");
            } else if (groupService.isModerator(msg.getTextOrPassword(), user.getUserName())) {
                //User is allowed to make changes to this group
                //Split the string into key and value pair
                String[] keyValuePair = msg.getReceiverOrPassword().split(":");
                String attributeName = getGroupAttributeName(keyValuePair[0]);
                if (this.handleGroupUpdate(group.getGroupName(), attributeName, keyValuePair[1]))
                    this.enqueuePrattleResponseMessage("Group setting updated successfully");
                else
                    this.enqueuePrattleResponseMessage("Failed updating the group setting.");
            } else {
                this.enqueuePrattleResponseMessage("Sorry, you are not allowed to change settings for this group.");
            }
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleUpdateGroupMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE +
                    "group update syntax using HELP UPG");
        }
    }


    /**
     * Handles the updating of group setting based on particular attribute.
     *
     * @param groupName      the group name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the boolean   True if the attribute was successfully updated, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleGroupUpdate(String groupName, String attributeName, String attributeValue)
            throws SQLException {
        boolean result = false;
        if (attributeName.equals("is_searchable"))
            result = handleGroupUpdateForIsSearchable(groupName, attributeName, attributeValue);
        if (attributeName.equals("moderator_name"))
            result = handleGroupUpdateForModeratorChange(groupName, attributeName, attributeValue);
        return result;
    }

    /**
     * Handles the updating of group setting moderator_name attribute.
     *
     * @param groupName      the group name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value which can be a group member
     * @return the boolean   True if the attribute was successfully updated, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleGroupUpdateForModeratorChange(String groupName, String attributeName, String attributeValue)
            throws SQLException {
        boolean result = false;
        User nextModerator = userService.getUserByUserName(attributeValue);
        if (nextModerator == null) {
            this.enqueuePrattleResponseMessage("The user you provided does not exist");
        } else if (!groupService.checkMembershipInGroup(groupName, nextModerator.getUserName())) {
            this.enqueuePrattleResponseMessage("The user you are trying to make a moderator is not " +
                    "a member of the group");
        } else
            result = groupService.updateGroupSettings(groupName, attributeName, nextModerator.getUserName());
        return result;
    }

    /**
     * Handles the updating of group setting based on is_searchable attribute.
     *
     * @param groupName      the group name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value which can 0/1/true/false
     * @return the boolean   True if the attribute was successfully updated, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleGroupUpdateForIsSearchable(String groupName, String attributeName, String attributeValue)
            throws SQLException {
        boolean result = false;
        if (attributeValue.equals(Integer.toString(0)) || attributeValue.equalsIgnoreCase("false"))
            result = groupService.updateGroupSettings(groupName, attributeName, "0");
        else if (attributeValue.equals(Integer.toString(1)) || attributeValue.equalsIgnoreCase("true"))
            result = groupService.updateGroupSettings(groupName, attributeName, "1");
        else
            this.enqueuePrattleResponseMessage("Searchable values should be boolean (1/0 True/False)");

        return result;
    }

    /**
     * This method is used for mapping the group setting number sent in the update group message
     * to the group setting name that is present in the database. This is a separate function since
     * more group settings may get added later on.
     *
     * @param attributeNumber the number of the attribute
     * @return String          a String which is the name of the attribute as defined in the database
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private String getGroupAttributeName(String attributeNumber) throws SQLException {
        String attributeName;
        if (attributeNumber.equals("1"))
            attributeName = "is_searchable";
        else if (attributeNumber.equals("2"))
            attributeName = "moderator_name";
        else
            throw new SQLException("Group setting number out of bounds");
        return attributeName;
    }


    /**
     * Handles the search message type when encountered. Will call search method for users or groups
     * depending on the parameter passed by the user.
     *
     * @param msg The message sent by the user
     */
    private void handleSearchMessage(Message msg) {
        if (msg.getTextOrPassword().equalsIgnoreCase("user"))
            handleUserSearchMessage(msg.getReceiverOrPassword());
        else if (msg.getTextOrPassword().equalsIgnoreCase("group"))
            handleGroupSearchMessage(msg.getReceiverOrPassword());
        else
            this.enqueuePrattleResponseMessage("We support searching for users and groups only, please check " +
                    "the syntax for SRH using HELP SRH.");
    }


    /**
     * This method is used to handle the search functionality for when users are to be searched.
     *
     * @param searchString The string that is used for the regex to retrieve all similar users
     */
    private void handleUserSearchMessage(String searchString) {
        Map<String, String> resultantSet;
        try {
            resultantSet = userService.searchUser(searchString);
            if (resultantSet.isEmpty()) {
                this.enqueuePrattleResponseMessage("Sorry, did not find any matching records.");
                return;
            }
            helperForBuildingAndSendingSearchMessage(resultantSet, "User");
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleUserSearchMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE +
                    " using HELP SRH.");
        }
    }

    /**
     * Handle the search message when groups are supposed to be searched, given the search string
     *
     * @param searchString the string that is used by the regex to retrieve all the similar groups
     */
    private void handleGroupSearchMessage(String searchString) {
        Map<String, String> resultantSet;
        try {
            resultantSet = groupService.searchGroup(searchString);
            if (resultantSet.isEmpty()) {
                this.enqueuePrattleResponseMessage("Sorry, did not find any matching records.");
                return;
            }
            helperForBuildingAndSendingSearchMessage(resultantSet, "Group");
        } catch (Exception e) {
            ChatLogger.error("Error occurred - ClientRunnable.java - handleGroupSearchMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE +
                    " using HELP SRH.");
        }
    }

    /**
     * Helper function that will build a string to be sent to the client. This builds a string
     * with all the usernames and fullnames OR groupnames and their moderator usernames depending
     * on the user's request. The string is then enqueued to be sent to the client
     *
     * @param resultantSet the set containing the mapped values that is retrieved from the database
     */
    private void helperForBuildingAndSendingSearchMessage(Map<String, String> resultantSet, String userOrGroup) {
        StringBuilder workString = new StringBuilder();
        if (userOrGroup.equalsIgnoreCase("User"))
            workString.append(String.format("%n%-15s | %-15s %n", "Username::", "Full Name::"));
        else
            workString.append(String.format("%n%-15s | %-15s %n", "Group Name::", "Moderator Name::"));
        for (Map.Entry<String, String> pair : resultantSet.entrySet())
            workString.append(String.format("%-15s | %-15s %n", pair.getKey(), pair.getValue()));
        workString.append("Number of results : ");
        workString.append(resultantSet.size());
        String answerString = workString.toString();

        this.enqueuePrattleResponseMessage(answerString);
    }


    /**
     * Handle message of type delete group message.
     *
     * @param msg the message object
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDeleteGroupMessageMessage(Message msg) throws SQLException {
        // check if the sender of this message is actually the sender of the message that he wants to delete
        String msgSender = msg.getTextOrPassword().split("::")[0];
        if (msg.getName().equals(msgSender)) {
            if (conversationalMessagesService.deleteGroupMessage(msg.getTextOrPassword())) {
                this.enqueuePrattleResponseMessage("message with key: " + msg.getTextOrPassword() +
                        " deleted successfully.");
            } else {
                this.enqueuePrattleResponseMessage("error deleting message with key: " + msg.getTextOrPassword());
            }
        } else {
            this.enqueuePrattleResponseMessage("You do not have the permissions to delete this message");
        }
    }

    /**
     * Handle delete private message message.
     *
     * @param msg the msg
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleDeletePrivateMessageMessage(Message msg) throws SQLException {
        String messageSender = conversationalMessagesService.getSender(msg.getTextOrPassword());
        if (msg.getName().equals(messageSender)) {
            if (conversationalMessagesService.deleteMessage(msg.getTextOrPassword())) {
                this.enqueuePrattleResponseMessage("message with key: " + msg.getTextOrPassword() +
                        " deleted successfully.");
            } else {
                this.enqueuePrattleResponseMessage("error deleting message with key: " + msg.getTextOrPassword());
            }
        } else {
            this.enqueuePrattleResponseMessage("You do not have the permissions to delete this message");
        }
    }

    /**
     * Handle add group to group message.
     *
     * @param msg the msg object of type Add Group to Group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleAddGroupToGroupMessage(Message msg) throws SQLException {
        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
        Group guestGroup = groupService.getGroup(msg.getTextOrPassword());
        if (helperAddRemoveGroupToGroupMessage(currentUser, currentGroup, guestGroup)) {
            if (groupService.addGroupToGroup(currentGroup.getGroupName(), guestGroup.getGroupName())) {
                this.enqueuePrattleResponseMessage("Group was added successfully");
            } else {
                this.enqueuePrattleResponseMessage("Group was not added as the group was already there");
            }
        }
    }

    /**
     * Handle remove group from group message.
     *
     * @param msg the msg object of type Remove Group from Group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleRemoveGroupFromGroupMessage(Message msg) throws SQLException {
        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
        Group guestGroup = groupService.getGroup(msg.getTextOrPassword());
        if (helperAddRemoveGroupToGroupMessage(currentUser, currentGroup, guestGroup)) {
            if (groupService.removeGroupFromGroup(currentGroup.getGroupName(), guestGroup.getGroupName())) {
                this.enqueuePrattleResponseMessage("Group was removed successfully");
            } else {
                this.enqueuePrattleResponseMessage("Internal error occurred while removing the group. Group " +
                        "could not be removed");
            }
        }
    }

    /**
     * Handle leave group message.
     *
     * @param msg the msg object of type Remove Group from Group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleLeaveGroupMessage(Message msg) throws SQLException {
        User currentUser = userService.getUserByUserName(msg.getName());
        Group guestGroup = groupService.getGroup(msg.getTextOrPassword());
        try {
            if (guestGroup == null) {
                this.enqueuePrattleResponseMessage("The group you are trying to leave does not exist");
            } else if (guestGroup.getModeratorName().equalsIgnoreCase(currentUser.getUserName())) {
                this.enqueuePrattleResponseMessage("As you are the moderator of the group you cannot leave the group "
                        + "Please transfer the ownership to someone else and then leave");
            } else if (groupService.checkMembershipInGroup(guestGroup.getGroupName(), currentUser.getUserName())) {
                groupService.removeUserFromGroup(guestGroup.getGroupName(), currentUser.getUserName());
                this.enqueuePrattleResponseMessage("You successfully left the group.");
            } else {
                this.enqueuePrattleResponseMessage("You are not a member of the group you are trying to leave");
            }
        } catch (SQLException e) {
            ChatLogger.error("SQLException occurred - ClientRunnable.java - handleLeaveGroupMessage() : " + ChatLogger.getTrace(e));
            this.enqueuePrattleResponseMessage(CHECK_SYNTAX_ERROR_MESSAGE);
        }
    }

    /**
     * This function retrieves all the messages that a user has received and formats a giant string to be sent to the
     * user having all these messages.
     * It generates two separate strings, one for the group messages and one for the private messages and sends the
     * concatenated string to the user.
     *
     * @param msg the message object sent by the user
     */
    private void handleGetPastMessages(Message msg) {
        try {
            List<ConversationalMessage> msgs
                    = conversationalMessagesService.getMessagesForUser(msg.getName(), false);
            this.helperFormatAndEnqueueMessages(msgs);
        } catch (Exception e) {
            enqueuePrattleResponseMessage("Something went wrong while retrieving your messages, please try again");
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleGetConversationHistory() : " + ChatLogger.getTrace(e));
        }
    }


    /**
     * This function returns the conversation history of a user to the government, provided the request comes from the
     * government
     *
     * @param msg The message object sent by the government
     */
    private void handleGetConversationHistory(Message msg) {
        try {
            // Check if the message is from the government
            if (!msg.getName().equalsIgnoreCase(GOVERNMENT)) {
                enqueuePrattleResponseMessage("Sorry, you are not allowed to perform this operation");
            } else if (userService.getUserByUserName(msg.getTextOrPassword()) == null) {
                enqueuePrattleResponseMessage("This user does not exist in the system, please check for correct " +
                        "username with SRH");
            } else {
                List<ConversationalMessage> msgs = conversationalMessagesService.
                        getMessagesForUser(msg.getTextOrPassword(), false);
                this.helperFormatAndEnqueueMessages(msgs);
            }
        } catch (SQLException e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleGetConversationHistory() : " + ChatLogger.getTrace(e));
            enqueuePrattleResponseMessage("Looks like gremlins are at work, please try again.");
        }
    }

    /**
     * Helper method for formatting the message history and sending it to the appropriate user (user or government)
     *
     * @param msgs the list of conversational messages
     */
    private void helperFormatAndEnqueueMessages(List<ConversationalMessage> msgs) {
        List<String> conversations = helperFormatMessagesString(msgs);
        for (String s : conversations)
            enqueuePrattleResponseMessage(s);
    }

    /**
     * Helper method that will return all the formatted strings of a user's conversation history.
     * The return list is an attempt to enqueue separate messages to the user to reduce the load on the network.
     *
     * @param msgs The list of conversational message objects
     * @return List     This returns a list of two strings (one string contains the private chats, another string
     * returns the group messages for a user.
     */
    private List<String> helperFormatMessagesString(List<ConversationalMessage> msgs) {
        List<String> toSend = new ArrayList<>();
        StringBuilder workSpaceForPrivate = new StringBuilder();
        workSpaceForPrivate.append("\nAll their private messages::\n");
        workSpaceForPrivate.append(String.format("%n%-15s | %-20s | %-30s | %-15s%n", "Sender Username"
                , "Destination Username", "Message", "Message Key"));
        StringBuilder workSpaceForGroups = new StringBuilder();
        workSpaceForGroups.append("\n\nAll their group messages::\n");
        workSpaceForGroups.append(String.format("%n%-15s | %-15s | %-30s | %-15s%n", "Group Name", "Sender Username",
                "Message", "Message Key"));
        for (ConversationalMessage m : msgs) {
            if (m.getGroupUniqueKey() == null)
                workSpaceForPrivate.append(String.format(
                        "%n%-15s | %-20s | %-30s | %-15s",
                        m.getSourceName(),
                        m.getDestinationName(),
                        m.getMessageText(),
                        m.getMessageUniquekey()
                ));
            else
                workSpaceForGroups.append(String.format(
                        "%n%-15s | %-15s | %-30s | %-15s",
                        m.getGroupUniqueKey().split("::")[1],
                        m.getSourceName(),
                        m.getMessageText(),
                        m.getMessageUniquekey()
                ));
        }
        toSend.add(workSpaceForPrivate.toString());
        toSend.add(workSpaceForGroups.toString());
        return toSend;
    }


    /**
     * Method to tap a user of interest, provided the operation is requested by the government and
     * the user of interest is present in the system.
     *
     * @param msg the message object sent by the government.
     */
    private void handleTapUserMessage(Message msg) {
        try {
            if (!msg.getName().equalsIgnoreCase(GOVERNMENT))
                enqueuePrattleResponseMessage("Sorry, you are not allowed to do this operation");
            else if (userService.getUserByUserName(msg.getTextOrPassword()) == null)
                enqueuePrattleResponseMessage("This user does not exist, please check the username again.");
            else if (userService.tapUser(msg.getTextOrPassword()))
                enqueuePrattleResponseMessage(msg.getTextOrPassword() + " is now being tapped.");
            else
                enqueuePrattleResponseMessage("Couldn't tap the user, please try again.");
        } catch (Exception e) {
            ChatLogger.error("Exception occurred - ClientRunnable.java - handleTapUserMessage() : " + ChatLogger.getTrace(e));
            enqueuePrattleResponseMessage("Seems like gremlins are at work today, something went wrong, " +
                    "please try again.");
        }
    }

    /**
     * This method handles general messages
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleGeneralMessages(Message msg) throws SQLException {
        boolean result = false;
        if (msg.isBroadcastMessage()) {
            Prattle.broadcastMessage(msg);
            result = true;
        } else if (msg.isLoginMessage()) {
            handleLoginMessage(msg);
            result = true;
        } else if (msg.isRegisterMessage()) {
            handleRegisterMessage(msg);
            result = true;
        } else if (msg.isSearchMessage()) {
            handleSearchMessage(msg);
            result = true;
        }
        return result;
    }

    /**
     * This method handles communication messages
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleCommunicationMessages(Message msg) throws SQLException {
        boolean result = false;
        if (msg.isPrivateUserMessage()) {
            handlePrivateMessage(msg);
            result = true;
        } else if (msg.isPrivateReplyMessage()) {
            handlePrivateReplyMessage(msg);
            result = true;
        } else if (msg.isGroupMessage()) {
            handleGroupMessage(msg);
            result = true;
        } else if (msg.isDeletePrivateMessageMessage()) {
            handleDeletePrivateMessageMessage(msg);
            result = true;
        } else if (msg.isGetPastMessages()) {
            handleGetPastMessages(msg);
            result = true;
        } else if (msg.isGetConversationHistory()) {
            handleGetConversationHistory(msg);
            result = true;
        }
        return result;
    }

    /**
     * This method handles group messages
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleGroupMessages(Message msg) throws SQLException {
        boolean result = false;
        if (msg.isCreateGroupMessage()) {
            handleCreateGroupMessage(msg);
            result = true;
        } else if (msg.isDeleteGroupMessage()) {
            handleDeleteGroupMessage(msg);
            result = true;
        } else if (msg.isAddUserToGroupMessage()) {
            handleAddUserToGroupMessage(msg);
            result = true;
        } else if (msg.isRemoveUserFromGroupMessage()) {
            handleRemoveUserFromGroupMessage(msg);
            result = true;
        } else if (msg.isGetGroupMessage()) {
            handleGetGroupMessage(msg);
            result = true;
        } else if (msg.isUpdateGroupMessage()) {
            handleUpdateGroupMessage(msg);
            result = true;
        } else if (msg.isDeleteGroupMessageMessage()) {
            handleDeleteGroupMessageMessage(msg);
            result = true;
        } else if (msg.isAddGroupToGroupMessage()) {
            handleAddGroupToGroupMessage(msg);
            result = true;
        } else if (msg.isRemoveGroupFromGroupMessage()) {
            handleRemoveGroupFromGroupMessage(msg);
            result = true;
        } else if (msg.isLeaveGroupMessage()) {
            handleLeaveGroupMessage(msg);
            result = true;
        }
        return result;
    }

    /**
     * This method handles user messages
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleUserMessages(Message msg) throws SQLException {
        boolean result = false;
        if (msg.isUserProfileUpdateMessage()) {
            handleUserProfileUpdateMessage(msg);
            result = true;
        } else if (msg.isDeleteUserMessage()) {
            handleDeleteUserMessage(msg);
            result = true;
        } else if (msg.isFollowUserMessage()) {
            handleFollowUserMessage(msg);
            result = true;
        } else if (msg.isUnfollowUserMessage()) {
            handleUnfollowUserMessage(msg);
            result = true;
        } else if (msg.isGetFollowersMessage()) {
            handleGetFollowersMessage(msg);
            result = true;
        } else if (msg.isGetFolloweesMessage()) {
            handleGetFolloweesMessage(msg);
            result = true;
        } else if (msg.isGetOnlineUsersMessage()) {
            handleGetOnlineUserMessage(msg);
            result = true;
        } else if (msg.isTapUserMessage()) {
            handleTapUserMessage(msg);
            result = true;
        }
        return result;
    }

    /**
     * This method handles Invitation messages
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private boolean handleInvitationMessages(Message msg) throws SQLException {
        boolean result = false;
        if (msg.isCreateInvitationMessage()) {
            handleCreateInvitationMessage(msg);
            result = true;
        } else if (msg.isDeleteInvitationMessage()) {
            handleDeleteInvitationUserMessage(msg);
            result = true;
        } else if (msg.isAcceptInviteUserMessage()) {
            handleAcceptInvitationUserMessage(msg);
            result = true;
        } else if (msg.isDenyInviteUserMessage()) {
            handleDenyInvitationUserMessage(msg);
            result = true;
        } else if (msg.isApproveInviteModeratorMessage()) {
            handleApproveInvitationModeratorMessage(msg);
            result = true;
        } else if (msg.isRejectInviteModeratorMessage()) {
            handleRejectInvitationModeratorMessage(msg);
            result = true;
        }
        return result;
    }

    /**
     * This method handles different types of messages and delegates works to its respective methods
     *
     * @param msg The incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void handleMessageByType(Message msg) throws SQLException {
        // Check for our "special messages"
        boolean result;
        result = handleGeneralMessages(msg);
        result = result || handleGroupMessages(msg);
        result = result || handleUserMessages(msg);
        result = result || handleCommunicationMessages(msg);
        result = result || handleInvitationMessages(msg);

        if (!result) {
            ChatLogger.warning("Message not one of the required types " + msg);
        }
    }

    /**
     * This method handles the incoming message
     *
     * @param msg the incoming message
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    private void processMessage(Message msg) throws SQLException {
        // Check if the message is legal formatted
        if (messageChecks(msg)) {
            handleMessageByType(msg);
        } else {
            Message sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
                    "Last message was rejected because it specified an incorrect user name.");
            enqueueMessage(sendMsg);
        }
    }

    /**
     * Sends the enqueued messages to the printer and makes sure they were sent out.
     */
    private void handleOutgoingMessages() {
        // Check to make sure we have a client to send to.
        boolean keepAlive = true;
        if (!waitingList.isEmpty()) {
            keepAlive = false;
            // Send out all of the message that have been added to the queue.
            do {
                Message msg = waitingList.remove();
                boolean sentGood = sendMessage(msg);
                keepAlive |= sentGood;
                // Update the time until we terminate the client for inactivity.
                timer.updateAfterActivity();

            } while (!waitingList.isEmpty());
        }
        terminate |= !keepAlive;
    }

    /**
     * Store the object used by this client runnable to control when it is scheduled
     * for execution in the thread pool.
     *
     * @param future Instance controlling when the runnable is executed from within
     *               the thread pool.
     */
    void setFuture(ScheduledFuture<?> future) {
        runnableMe = future;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    private void terminateClient() throws SQLException {
        // Once the communication is done, close this connection.
        connection.close();

        // logout user if already logged in
        User currentUser = userService.getUserByUserName(this.getName());
        userClients.remove(this.getName());
        if (currentUser.isLoggedIn()) {
            boolean updated = userService.updateUserAttributes(
                    currentUser.getUserName(),
                    "logged_in",
                    "0"
            );
            if (!updated) {
                ChatLogger.error("LOGOUT: terminateClient: The profile details for " +
                        currentUser.getUserName() + " was not updated.");
            }
        }
        // Remove the client from our client listing.
        Prattle.removeClient(this);
        // And remove the client from our client pool.
        runnableMe.cancel(false);
    }

    /**
     * This method gets the client runnable instance based on the username provided
     *
     * @param username key on which the corresponding instance is to be found
     * @return ClientRunnable   null (if there no entry for the key) or the corresponding ClientRunnable instance
     */
    public static ClientRunnable getClientByUsername(String username) {
        return userClients.getOrDefault(username, null);
    }
}

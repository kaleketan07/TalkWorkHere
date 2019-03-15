package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.NetworkConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
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
    public ClientRunnable(NetworkConnection network) {
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
        } catch (ClassNotFoundException | SQLException | IOException e) {
            ChatLogger.error("Exception occurred : " + e);
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
     * @return True if message is correct; false otherwise
     */
    private boolean messageChecks(Message msg) {
        // Check that the message name matches.
        return (msg.getName() != null) && (msg.getName().compareToIgnoreCase(getName()) == 0);
    }

    /**
     * Immediately send this message to the client. This returns if we were
     * successful or not in our attempt to send the message.
     *
     * @param message Message to be sent immediately.
     * @return True if we sent the message successfully; false otherwise.
     */
    private boolean sendMessage(Message message) {
        ChatLogger.info("\t" + message);
        return connection.sendMessage(message);
    }

    /**
     * Try allowing this user to set his/her user name to the given username.
     *
     * @param userName The new value to which we will try to set userName.
     * @return True if the username is deemed acceptable; false otherwise
     */
    private boolean setUserName(String userName) {
        boolean result = false;
        // Now make sure this name is legal.
        if (userName != null) {
            if (userClients.getOrDefault(userName, null) == null) {
                // Optimistically set this users ID number.
                setName(userName);
                userId = hashCode();
                result = true;
                userClients.put(userName, this);
            } else {
                incrementInvalidCounter(invalidCounter);
                setName("invalid-" + userName + "-" + invalidCounter);
                userId = -1;
                result = true;
                ChatLogger.error("There is already a user with this username connected to the portal.");
            }
        } else {
            // Clear this name; we cannot use it. *sigh*
            userId = -1;
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
     * @return Returns the name of this client.
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
     * @return Returns the current value of userName.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Return if this thread has completed the initialization process with its
     * client and is read to receive messages.
     *
     * @return True if this thread's client should be considered; false otherwise.
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
                terminate = true;
            }
            if (terminate) {
                terminateClient();
            }
        } catch (SQLException e) {
            ChatLogger.error("SQL Exception occurred - run() : " + e);
        }
    }

    /**
     * Checks incoming messages and performs appropriate actions based on the type
     * of message.
     */
    protected void handleIncomingMessages() throws SQLException {
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
                } else if (msg.isLoginMessage() || msg.isRegisterMessage() || ( user!=null && user.isLoggedIn())) {
                    processMessage(msg);
                } else {
                    ChatLogger.error("User not logged in.");
                }
            }
        }
    }

    /**
     * Handles the register message
     *
     * @param msg - the incoming register message
     * @throws SQLException - the exception thrown by the database queries and calls
     */
    private void handleRegisterMessage(Message msg) throws SQLException {
        // Register the user after checking whether the user already exists or no
        User currentUser = userService.getUserByUserName(msg.getName());
        if (currentUser != null) {
            ChatLogger.error("Username already exists.");
        } else {
            // since the user was not found, a new user with this name may be created
            if (msg.getTextOrPassword().equals(msg.getReceiverOrPassword())) {
                userService.createUser(new User(null, null, msg.getName(), msg.getTextOrPassword(), true));
                ChatLogger.error("User" + msg.getName() +"registed");
            }
            else {
            	ChatLogger.error("Password and confirm password do not match.");
            }
            
        }
    }
    
    /**
     * Handles private message
     * 
     * @param msg the incoming message_user type of message
     * @throws SQLException thrown by the database queries and calls
     */
    private void handlePrivateMessage(Message msg) throws SQLException {
    	 User destUser = userService.getUserByUserName(msg.getReceiverOrPassword());
    	 if (destUser == null) {
    		 ChatLogger.error("Destination username does not exist.");
    	 }
    	 else {
    		 destUser.userSendMessage(msg);
    	 }
    }

    /**
     * Handles the login message
     *
     * @param msg - the incoming login message
     * @throws SQLException - thrown by the database queries and calls
     */
    private void handleLoginMessage(Message msg) throws SQLException {
        // Login the user after checking in the user with this username-password combo exists
        User currentUser = userService.getUserByUserNameAndPassword(msg.getName(), msg.getTextOrPassword());
        if (currentUser == null) {
            ChatLogger.error("Incorrect username or password.");
        } else {
            // since the user was found, set the loggedIn attribute to true in the database
            currentUser.setLoggedIn(true);
            boolean updated = userService.updateUser(currentUser);
            if (!updated) {
                ChatLogger.error("The profile details for " + currentUser.getUserName() + " was not updated.");
            }
        }
    }

    /**
     * Handles the createGroupMessage
     *
     * @param msg - the incoming createGroupMessage
     * @throws SQLException - thrown by the database queries and calls
     */
    private void handleCreateGroupMessage(Message msg) throws SQLException {
        // Create a group with the specified name with the sender as the moderator, if a group with the same name does not already exists
        Group existingGroup = groupService.getGroup(msg.getTextOrPassword());
        if (existingGroup != null) {
            ChatLogger.error("Groupname already exists! Please use a different group name.");
        } else {
            groupService.createGroup(msg.getTextOrPassword(), msg.getName());
        }
    }
    
    /**
     * Handles the GetGroupMessage
     *
     * @param msg - the incoming getGroup message
     * @throws SQLException - thrown by the database queries and calls
     */
    private void handleGetGroupMessage(Message msg) throws SQLException {
        // Create a group with the specified name with the sender as the moderator, if a group with the same name does not already exists
        Group existingGroup = groupService.getGroup(msg.getTextOrPassword());
        if (existingGroup == null) {
            ChatLogger.error("Groupname does not exist. So no details can be provided");
        } else {
        	//Not sure if this code will work so have commented just see if next statement is valid or no
        	//this.enqueue(msg);  HERE we might have to create a public makeGeneraMsg/MakePrattlemsg in Message class
        	ChatLogger.error("Groupname: " + existingGroup.getGroupName() + " Moderator: " + existingGroup.getModeratorName());
        }
    }

    /**
     * Handles the handleDeleteGroupMessage
     *
     * @param msg - the incoming login message
     * @throws SQLException - thrown by the database queries and calls
     */
    private void handleDeleteGroupMessage(Message msg) throws SQLException {
        // Delete the group after getting the valid moderator name and valid group name
        User currentUser = userService.getUserByUserName(msg.getName());
        Group currentGroup = groupService.getGroup(msg.getTextOrPassword());
        // if group does not exist
        if (currentGroup == null) {
            ChatLogger.error("Group does not exist.");
        } else {
            // if the user is in fact the moderator of the group only then delete the group
            if (groupService.isModerator(currentGroup.getGroupName(), currentUser.getUserName())) {
                groupService.deleteGroup(currentGroup.getGroupName());
            } else {
                ChatLogger.error("CurrentUser is not the moderator of the group.");
            }
        }
    }
    
    /**
     * Handles the handleDeleteUserMessage
     *
     * @param msg - the incoming delete user message
     * @throws SQLException - thrown by the database queries and calls
     */
    private void handleDeleteUserMessage(Message msg) throws SQLException {
        // Delete the user after getting the user
    	User currentUser = userService.getUserByUserName(msg.getName());
    	boolean result = userService.deleteUser(currentUser);
    	if (!result) {
    		ChatLogger.error("User could not deteled");
    	}
    	else {
    		this.terminate = true;
    	}
    }

    /**
     * Handle add user to group message.
     *
     * @param msg the msg
     * @throws SQLException the SQL exception
     */
    private void handleAddUserToGroupMessage(Message msg) throws SQLException {
    	User currentUser = userService.getUserByUserName(msg.getName());
    	Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
    	User guestUser = userService.getUserByUserName(msg.getTextOrPassword());
    	if (currentGroup == null) {
    		ChatLogger.error("The group you are trying to add to does not exist!");
    	}
    	else if (!currentGroup.getModeratorName().equals(currentUser.getUserName())) {
    		ChatLogger.error("You do not have the permissions to perform this operation");
    	} 
    	else if (guestUser == null) {
    		ChatLogger.error("The user you are trying to add does not exist");
    	}
    	else {
    		if(groupService.addUserToGroup(currentGroup.getGroupName(), guestUser.getUserName())) 
    		{
    			ChatLogger.error("User was added successfully");
    		}
    		else 
    		{
    			ChatLogger.error("user was not added as the user was already there");	
    		}
    		
    	}
    }    
    
    /**
     * Handle remove user from group message.
     *
     * @param msg the msg
     * @throws SQLException the SQL exception
     */
    private void handleRemoveUserFromGroupMessage(Message msg) throws SQLException {
    	User currentUser = userService.getUserByUserName(msg.getName());
    	Group currentGroup = groupService.getGroup(msg.getReceiverOrPassword());
    	User guestUser = userService.getUserByUserName(msg.getTextOrPassword());
    	if (currentGroup == null) {
    		ChatLogger.error("The group you are trying to add to does not exist!");
    	}
    	else if (!currentGroup.getModeratorName().equals(currentUser.getUserName())) {
    		ChatLogger.error("You do not have the permissions to perform this operation");
    	} 
    	else if (guestUser == null) {
    		ChatLogger.error("The user you are trying to add does not exist");
    	}
    	else {
    		if(groupService.removeUserFromGroup(currentGroup.getGroupName(), guestUser.getUserName())) 
    		{
    			ChatLogger.error("User was removed successfully");
    		}
    		else 
    		{
    			ChatLogger.error("user was not removed as the user was not in the group");	
    		}
    		
    	}
    }
    
    /**
     * Handle the update message sent by the user. This just updates the first name and
     * last name for the time being.
     *
     * @param msg The incoming user profile update message (for firstName and lastName only)
     * @throws SQLException thrown by wrong database queries
     */
    private void handleUserProfileUpdateMessage(Message msg) throws SQLException{
        if (userService.updateUserAttributes(msg.getName(), msg.getTextOrPassword(), msg.getReceiverOrPassword()))
            ChatLogger.info("User's first name and last name updated successfully");
        else
            ChatLogger.error("Failed updating database");
    }


    /**
     * This method handles different types of messages and delegates works to its respective methods
     *
     * @param msg - The incoming message
     * @throws SQLException - thrown by Database related queries and calls
     */
    private void handleMessageByType(Message msg) throws SQLException {
        // Check for our "special messages"
        if (msg.isBroadcastMessage()) {
            // Check for our "special messages"
            Prattle.broadcastMessage(msg);
        } else if (msg.isLoginMessage()) {
            handleLoginMessage(msg);
        } else if (msg.isRegisterMessage()) {
            handleRegisterMessage(msg);
        } else if (msg.isCreateGroupMessage()) {
            handleCreateGroupMessage(msg);
        } else if (msg.isDeleteGroupMessage()) {
            handleDeleteGroupMessage(msg);
        } else if(msg.isAddUserToGroupMessage()) {
        	handleAddUserToGroupMessage(msg);
        } else if(msg.isRemoveUserFromGroupMessage()) {
        	handleRemoveUserFromGroupMessage(msg);	
        } else if (msg.isPrivateUserMessage()) {
        	handlePrivateMessage(msg);
        } else if (msg.isGetGroupMessage()) {
        	handleGetGroupMessage(msg);
        } else if (msg.isUserProfileUpdateMessage()){
            handleUserProfileUpdateMessage(msg);
        } else if (msg.isDeleteUserMessage()) {
        	handleDeleteUserMessage(msg);
        } else {
            ChatLogger.warning("Message not one of the required types " + msg);
        }
    }

    /**
     * This method handles the incoming message
     *
     * @param msg the incoming message
     * @throws SQLException - Exception thrown from the database
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
    protected void handleOutgoingMessages() {
        // Check to make sure we have a client to send to.
        boolean keepAlive = true;
        if (!waitingList.isEmpty()) {
            keepAlive = false;
            // Send out all of the message that have been added to the
            // queue.
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
    public void setFuture(ScheduledFuture<?> future) {
        runnableMe = future;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    public void terminateClient() throws SQLException {
        // Once the communication is done, close this connection.
        connection.close();

        // logout user if already logged in
        User currentUser = userService.getUserByUserName(this.getName());
        userClients.remove(this.getName());
        if (currentUser.isLoggedIn()) {
            currentUser.setLoggedIn(false);
            boolean updated = userService.updateUser(currentUser);
            if (!updated) {
                ChatLogger.error("LOGOUT: terminateClient: The profile details for " + currentUser.getUserName() + " was not updated.");
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
     * @param username - key on which the corresponding instance is to be found
     * @return null (if there no entry for the key) or the corresponding ClientRunnable instance
     */
    public static ClientRunnable getClientByUsername(String username) {
        return userClients.getOrDefault(username, null);
    }
}

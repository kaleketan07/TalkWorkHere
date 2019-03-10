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
     *
     */
    private static Map<String,ClientRunnable> userClients = new HashMap<>();

    /**
     * This static counter is going to be used for making every "invalid"
     * connection unique, so we can add all invalid connections of a user to
     * the hashmap and send out error messages together
     */
    private static int invalidCounter = 0;

    /**
     * Used for incrementing the invalidCounter, defined a separate method here
     * on account of invalidCounter being a static field
     * @param invalidCounter the static counter
     */
    private static void incrementInvalidCounter(int invalidCounter) {
        ClientRunnable.invalidCounter = invalidCounter+1;
    }

    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param network NetworkConnection used by this new client
     */
    public ClientRunnable(NetworkConnection network)  {
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
        } catch(ClassNotFoundException | SQLException | IOException e) {
            ChatLogger.error("Exception occurred : " + e.getMessage());
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
            if(userClients.getOrDefault(userName, null) == null) {
                // Optimistically set this users ID number.
                setName(userName);
                userId = hashCode();
                result = true;
                userClients.put(userName, this);
            } else {
                incrementInvalidCounter(invalidCounter);
                setName("invalid-" + userName +"-"+ invalidCounter);
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
        } catch(SQLException e) {
            ChatLogger.error("SQL Exception occurred - run() : " + e);
        }
    }

    /**
     * Checks incoming messages and performs appropriate actions based on the type
     * of message.
     */
    protected void handleIncomingMessages() throws  SQLException{
        // Client has already been initialized, so we should first check
        // if there are any input
        // messages.
        Iterator<Message> messageIter = connection.iterator();
        if (messageIter.hasNext()) {
            // Get the next message
            Message msg = messageIter.next();
            // If the message is a broadcast message, send it out
            if (msg.terminate()) {
                // Stop sending the poor client message.
                terminate = true;
                // Reply with a quit message.
                enqueueMessage(Message.makeQuitMessage(name));
            } else {
                processMessage(msg);
            }
        }
    }

    private void processMessage(Message msg) throws  SQLException{
        // Check if the message is legal formatted
        if (messageChecks(msg)) {
            // Check for our "special messages"
            if (msg.isBroadcastMessage()) {
                // Check for our "special messages"
                Prattle.broadcastMessage(msg);
            }
            else if(msg.isLoginMessage()) {
                // Login the user after checking in the user with this username-password combo exists
                User currentUser = userService.getUserByUserNameAndPassword(msg.getName(), msg.getTextOrPassword());
                if(currentUser == null) {
                    ChatLogger.error("Incorrect username or password.");
                } else {
                    // since the user was found, set the loggedIn attribute to true in the database
                    currentUser.setLoggedIn(true);
                    boolean updated = userService.updateUser(currentUser);
                    if(!updated) {
                        ChatLogger.error("The profile details for " + currentUser.getUserName() + " was not updated.");
                    }
                }
            }else if(msg.isRegisterMessage()) {
                // Register the user after checking whether the user already exists or no
                User currentUser = userService.getUserByUserName(msg.getName());
                if(currentUser != null) {
                    ChatLogger.error("Username already exists.");
                } else {
                    // since the user was not found, a new user with this name may be created
                    if (msg.getTextOrPassword().equals(msg.getReceiverOrPassword())) {
                	userService.createUser(new User(null, null, msg.getName(), msg.getTextOrPassword(), true));
                    }
                }
            } else if (msg.isCreateGroupMessage()) {
            		// Create a group with the specified name with the sender as the moderator, if a group with the same name does not already exists
            		Group existingGroup = groupService.getGroup(msg.getTextOrPassword());
            		if (existingGroup != null) {
            			ChatLogger.error("Groupname already exists! Please use a different group name.");
            		} else {
            			User currentUser = userService.getUserByUserName(msg.getName());
                        if(currentUser == null) {
                            ChatLogger.error("Please log in to the system first!");
                        } 
            			groupService.createGroup(msg.getReceiverOrPassword(), msg.getName());
            		}
            } else {
                ChatLogger.warning("Message not one of the required types " + msg);
          }
        } else {
            Message sendMsg;
            sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
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
        if(currentUser == null) {
            ChatLogger.error("Incorrect username or password.");
        } else {
            userClients.remove(this.getName());
            if(currentUser.isLoggedIn()) {
                currentUser.setLoggedIn(false);
                boolean updated = userService.updateUser(currentUser);
                if(!updated) {
                    ChatLogger.error("LOGOUT: terminateClient: The profile details for " + currentUser.getUserName() + " was not updated.");
                }
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
        return userClients.getOrDefault(username,null);
    }
}
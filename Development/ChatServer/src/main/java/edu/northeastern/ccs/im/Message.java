package edu.northeastern.ccs.im;

/**
 * Each instance of this class represents a single transmission by our IM
 * clients.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class Message {

    /**
     * The string sent when a field is null.
     */
    private static final String NULL_OUTPUT = "--";

    /**
     * The handle of the message.
     */
    private MessageType msgType;

    /**
     * The first argument used in the message. This will be the sender's identifier.
     */
    private String msgSender;

    /**
     * The second argument used in the message.
     */
    private String msgTextOrPassword;
    /**
     * The third argument used in the message.
     */
    private String msgReceiverOrPassword;

    /**
     * Create a new message that contains actual IM text. The type of distribution
     * is defined by the handle and we must also set the name of the message sender,
     * message recipient, and the text to send.
     *
     * @param handle  Handle for the type of message being created.
     * @param srcName Name of the individual sending this message
     * @param text    Text of the instant message
     */
    private Message(MessageType handle, String srcName, String text) {
        msgType = handle;
        // Save the properly formatted identifier for the user sending the
        // message.
        msgSender = srcName;
        // Save the text of the message.
        msgTextOrPassword = text;
    }
    
    /**
     * Create a new message that contains actual IM text. The type of distribution
     * is defined by the handle and we must also set the name of the message sender,
     * message recipient, and the text to send.
     *
     * @param handle  Handle for the type of message being created.
     * @param srcName Name of the individual sending this message
     * @param text    Text of the instant message
     */
    private Message(MessageType handle, String srcName, String textorpassword, String receiverorPassword) {
        msgType = handle;
        // Save the properly formatted identifier for the user sending the
        // message.
        msgSender = srcName;
        // Save the text of the message.
        msgTextOrPassword = textorpassword;
        // Save the receiver or password
        msgReceiverOrPassword = receiverorPassword;
    }
    
    /**
     * Create a new message that contains a command sent the server that requires a
     * single argument. This message contains the given handle and the single
     * argument.
     *
     * @param handle  Handle for the type of message being created.
     * @param srcName Argument for the message; at present this is the name used to
     *                log-in to the IM server.
     */
    private Message(MessageType handle, String srcName) {
        this(handle, srcName, null);
    }

    /**
     * Create a new message to continue the logout process.
     *
     * @param myName The name of the client that sent the quit message.
     * @return Instance of Message that specifies the process is logging out.
     */
    public static Message makeQuitMessage(String myName) {
        return new Message(MessageType.QUIT, myName, null);
    }

    /**
     * Create a new message broadcasting an announcement to the world.
     *
     * @param myName Name of the sender of this very important missive.
     * @param text   Text of the message that will be sent to all users
     * @return Instance of Message that transmits text to all logged in users.
     */
    public static Message makeBroadcastMessage(String myName, String text) {
        return new Message(MessageType.BROADCAST, myName, text);
    }

    /**
     * Create a new message stating the name with which the user would like to
     * login.
     *
     * @param text Name the user wishes to use as their screen name.
     * @return Instance of Message that can be sent to the server to try and login.
     */
    protected static Message makeHelloMessage(String text) {
        return new Message(MessageType.HELLO, null, text);
    }

    /**
     * Given a handle, name and textOrPassword, return the appropriate message instance or an
     * instance from a subclass of message.
     *
     * @param handle  Handle of the message to be generated.
     * @param srcName Name of the originator of the message (may be null)
     * @param textOrPassword    Text sent in this message (may be null)
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword.
     */
    protected static Message makeMessage(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        Message result = null;
        if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
            result = makeQuitMessage(srcName);
        } else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
            result = makeSimpleLoginMessage(srcName);
        } else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
            result = makeBroadcastMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.LOGIN.toString()) == 0) {
            result = makeLoginMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.REGISTER.toString()) == 0) {
                result = makeRegisterMessage(srcName, textOrPassword, receiverOrPassword);    
        } else if (handle.compareTo(MessageType.DELETE_GROUP.toString()) == 0) {
            result = makeDeleteGroupMessage(srcName, textOrPassword);    
        } else if (handle.compareTo(MessageType.CREATE_GROUP.toString()) == 0) {
        	result = makeCreateGroupMessage(srcName, textOrPassword);  
        } else if (handle.compareTo(MessageType.ADD_USER_GROUP.toString()) == 0) {
        	result = makeAddUserToGroupMessage(srcName, textOrPassword, receiverOrPassword);
        }
        return result;
    }

    /**
     * Create a new message for the early stages when the user logs in without all
     * the special stuff.
     *
     * @param myName Name of the user who has just logged in.
     * @return Instance of Message specifying a new friend has just logged in.
     */
    public static Message makeSimpleLoginMessage(String myName) {
        return new Message(MessageType.HELLO, myName);
    }

    /**
     * This method creates a login message based on the given name and password
     * @param myName - username of the user requesting a login
     * @param password - password of the user used to validate the login
     * @return a Message object of type login
     */
    public static Message makeLoginMessage(String myName, String password) {
        return new Message(MessageType.LOGIN, myName, password);
    }
    
    /**
     * 
     * This method creates a register message based on the given name and password
     * @param myName - username of the user requesting a register
     * @param password - password of the user requesting a register
     * @return a Message object of type register
     */
    public static Message makeRegisterMessage(String myName, String password, String confirmPassword) {
        return new Message(MessageType.REGISTER, myName, password, confirmPassword);
    }
    /**
     * 
     * This method creates a delete group message based on the given group_name and moderator
     * @param userName - username of the user(moderator) requesting a register
     * @param groupName - groupName of the group to be deleted
     * @return a Message object of type delete_group
     */
    public static Message makeDeleteGroupMessage(String userName, String groupName) {
        return new Message(MessageType.DELETE_GROUP, userName, groupName);
    }
    
    /**
     * This method creates a message to be sent for creating a group.
     *
     * @param myName the name of the sender
     * @param groupName the desired group name for the new group
     * @return the Message object of type Create Group
     */
    public static Message makeCreateGroupMessage(String myName, String groupName) {
        return new Message(MessageType.CREATE_GROUP, myName, groupName);
    }
    
    
    /**
     * This method creates a message to add a user to group.
     *
     * @param myName the name of the sender
     * @param userName the user name that is to be added in the group
     * @param groupName the group name in which the user will be added
     * @return the message object with handle Add user to group
     */
    public static Message makeAddUserToGroupMessage(String myName, String userName, String groupName) {
    	return new Message(MessageType.ADD_USER_GROUP, myName, userName, groupName);
    }
    /**
     * Return the name of the sender of this message.
     *
     * @return String specifying the name of the message originator.
     */
    public String getName() {
        return msgSender;
    }

    /**
     * Return the text of this message.
     *
     * @return String equal to the text sent by this message.
     */
    public String getTextOrPassword() {
        return msgTextOrPassword;
    }
    
    /**
     * Return the text of this message.
     *
     * @return String equal to the receiver/Password sent by this message.
     */
    public String getReceiverOrPassword() {
        return msgReceiverOrPassword;
    }
    
    /**
     * Determine if this message is broadcasting text to everyone.
     *
     * @return True if the message is a broadcast message; false otherwise.
     */
    public boolean isBroadcastMessage() {
        return (msgType == MessageType.BROADCAST);
    }

    /**
     * Determine if this message is sent by a new client to log-in to the server.
     *
     * @return True if the message is an initialization message; false otherwise
     */
    public boolean isInitialization() {
        return (msgType == MessageType.HELLO);
    }

    /**
     * This method verifies if the current message has the handle LGN (is a login message)
     * @return true or false based on the comparison result
     */
    public boolean isLoginMessage() { return (msgType == MessageType.LOGIN);}

    /**
     * This method verifies if the current message has the handle REG (is a register message)
     * @return true or false based on the comparison result
     */
    public boolean isRegisterMessage() { return (msgType == MessageType.REGISTER);}
    
    /**
     * This method verifies if the current message has the handle DEG (is a delete_group message)
     * @return true or false based on the comparison result
     */
    public boolean isDeleteGroupMessage() { return (msgType == MessageType.DELETE_GROUP);}

    
    /**
     * This method verifies if the current message has the handle CRG (is a create group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isCreateGroupMessage() { return (msgType == MessageType.CREATE_GROUP);}
    
    /**
     * This method verifies if the current message has the handle ADG (is a add user to group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isAddUserToGroupMessage() {return (msgType == MessageType.ADD_USER_GROUP);}
    
    /**
     * Determine if this message is a message signing off from the IM server.
     *
     * @return True if the message is sent when signing off; false otherwise
     */
    public boolean terminate() {
        return (msgType == MessageType.QUIT);
    }

    /**
     * Representation of this message as a String. This begins with the message
     * handle and then contains the length (as an integer) and the value of the next
     * two arguments.
     *
     * @return Representation of this message as a String.
     */
    @Override
    public String toString() {
        String result = msgType.toString();
        if (msgSender != null) {
            result += " " + msgSender.length() + " " + msgSender;
        } else {
            result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
        }
        if (msgTextOrPassword != null) {
            result += " " + msgTextOrPassword.length() + " " + msgTextOrPassword;
        } else {
            result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
        }
        if (msgReceiverOrPassword != null) {
            result += " " + msgReceiverOrPassword.length() + " " + msgReceiverOrPassword;
        } else {
            result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
        }
        return result;
    }
}

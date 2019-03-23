package edu.northeastern.ccs.im;

import java.util.HashSet;
import java.util.Set;

import edu.northeastern.ccs.im.models.User;

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
     * The recipients of this message.
     */
    private Set<User> messageRecipients;

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
        // initialize an empty set of recipients
        messageRecipients = new HashSet<>();
    }

    /**
     * Create a new message that contains actual IM text. The type of distribution
     * is defined by the handle and we must also set the name of the message sender,
     * message recipient, and the text to send.
     *
     * @param handle             Handle for the type of message being created.
     * @param srcName            Name of the individual sending this message
     * @param textorpassword     the text or the password to be sent
     * @param receiverorPassword receiver or password
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
        // initialize an empty set of recipients
        messageRecipients = new HashSet<>();
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
     * Checks if this message has already been sent to the user.
     *
     * @param u the user object to be checked for being a recipient
     * @return true, if the user is already in the recipients else returns false
     */
    public boolean messageAlreadySent(User u) {
        return messageRecipients.contains(u);
    }

    /**
     * Adds the user to recipients.
     *
     * @param u the user object
     * @return true, if the user is added to the messageRecipients successfully else returns false
     */
    public boolean addUserToRecipients(User u) {
        return messageRecipients.add(u);
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
     * Given a handle, name and textOrPassword, return the appropriate General message instance or an
     * instance from a subclass of message.
     *
     * @param handle         Handle of the message to be generated.
     * @param srcName        Name of the originator of the message (may be null)
     * @param textOrPassword Text sent in this message (may be null)
     * @param receiverOrPassword The third parameter which can have different values based on the type of the message
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword. Null if none of the handles match.
     */
    private static Message handleMakeGeneralMessages(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
            return makeQuitMessage(srcName);
        } else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
            return makeSimpleLoginMessage(srcName);
        } else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
            return makeBroadcastMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.LOGIN.toString()) == 0) {
            return makeLoginMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.REGISTER.toString()) == 0) {
            return makeRegisterMessage(srcName, textOrPassword, receiverOrPassword);
        }
        return null;
    }

    /**
     * Given a handle, name and textOrPassword, return the appropriate Group message instance or an
     * instance from a subclass of message.
     *
     * @param handle         Handle of the message to be generated.
     * @param srcName        Name of the originator of the message (may be null)
     * @param textOrPassword Text sent in this message (may be null)
     * @param receiverOrPassword The third parameter which can have different values based on the type of the message
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword. Null if none of the handles match.
     */
    private static Message handleMakeGroupMessages(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        if (handle.compareTo(MessageType.DELETE_GROUP.toString()) == 0) {
            return makeDeleteGroupMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.CREATE_GROUP.toString()) == 0) {
            return makeCreateGroupMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.ADD_USER_GROUP.toString()) == 0) {
            return makeAddUserToGroupMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.GET_GROUP.toString()) == 0) {
            return makeGetGroupMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.REMOVE_USER_GROUP.toString()) == 0) {
            return makeRemoveUserFromGroupMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.UPDATE_GROUP.toString()) == 0) {
            return makeUpdateGroupMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.DELETE_GROUP_MESSAGE.toString()) == 0) {
        	return makeDeleteGroupMessageMessage(srcName, textOrPassword);
        }
        return null;
    }

    /**
     * Given a handle, name and textOrPassword, return the appropriate User message instance or an
     * instance from a subclass of message.
     *
     * @param handle         Handle of the message to be generated.
     * @param srcName        Name of the originator of the message (may be null)
     * @param textOrPassword Text sent in this message (may be null)
     * @param receiverOrPassword The third parameter which can have different values based on the type of the message
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword. Null if none of the handles match.
     */
    private static Message handleMakeUserMessages(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        if(handle.compareTo(MessageType.UPDATE_PROFILE_USER.toString()) == 0){
            return makeUserProfileUpdateMessage(srcName,textOrPassword,receiverOrPassword);
        } else if (handle.compareTo(MessageType.DELETE_USER.toString()) == 0) {
            return makeDeleteUserMessage(srcName);
        } else if (handle.compareTo(MessageType.FOLLOW_USER.toString()) == 0) {
            return makeFollowUserMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.UNFOLLOW_USER.toString()) == 0) {
            return makeUnfollowUserMessage(srcName, textOrPassword);
        } else if (handle.compareTo(MessageType.GET_FOLLOWERS.toString()) == 0) {
            return makeGetFollowersMessage(srcName);
        } else if (handle.compareTo(MessageType.GET_FOLLOWEES.toString()) == 0) {
            return makeGetFolloweesMessage(srcName);
        }
        return null;
    }

    /**
     * Given a handle, name and textOrPassword, return the appropriate Communication message instance or an
     * instance from a subclass of message.
     *
     * @param handle         Handle of the message to be generated.
     * @param srcName        Name of the originator of the message (may be null)
     * @param textOrPassword Text sent in this message (may be null)
     * @param receiverOrPassword The third parameter which can have different values based on the type of the message
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword. Null if none of the handles match.
     */
    private static Message handleMakeCommunicationMessages(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        if (handle.compareTo(MessageType.MESSAGE_USER.toString()) == 0) {
            return makePrivateUserMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.PRIVATE_REPLY_MESSAGE.toString()) == 0) {
            return makePrivateReplyMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.MESSAGE_GROUP.toString()) == 0) {
            return makeGroupMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.SEARCH_MESSAGE.toString()) == 0) {
            return makeSearchMessage(srcName, textOrPassword, receiverOrPassword);
        } else if (handle.compareTo(MessageType.DELETE_PRIVATE_MESSAGE.toString()) == 0) {
        	return makeDeletePrivateMessageMessage(srcName, textOrPassword);
        }
        return null;
    }

    /**
     * Given a handle, name and textOrPassword, return the appropriate message instance or an
     * instance from a subclass of message.
     *
     * @param handle         Handle of the message to be generated.
     * @param srcName        Name of the originator of the message (may be null)
     * @param textOrPassword Text sent in this message (may be null)
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & textOrPassword.
     */
    protected static Message makeMessage(String handle, String srcName, String textOrPassword, String receiverOrPassword) {
        Message result = null;
        result = handleMakeGeneralMessages(handle, srcName, textOrPassword, receiverOrPassword);
        result = (result == null)? handleMakeGroupMessages(handle, srcName, textOrPassword, receiverOrPassword):result;
        result = (result == null)? handleMakeUserMessages(handle, srcName, textOrPassword, receiverOrPassword):result;
        result = (result == null)? handleMakeCommunicationMessages(handle, srcName, textOrPassword, receiverOrPassword):result;
        return result;
    }

    /**
     * Creates a new message which updates the client with the responses from the prattle
     * server(error/success information)
     *
     * @param responseMessage text of the response message
     * @return a message with Prattle_Message Handle
     */
    public static Message makePrattleMessage(String responseMessage) {
        return new Message(MessageType.PRATTLE_MESSAGE, "Prattle", responseMessage);
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
     *
     * @param myName   - username of the user requesting a login
     * @param password - password of the user used to validate the login
     * @return a Message object of type login
     */
    public static Message makeLoginMessage(String myName, String password) {
        return new Message(MessageType.LOGIN, myName, password);
    }

    /**
     * This method creates a register message based on the given name and password
     *
     * @param myName   - username of the user requesting a register
     * @param password - password of the user requesting a register
     * @return a Message object of type register
     */
    public static Message makeRegisterMessage(String myName, String password, String confirmPassword) {
        return new Message(MessageType.REGISTER, myName, password, confirmPassword);
    }

    /**
     * This message creates a Private User message type of a message
     *
     * @param srcName  the username of the sender of the message
     * @param text     the text the sender wants to send
     * @param destName the username of the receiver of the message
     * @return
     */
    public static Message makePrivateUserMessage(String srcName, String text, String destName) {
        return new Message(MessageType.MESSAGE_USER, srcName, text, destName);
    }

    /**
     * This message creates a Delete User message type of a message
     *
     * @param srcName the username of the sender whoes profile needs to be deleted
     * @return Message of type DeleteUser Message
     */
    public static Message makeDeleteUserMessage(String srcName) {
        return new Message(MessageType.DELETE_USER, srcName);
    }

    /**
     * This method creates a delete group message based on the given group_name and moderator
     *
     * @param userName  - username of the user(moderator) requesting a register
     * @param groupName - groupName of the group to be deleted
     * @return a Message object of type delete_group
     */
    public static Message makeDeleteGroupMessage(String userName, String groupName) {
        return new Message(MessageType.DELETE_GROUP, userName, groupName);
    }

    /**
     * This method creates a message to be sent for creating a group.
     *
     * @param myName    the name of the sender
     * @param groupName the desired group name for the new group
     * @return the Message object of type Create Group
     */
    public static Message makeCreateGroupMessage(String myName, String groupName) {
        return new Message(MessageType.CREATE_GROUP, myName, groupName);
    }


    /**
     * This method creates a message to add a user to group.
     *
     * @param myName    the name of the sender
     * @param userName  the user name that is to be added in the group
     * @param groupName the group name in which the user will be added
     * @return the message object with handle Add user to group
     */
    public static Message makeAddUserToGroupMessage(String myName, String userName, String groupName) {
        return new Message(MessageType.ADD_USER_GROUP, myName, userName, groupName);
    }

    /**
     * This method creates a message to remove a user from group.
     *
     * @param myName    the name of the sender
     * @param userName  the user name that is to be removed from the group
     * @param groupName the group name in which the user will be removed form
     * @return the message object with handle delete user from group
     */
    public static Message makeRemoveUserFromGroupMessage(String myName, String userName, String groupName) {
        return new Message(MessageType.REMOVE_USER_GROUP, myName, userName, groupName);
    }


    /**
     * This method creates a get group message based on the given group name
     *
     * @param srcName   - srcName of the user asking for group details
     * @param groupName - groupName is the name of the group whose details are needed
     * @return a Message object of type login
     */
    public static Message makeGetGroupMessage(String srcName, String groupName) {
        return new Message(MessageType.GET_GROUP, srcName, groupName);
    }

    /**
     * The method creates a message that handles the user's request for updating
     * his profile details
     *
     * @param uname          the username of the user
     * @param attributeName  the attribute name (user profile attribute to be updated)
     * @param attributeValue the value of the attribute to be set
     * @return the message
     */
    public static Message makeUserProfileUpdateMessage(String uname, String attributeName, String attributeValue) {
        return new Message(MessageType.UPDATE_PROFILE_USER, uname, attributeName, attributeValue);
    }

    /**
     * This method creates a private reply message
     *
     * @param msgUniqueKey - msg_uniqueKey to which the the reply is
     * @param text         - text in the message
     * @return a Message object of type Private_Reply
     */
    public static Message makePrivateReplyMessage(String srcName, String text, String msgUniqueKey) {
        return new Message(MessageType.PRIVATE_REPLY_MESSAGE, srcName, text, msgUniqueKey);
    }

    /**
     * Make update group message message.
     *
     * @param userName          the username of the sender of the update message
     * @param groupName         the group name whose settings needs to be updated
     * @param attributeKeyValue the attribute key and it's value separated by ':'
     * @return the message object of type UPDATE_GROUP
     */
    public static Message makeUpdateGroupMessage(String userName, String groupName, String attributeKeyValue) {
        return new Message(MessageType.UPDATE_GROUP, userName, groupName, attributeKeyValue);
    }


    /**
     * This method creates a follow user message
     *
     * @param follower - String of the follower user name
     * @param followee - string of the followee user name
     * @return a Message object of type Follow User
     */
    public static Message makeFollowUserMessage(String follower, String followee) {
        return new Message(MessageType.FOLLOW_USER, follower, followee);
    }

    /**
     * This method creates a Get Followers message
     *
     * @param followee - string of the followee user name
     * @return a Message object of type Get Followers
     */
    public static Message makeGetFollowersMessage(String followee) {
        return new Message(MessageType.GET_FOLLOWERS, followee);
    }

    /**
     * This method creates a Get Followees message 
     *
     * @param followee - string of the follower user name
     * @return a Message object of type Get Followees
     */
    public static Message makeGetFolloweesMessage(String follower) {
        return new Message(MessageType.GET_FOLLOWEES, follower);
    }
    
    /**
     * This method creates a unfollow user message
     *
     * @param follower - String of the follower user name
     * @param followee - string of the followee user name
     * @return a Message object of type Unfollow User
     */
    public static Message makeUnfollowUserMessage(String follower, String followee) {
        return new Message(MessageType.UNFOLLOW_USER, follower, followee);
    }

    /**
     * Make search message message.
     *
     * @param userName     the user name of the user asking for the search results
     * @param userOrGroup  the User or Group keyword specifying the filter for search criteria
     * @param searchString the search string
     * @return the message object of type search_message
     */
    public static Message makeSearchMessage(String userName, String userOrGroup, String searchString) {
        return new Message(MessageType.SEARCH_MESSAGE, userName, userOrGroup, searchString);
    }

    /**
     * Add's the UniqueKey details to the existing message
     *
     * @param msg  The message to be sent
     * @param text The text to be replaced in msg object
     * @return msg
     */
    public static Message addUniqueKeyToMsg(Message msg, String text) {
        if (msg.isPrivateUserMessage() || msg.isPrivateReplyMessage()) {
            return new Message(msg.msgType, msg.msgSender, text, msg.msgReceiverOrPassword);
        }
        return msg;
    }

    /**
     * This method creates a Group message to be sent on the specified group
     *
     * @param srcName the name of the sender
     * @param text    the text content of the message
     * @param grpName the group name to which the message is to be sent
     * @return the message object of type Group Message
     */
    public static Message makeGroupMessage(String srcName, String text, String grpName) {
        return new Message(MessageType.MESSAGE_GROUP, srcName, text, grpName);
    }
    
    
    /**
     * Make a message for deleting private message.
     *
     * @param srcName the name of the sender
     * @param text the text representing the unique message key for the message to be deleted 
     * @return the message object
     */
    public static Message makeDeletePrivateMessageMessage(String srcName, String text) {
    	return new Message(MessageType.DELETE_PRIVATE_MESSAGE, srcName, text);
    }
    
    /**
     * Make a message for deleting a group message.
     *
     * @param srcName the name of the sender
     * @param text the text representing the unique group message key for the message to be deleted
     * @return the message object
     */
    public static Message makeDeleteGroupMessageMessage(String srcName, String text) {
    	return new Message(MessageType.DELETE_GROUP_MESSAGE, srcName, text);
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
     *
     * @return true or false based on the comparison result
     */
    public boolean isLoginMessage() {
        return (msgType == MessageType.LOGIN);
    }

    /**
     * This method verifies if the current message has the handle REG (is a register message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isRegisterMessage() {
        return (msgType == MessageType.REGISTER);
    }

    /**
     * This method verifies if the current message has the handle DEG (is a delete_group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isDeleteGroupMessage() {
        return (msgType == MessageType.DELETE_GROUP);
    }

    /**
     * This method verifies if the current message has the handle DLU (is a Delete_User message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isDeleteUserMessage() {
        return (msgType == MessageType.DELETE_USER);
    }

    /**
     * This method verifies if the current message has the handle CRG (is a create group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isCreateGroupMessage() {
        return (msgType == MessageType.CREATE_GROUP);
    }

    /**
     * This method verifies if the current message has the handle ADG (is a add user to group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isAddUserToGroupMessage() {
        return (msgType == MessageType.ADD_USER_GROUP);
    }

    /**
     * This method verifies if the current message has the handle ADG (is a add user to group message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isRemoveUserFromGroupMessage() {
        return (msgType == MessageType.REMOVE_USER_GROUP);
    }

    /**
     * This method verifies if the current message has the handle MSU (is a Message_User message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isPrivateUserMessage() {
        return (msgType == MessageType.MESSAGE_USER);
    }

    /**
     * This method verifies if the current message has the handle FWU (is a Follow_User message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isFollowUserMessage() {
        return (msgType == MessageType.FOLLOW_USER);
    }

    /**
     * This method verifies if the current message has the handle UFU (is a Unfollow_User message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isUnfollowUserMessage() {
        return (msgType == MessageType.UNFOLLOW_USER);
    }

    /**
     * This method verifies if the current message has the handle PRE (is a PRIVATE_REPLY_MESSAGE)
     *
     * @return true or false based on the comparison result
     */
    public boolean isPrivateReplyMessage() {
        return (msgType == MessageType.PRIVATE_REPLY_MESSAGE);
    }

    /**
     * This method verifies if the current message has the handle MSU (is a Message_User message)
     *
     * @return the boolean
     */
    public boolean isUserProfileUpdateMessage() {
        return (msgType == MessageType.UPDATE_PROFILE_USER);
    }

    /**
     * This method verifies if the current message has the handle GFR (is a Get_Followers message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isGetFollowersMessage() {
        return (msgType == MessageType.GET_FOLLOWERS);
    }
    
    /**
     * This method verifies if the current message has the handle GFE (is a Get_Followees message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isGetFolloweesMessage(){
        return (msgType == MessageType.GET_FOLLOWEES);
    }

    /**
     * This method verifies if the current message has the handle GTG (is a GET_GROUP message)
     *
     * @return true or false based on the comparison result
     */
    public boolean isGetGroupMessage() {
        return (msgType == MessageType.GET_GROUP);
    }

    /**
     * This method verifies if the current message has the handle MSG (is a group message)
     *
     * @return true if the message has the handle MSG else return false
     */
    public boolean isGroupMessage() {
        return (msgType == MessageType.MESSAGE_GROUP);
    }

    /**
     * Verify if the message is a update group message.
     *
     * @return true if the message is a update_group message, false otherwise
     */
    public boolean isUpdateGroupMessage() {
        return (msgType == MessageType.UPDATE_GROUP);
    }

    /**
     * Verify if the message Is a search message.
     *
     * @return the boolean, true if the message is a search message, false otherwise
     */

    public boolean isSearchMessage(){ return (msgType == MessageType.SEARCH_MESSAGE);}
    
    
    /**
     * Verify if the message is a message for deleting a private message
     *
     * @return true, if message is for deleting a private message 
     */
    public boolean isDeletePrivateMessageMessage() {return (msgType == MessageType.DELETE_PRIVATE_MESSAGE);}
    
    
    /**
     * Verify if the message is a message for deleting a group message
     *
     * @return true, if message is for deleting a group message
     */
    public boolean isDeleteGroupMessageMessage() {return (msgType == MessageType.DELETE_GROUP_MESSAGE);}
    
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

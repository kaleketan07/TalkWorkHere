package edu.northeastern.ccs.im;

/**
 * Enumeration for the different types of messages.
 *
 * @author Maria Jump
 */
public enum MessageType {
    /**
     * Message sent by the user attempting to login using a specified username.
     */
    HELLO("HLO"),
    /**
     * Message sent by the user to start the logging out process and sent by the
     * server once the logout process completes.
     */
    QUIT("BYE"),
    /**
     * Message whose contents is broadcast to all connected users.
     */
    BROADCAST("BCT"),
    /**
     * Message whose contents are to login a given user
     */
    LOGIN("LGN"),
    /**
     * Message whose contents are to register a given user
     */
    REGISTER("REG"),
    /**
     * Message whose contents are to delete a given group
     */
    DELETE_GROUP("DEG"),
    /**
     * Message whose contents are to create a group with the passed parameters
     */
    CREATE_GROUP("CRG"),
    /**
     * Message whose content is a groupname of the group of which details are needed
     */
	GET_GROUP("GTG"),
    /**
     * Message whose contents are to send a message from one user to another
     */
    MESSAGE_USER("MSU"),

    /**
     * Message whose contents are to add a user to a group with the passed parameters
     */
    ADD_USER_GROUP("AUG"),

    /**
     * Message whose contents are to update a user's profile
     */
    UPDATE_PROFILE_USER("UPU"),
    
    /**
     * Message to privately reply to a group Message
     */
    PRIVATE_REPLY_MESSAGE("PRE"),
    
	/**
    * Message whose contents are to delete a user profile
    */ 
    DELETE_USER("DLU"),
    
    /**
     * Message to get followers of the followee
     */
    GET_FOLLOWERS("GFR"),
    
	/**
    * Message whose contents are to delete a user profile from a group
    */ 
    REMOVE_USER_GROUP("RUG"),  
    /**
     * Message whose contents are to follow a user
     */
    FOLLOW_USER("FWU"),
    /**
     * Message whose contents are to unfollow a user
     */
    UNFOLLOW_USER("UFU"),
	/**
	 * Handle To send prattle messages to the client 
	 */
	PRATTLE_MESSAGE("PRM"),
	/**
	 * Handle To send prattle messages to the client 
	 */
	MESSAGE_GROUP("MSG"),

    /**
     * Handle to Update group settings.
     */
    UPDATE_GROUP("UPG"),

    /**
     * Handle to search a user or a group
     */
    SEARCH_MESSAGE("SRH"),
	
	/** 
	 * Handle to delete a private message.
	 */
	DELETE_PRIVATE_MESSAGE("DPM"),
	
	/** 
	 * Handle to delete a group message. 
	 */
	DELETE_GROUP_MESSAGE("DGM");
	
	/**
     * Store the short name of this message type.
     */
    private String abbreviation;

    /**
     * Define the message type and specify its short name.
     *
     * @param abbrev Short name of this message type, as a String.
     */
    private MessageType(String abbrev) {
        abbreviation = abbrev;
    }

    /**
     * Return a representation of this Message as a String.
     *
     * @return Three letter abbreviation for this type of message.
     */
    @Override
    public String toString() {
        return abbreviation;
    }
}

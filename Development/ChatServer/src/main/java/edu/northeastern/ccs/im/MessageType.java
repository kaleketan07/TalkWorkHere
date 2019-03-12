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
    MESSAGE_USER("MSU");
    
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

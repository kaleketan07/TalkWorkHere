package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
import java.util.List;

import edu.northeastern.ccs.im.models.ConversationalMessage;

public interface ConversationalMessageDAO {

    /**
     * Adding a record for a conversationalMessage to Database message table
     *
     * @param msgSource      Username of the source of the message
     * @param msgDestination Username of the Destination of the message
     * @param msgText        Text in the message
     * @param setFlag        marks if this message has been sent to the user or queued
     * @return String            UniqueKey for the particular message (msgSource + msgDestination + sqlTimestamp)
     * @throws SQLException the sql exception
     */
    String insertConversationalMessage(String msgSource, String msgDestination, String msgText, boolean setFlag) throws SQLException;

    /**
     * Retrieving a list of messages between a source and destination
     *
     * @param msgSource      Username of the source of the message
     * @param msgDestination Username of the Destination of the message
     * @throws SQLException the sql exception
     */
    List<ConversationalMessage> getMessagebySourceAndDestination(String msgSource, String msgDestination) throws SQLException;

    /**
     * Retrieving a list of messages from a given source
     *
     * @param msgSrc Username of the source of the message
     * @throws SQLException the sql exception
     */
    List<ConversationalMessage> getMessagebySource(String msgSrc) throws SQLException;

    /**
     * Retrieving a list of messages from a given destination
     *
     * @param msgDest Username of the destination of the message
     * @throws SQLException the sql exception
     */
    List<ConversationalMessage> getMessagebyDestination(String msgDest) throws SQLException;

    /**
     * Updating the delete flag of the given message
     *
     * @param msgUniqueKey UniqueKey for the message to be deleted
     * @throws SQLException the sql exception
     */
    boolean deleteMessage(String msgUniqueKey) throws SQLException;


    /**
     * Insert an entry to the group_messages table.
     *
     * @param uniqueGroupKey   the unique group key
     * @param uniqueMessageKey the unique message key
     * @return true if the message was inserted successfully else return false
     * @throws SQLException the SQL exception
     */
    boolean insertGroupConversationalMessage(String uniqueGroupKey, String uniqueMessageKey) throws SQLException;
    
    
    /**
     * Delete group message and the mappings from the group message table and all the messages from the .
     *
     * @param grpMsgUniqueKey the group message unique key
     * @return true, if successfully deleted else return false
     * @throws SQLException the SQL exception
     */
    boolean deleteGroupMessage(String grpMsgUniqueKey) throws SQLException; 
    

}

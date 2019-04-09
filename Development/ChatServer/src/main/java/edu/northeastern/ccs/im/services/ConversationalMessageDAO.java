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
     * @return String        Unique Key for the particular message (msgSource + msgDestination + sqlTimestamp)
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    String insertConversationalMessage(String msgSource, String msgDestination, String msgText, boolean setFlag) throws SQLException;

    /**
     * Retrieving a list of messages between a source and destination
     *
     * @param msgSource      Username of the source of the message
     * @param msgDestination Username of the Destination of the message
     * @return List          A list of conversational message objects
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    List<ConversationalMessage> getMessagebySourceAndDestination(String msgSource, String msgDestination) throws SQLException;

    /**
     * Retrieving a list of messages from a given source
     *
     * @param msgSrc        Username of the source of the message
     * @return List         A list of conversational message objects
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    List<ConversationalMessage> getMessagebySource(String msgSrc) throws SQLException;

    /**
     * Retrieving a list of messages from a given destination
     *
     * @param msgDest       Username of the destination of the message
     * @return List         A list of conversational message objects
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    List<ConversationalMessage> getMessagebyDestination(String msgDest) throws SQLException;

    /**
     * Updating the delete flag of the given message
     *
     * @param msgUniqueKey   UniqueKey for the message to be deleted
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean deleteMessage(String msgUniqueKey) throws SQLException;


    /**
     * Insert an entry to the group_messages table.
     *
     * @param uniqueGroupKey    the unique group key
     * @param uniqueMessageKey  the unique message key
     * @return boolean          true if the message was inserted successfully else return false
     * @throws SQLException     the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean insertGroupConversationalMessage(String uniqueGroupKey, String uniqueMessageKey) throws SQLException;


    /**
     * Delete group message and the mappings from the group message table and all the messages from the .
     *
     * @param   grpMsgUniqueKey     the group message unique key
     * @return  boolean             true, if successfully deleted else return false
     * @throws  SQLException        the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean deleteGroupMessage(String grpMsgUniqueKey) throws SQLException;

    /**
     * Gets all the messages for the user.
     *
     * @param userName      the user name for whom the messages are to be fetched
     * @param flag          for deciding whether this function should retrieve all unsent messages or all past messages
     *                      if true - then retrieve all messages that are not sent to the user
     *                      if false - then retrieve all past messages for the user
     * @return List         the unsent messages for user as Map with keys as the message objects and unique keys as the value
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    List<ConversationalMessage> getMessagesForUser(String userName, boolean flag) throws SQLException;

    /**
     * Marks a message with the provided uniqueKey as sent.
     *
     * @param msgUniqueKey     the unique key of the message to be marked sent
     * @return boolean         true, if successfully marked the message to be sent else return false
     * @throws SQLException    the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean markMessageAsSent(String msgUniqueKey) throws SQLException;
}

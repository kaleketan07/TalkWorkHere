package edu.northeastern.ccs.im.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.db.IDBConnection;
import edu.northeastern.ccs.im.models.ConversationalMessage;

/**
 * Class for services related to CoversationalMessages
 *
 * @author rahul
 */
public class ConversationalMessageService implements ConversationalMessageDAO {


    private IDBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils = null;
    private ResultSet result;
    private static ConversationalMessageService conversationalMessageService;
    private static final String DB_COL_MSG_SRC = "msg_src";
    private static final String DB_COL_MSG_DEST = "msg_dest";
    private static final String DB_COL_MSG_TEXT = "msg_text";
    private static final String DB_COL_MSG_TIMESTAMP = "msg_timestamp";
    private static final String DB_COL_MSG_UNIQUEKEY = "msg_uniquekey";
    private static final String GRP_COL_MSG_KEY = "message_unique_key";
    private static final String GRP_COL_GRP_KEY = "group_unique_key";

    /**
     * Instantiates an conversationalMessageService object for ConversationalMessageService. This constructor will initialize
     * and establish the connection to the database for the message table
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the sql exception
     */
    private ConversationalMessageService() throws ClassNotFoundException, SQLException, IOException {
        conn = new DBConnection();
        utils = new DBUtils();
        result = null;
    }

    /**
     * Getting the singleton instance of the class
     *
     * @return the conversational message service
     * @throws SQLException           the sql exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static ConversationalMessageService getInstance() throws SQLException, IOException, ClassNotFoundException {
        if (conversationalMessageService == null)
            conversationalMessageService = new ConversationalMessageService();
        return conversationalMessageService;
    }

    /**
     * Adding a record for a conversationalMessage to Database message table
     *
     * @param msgSource      Username of the source of the message
     * @param msgDestination Username of the Destination of the message
     * @param msgText        Text in the message
     * @param setFlag        Marks if this message has been sent to the user or queued
     * @return String        UniqueKey for the particular message (msgSource + msgDestination + sqlTimestamp)
     * @throws SQLException the sql exception
     */
    public String insertConversationalMessage(String msgSource, String msgDestination, String msgText, boolean setFlag)
            throws SQLException {
        final String CREATE_MESSAGE =
                "INSERT INTO messages (msg_src, msg_dest, msg_text, msg_timestamp, msg_uniquekey, msg_sent) " +
                        "VALUES (?,?,?,?,?,?)";
        pstmt = conn.getPreparedStatement(CREATE_MESSAGE);
        long time = System.currentTimeMillis();
        Timestamp sqlTimestamp = new Timestamp(time);
        String uniqueKey = msgSource + msgDestination + sqlTimestamp;
        pstmt = utils.setPreparedStatementArgs(pstmt, msgSource, msgDestination, msgText, sqlTimestamp, uniqueKey, setFlag);
        pstmt.executeUpdate();
        pstmt.close();
        return uniqueKey;
    }

    /**
     * Retrieving a list of messages between a source and destination
     *
     * @param msgSource      Username of the source of the message
     * @param msgDestination Username of the Destination of the message
     * @throws SQLException the sql exception
     */
    public List<ConversationalMessage> getMessagebySourceAndDestination(String msgSource, String msgDestination) throws SQLException {
        final String GET_MESSAGES_BETWEEN_SOURCE_DESTINATION = "SELECT * FROM messages WHERE msg_src = ? and msg_dest = ?";
        pstmt = conn.getPreparedStatement(GET_MESSAGES_BETWEEN_SOURCE_DESTINATION);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgSource, msgDestination);
        return getMessages(pstmt);
    }


    /**
     * Retrieving a list of messages from a given source
     *
     * @param msgSrc Username of the source of the message
     * @throws SQLException the sql exception
     */
    public List<ConversationalMessage> getMessagebySource(String msgSrc) throws SQLException {
        final String GET_MESSAGES_BY_SOURCE = "SELECT * FROM messages WHERE msg_src = ? ";
        pstmt = conn.getPreparedStatement(GET_MESSAGES_BY_SOURCE);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgSrc);
        return getMessages(pstmt);
    }

    /**
     * Retrieving a list of messages from a given destination
     *
     * @param msgDest Username of the destination of the message
     * @throws SQLException the sql exception
     */
    public List<ConversationalMessage> getMessagebyDestination(String msgDest) throws SQLException {
        final String GET_MESSAGES_BY_DESTINATION = "SELECT * FROM messages WHERE msg_dest= ? ";
        pstmt = conn.getPreparedStatement(GET_MESSAGES_BY_DESTINATION);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgDest);
        return getMessages(pstmt);
    }

    /**
     * Updating the delete flag of the given message
     *
     * @param msgUniqueKey UniqueKey for the message to be deleted
     * @throws SQLException the sql exception
     */
    public boolean deleteMessage(String msgUniqueKey) throws SQLException {
        final String UPDATE_DELETE_FLAG = "UPDATE messages SET msg_deleted = 1 WHERE msg_uniquekey = ?";
        pstmt = conn.getPreparedStatement(UPDATE_DELETE_FLAG);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgUniqueKey);
        int res = 0;
        try {
            res = pstmt.executeUpdate();
        } catch (Exception e) {
            throw new SQLException(e);
        }
        pstmt.close();
        return (res > 0);
    }


    /**
     * This is a helper methods to getMessages based on the preparedStatement provided
     *
     * @param pstmt - the prepared statement to be executed
     * @return the list of messages which satisfy the given condition in the preparedstatement
     * @throws SQLException - thrown by database query and calls.
     */
    private List<ConversationalMessage> getMessages(PreparedStatement pstmt) throws SQLException {
        List<ConversationalMessage> cm = new ArrayList<>();
        result = pstmt.executeQuery();
        while (result.next()) {
            String msgsrc = result.getString(DB_COL_MSG_SRC);
            String msgdest = result.getString(DB_COL_MSG_DEST);
            String msgtext = result.getString(DB_COL_MSG_TEXT);
            Timestamp msgtimestamp = result.getTimestamp(DB_COL_MSG_TIMESTAMP);
            String msguniquekey = result.getString(DB_COL_MSG_UNIQUEKEY);
            cm.add(new ConversationalMessage(msgsrc, msgdest, msgtext, msgtimestamp, msguniquekey));
        }
        pstmt.close();
        return cm;
    }

    /**
     * gets the username of the sender of the message based on the msg_uniquekey
     *
     * @param msgUniqueKey UniqueKey for the message to be deleted
     * @throws SQLException the sql exception
     */
    public String getSender(String msgUniqueKey) throws SQLException {
        final String GET_SENDER = "SELECT * FROM messages WHERE msg_uniquekey = ?";
        pstmt = conn.getPreparedStatement(GET_SENDER);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgUniqueKey);
        String msgSrc = null;
        result = pstmt.executeQuery();
        if (result.first()) {
            msgSrc = result.getString(DB_COL_MSG_SRC);
        }
        pstmt.close();
        return msgSrc;
    }

    /**
     * Insert an entry to the group_messages table.
     *
     * @param uniqueGroupKey   the unique group key
     * @param uniqueMessageKey the unique message key
     * @return true, if the message was inserted successfully else return false
     * @throws SQLException the SQL exception
     */
    public boolean insertGroupConversationalMessage(String uniqueGroupKey, String uniqueMessageKey) throws SQLException {
        final String ADD_MAPPING = "INSERT into group_messages (group_unique_key, message_unique_key) VALUES (?,?)";
        pstmt = conn.getPreparedStatement(ADD_MAPPING);
        pstmt = utils.setPreparedStatementArgs(pstmt, uniqueGroupKey, uniqueMessageKey);
        int res = pstmt.executeUpdate();
        pstmt.close();
        return (res > 0);
    }


    /**
     * Delete group message and the mappings from the group message table and all the messages from the .
     *
     * @param grpMsgUniqueKey the group message unique key
     * @return true, if successfully deleted else return false
     * @throws SQLException the SQL exception
     */
    public boolean deleteGroupMessage(String grpMsgUniqueKey) throws SQLException {
        // fetch all the message keys for this group key
        final String FETCH_MESSAGE_KEYS = "SELECT message_unique_key FROM prattle.group_messages WHERE group_unique_key = ?";
        pstmt = conn.getPreparedStatement(FETCH_MESSAGE_KEYS);
        pstmt = utils.setPreparedStatementArgs(pstmt, grpMsgUniqueKey);
        List<String> cm = new ArrayList<>();
        result = pstmt.executeQuery();
        while (result.next()) {
            String msguniquekey = result.getString(GRP_COL_MSG_KEY);
            cm.add(msguniquekey);
        }
        pstmt.close();
        // for all keys fetched above delete the message in messages table
        for (String key : cm) {
            if (!deleteMessage(key)) return false;
        }
        return true;
    }
    
    /**
     * Gets the unsent messages for the user.
     *
     * @param userName the user name for whom the messages are to be fetched
     * @param flag for deciding whether this function should retrieve all unsent messages or all past messages
     *             if true - then retrieve all messages that are not sent to the user
     *             if false - then retrieve all past messages for the user
     * @return the unsent messages for user as Map with keys as the message objects and unique keys as the value
     * @throws SQLException the SQL exception
     */
    public List<ConversationalMessage> getUnsentMessagesForUser(String userName, boolean flag) throws SQLException {
    	final String GET_MESSAGES;
        if(flag)
            GET_MESSAGES = "SELECT * FROM prattle.group_messages right outer join prattle.messages on prattle.group_messages.message_unique_key = prattle.messages.msg_uniquekey WHERE msg_dest = ? AND msg_deleted = 0 AND msg_sent = 0;";
        else
            GET_MESSAGES = "SELECT * FROM prattle.group_messages right outer join prattle.messages on prattle.group_messages.message_unique_key = prattle.messages.msg_uniquekey WHERE msg_dest = ? AND msg_deleted = 0;";
        pstmt = conn.getPreparedStatement(GET_MESSAGES);
        pstmt = utils.setPreparedStatementArgs(pstmt, userName);
    	List<ConversationalMessage> msgs = new ArrayList<>();
        result = pstmt.executeQuery();
        while (result.next()) {
        	String msgSrc = result.getString(DB_COL_MSG_SRC);
            String msgDest = result.getString(DB_COL_MSG_DEST);
            String msgText = result.getString(DB_COL_MSG_TEXT);
            String msgKey = result.getString(DB_COL_MSG_UNIQUEKEY);
            String grpMsgKey = result.getString(GRP_COL_GRP_KEY);
            ConversationalMessage msg = new ConversationalMessage(msgSrc, msgDest, msgText, null, msgKey);
        	if (grpMsgKey != null) {
            	msg.setGroupUniqueKey(grpMsgKey);
            }
        	msgs.add(msg);
         }
        pstmt.close();
    	return msgs;
    }
    
    /**
     * Marks a message with the provided uniqueKey as sent.
     *
     * @param msgUniqueKey the unique key of the message to be marked sent
     * @return true, if successfully marked the message to be sent else return false
     * @throws SQLException the SQL exception
     */
    public boolean markMessageAsSent(String msgUniqueKey) throws SQLException {
    	final String MARK_MSG_AS_SENT = "UPDATE prattle.messages set msg_sent = 1 WHERE msg_uniquekey = ?";
        pstmt = conn.getPreparedStatement(MARK_MSG_AS_SENT);
        pstmt = utils.setPreparedStatementArgs(pstmt, msgUniqueKey);
        int res = pstmt.executeUpdate();
        pstmt.close();
        return (res > 0);
    }

}

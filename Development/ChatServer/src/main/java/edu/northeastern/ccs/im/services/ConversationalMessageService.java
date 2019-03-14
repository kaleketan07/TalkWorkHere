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
import edu.northeastern.ccs.im.models.ConversationalMessage;

/**
 * Class for services related to CoversationalMessages
 *
 * @author rahul
 */
public class ConversationalMessageService {


    private DBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils = null;
    private ResultSet result;
    private static ConversationalMessageService conversationalMessageService;
    private static final String DB_COL_MSG_SRC = "msg_src";
    private static final String DB_COL_MSG_DEST = "msg_dest";
    private static final String DB_COL_MSG_TEXT = "msg_text";
    private static final String DB_COL_MSG_TIMESTAMP = "msg_timestamp";
    private static final String DB_COL_MSG_UNIQUEKEY = "msg_uniquekey";

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
     * @return
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
     * @return String            UniqueKey for the particular message (msgSource + msgDestination + sqlTimestamp)
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
        try {
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new SQLException(e);
        }
        pstmt.close();
        return true;
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
}
package edu.northeastern.ccs.im.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.models.ConversationalMessage;


public class TestConversationalMessageService {

    private ConversationalMessageService cs;

    /**
     * The Mocked db connection.
     */
    @Mock
    DBConnection mockedDBConnection;

    /**
     * The Mocked db utils.
     */
    @Mock
    DBUtils mockedDBUtils;

    /**
     * The Mocked prepared statement.
     */
    @Mock
    PreparedStatement mockedPreparedStatement;

    /**
     * The Mocked ResultSet
     */
    @Mock
    ResultSet mockedRS;


    /**
     * Initialise the mock objects and define their behaviors here.
     * This also sets the required reflected fields in the ConversationMessageService class with
     * the mocked objects.
     *
     * @throws SQLException the sql exception
     */
    @BeforeEach
    public void initMocks() throws SQLException, NoSuchFieldException, IllegalAccessException,
            IOException, ClassNotFoundException {
        MockitoAnnotations.initMocks(this);
        cs = ConversationalMessageService.getInstance();
        when(mockedDBConnection.getPreparedStatement(Mockito.anyString())).thenReturn(mockedPreparedStatement);
        when(mockedDBUtils.setPreparedStatementArgs(Mockito.any(PreparedStatement.class),
                Mockito.anyVararg()))
                .thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedRS);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockedRS.first()).thenReturn(true);
        when(mockedRS.getString("msg_src")).thenReturn("ABC");
        when(mockedRS.getString("msg_dest")).thenReturn("BCD");
        when(mockedRS.getString("msg_text")).thenReturn("AB");
        when(mockedRS.getString("msg_uniquekey")).thenReturn("ABCBCD2018:05:05");
        when(mockedRS.getTimestamp("msg_timestamp")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockedRS.next()).thenReturn(true, false);
        Field rs = ConversationalMessageService.class.getDeclaredField("result");
        rs.setAccessible(true);
        rs.set(cs, mockedRS);
        Field ps = ConversationalMessageService.class.getDeclaredField("pstmt");
        ps.setAccessible(true);
        ps.set(cs, mockedPreparedStatement);
        Field db = ConversationalMessageService.class.getDeclaredField("conn");
        db.setAccessible(true);
        db.set(cs, mockedDBConnection);
        Field ut = ConversationalMessageService.class.getDeclaredField("utils");
        ut.setAccessible(true);
        ut.set(cs, mockedDBUtils);
    }

    /**
     * Tear down. Set all mocks to null
     */
    @AfterEach
    public void tearDown() {
        mockedDBConnection = null;
        mockedDBUtils = null;
        mockedPreparedStatement = null;
        mockedRS = null;
    }

    /**
     * Test getSender() method 
     * @throws SQLException 
     */
    @Test
    public void testGetSender() throws SQLException {
    	String ret = cs.getSender("ABC");
        assertTrue(ret.equals("ABC"));
    }
    

    /**
     * Test getSender() method  with invalid user
     * @throws SQLException 
     */
    @Test
    public void testGetSenderInvalid() throws SQLException {
    	when(mockedRS.first()).thenReturn(false);
        String ret = cs.getSender("ABC");
    	assertNull(ret);
    }
    
    
    
    /**
     * Test for inserConversationalMessage using src_name, dest_name, msg_text
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testInsertConversationalMessage() throws SQLException {
        String ret = cs.insertConversationalMessage("ABC", "BCD", "hello", false);
        assertTrue(ret.contains("ABCBCD"));
    }

    /**
     * Test getMessagebyDestination() by retrieving the uniqueKey of the message
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetMessagebyDestination() throws SQLException {
        List<ConversationalMessage> ret = cs.getMessagebyDestination("ABC");
        Optional<ConversationalMessage> firstMsg = ret.stream().findFirst();
        String msguniqueKey = firstMsg.toString();
        assertTrue(msguniqueKey.contains("ABCBCD2018:05:05"));
    }

    /**
     * Test getMessagebySource() by retrieving the uniqueKey of the message
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetMessagebySource() throws SQLException {
        List<ConversationalMessage> ret = cs.getMessagebySource("ABC");
        Optional<ConversationalMessage> firstMsg = ret.stream().findFirst();
        String msguniqueKey = firstMsg.toString();
        assertTrue(msguniqueKey.contains("ABCBCD2018:05:05"));
    }

    /**
     * Test getMessagebySourceAndDestination() by retrieving the uniqueKey of the message
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetMessagebySourceAndDestination() throws SQLException {
        List<ConversationalMessage> ret = cs.getMessagebySourceAndDestination("ABC", "BCD");
        Optional<ConversationalMessage> firstMsg = ret.stream().findFirst();
        String msguniqueKey = firstMsg.toString();
        assertTrue(msguniqueKey.contains("ABCBCD2018:05:05"));
    }

    /**
     * Test updateMessageDeleteFlag() by deleting a message
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateMessageDeleteFlag() throws SQLException {
        assertTrue(cs.deleteMessage("ABCBCD2018:05:05"));
    }

    /**
     * Test getMessagebyDestination when SQLException is thrown.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testMessagebyDestination() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> cs.getMessagebyDestination("BCD"));
    }

    /**
     * Test getMessagebySource when SQLException is thrown.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testMessagebySource() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> cs.getMessagebySource("ABC"));
    }

    /**
     * Test getMessagebySourceandDestination when SQLException is thrown.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testMessagebySourceandDestination() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> cs.getMessagebySourceAndDestination("ABC", "BCD"));
    }

    /**
     * Test deleteMessage when SQLException is thrown.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testDeleteMessage() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, () -> cs.deleteMessage("ABCBCD2018:05:05"));
    }
    
    /**
     * Test insert group conversational message for true.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testInsertGroupConversationalMessageForTrue() throws SQLException{
    	when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(cs.insertGroupConversationalMessage("ABC","BCD"));
    }
    
    /**
     * Test insert group conversational message for false.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testInsertGroupConversationalMessageForFalse() throws SQLException{
    	when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(cs.insertGroupConversationalMessage("ABC","BCD"));
    }

}

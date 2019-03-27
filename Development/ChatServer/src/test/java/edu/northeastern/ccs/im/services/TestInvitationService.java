package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.IDBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the invitation service methods
 *
 * @author Sachin Haldavanekar
 */
public class TestInvitationService {

    /**
     * Initialise the mock objects and define their behaviors here.
     * This also sets the required reflected fields in the InvitationService class with
     * the mocked objects.
     *
     * @throws SQLException           the sql exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    @BeforeEach
    public void setup() throws SQLException, NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
        invitationService = InvitationService.getInstance();

        dBConnectionMock = Mockito.mock(DBConnection.class);
        Field db = InvitationService.class.getDeclaredField("connection");
        db.setAccessible(true);
        db.set(invitationService, dBConnectionMock);
        preparedStatementMock = Mockito.mock(PreparedStatement.class);
        resultSetMock = Mockito.mock(ResultSet.class);

        when(dBConnectionMock.getPreparedStatement(anyString())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
    }

    /**
     * Tear down. Set all mocks to null
     */
    @AfterEach
    public void tearDown() {
        dBConnectionMock = null;
        preparedStatementMock = null;
        resultSetMock = null;
    }

    /**
     * Tests the getInstance method if it returns an
     * InvitationService instance.
     */
    @Test
    public void testGetInstance() throws ClassNotFoundException, IOException, SQLException {
        Object instance = InvitationService.getInstance();
        assertTrue(instance instanceof InvitationService);
    }

    /**
     * Test the getInvitation method when no match is found for two input overloaded version
     */
    @Test
    public void testGetInvitationTwoParamsNoMatchFound() throws SQLException {
        when(resultSetMock.first()).thenReturn(false);
        assertNull(invitationService.getInvitation(INVITEE, GROUP_NAME));
    }

    /**
     * Test the getInvitation method when match is found for two input overloaded version
     */
    @Test
    public void testGetInvitationTwoParamsMatchFound() throws SQLException {
        when(resultSetMock.first()).thenReturn(true);
        when(resultSetMock.getBoolean(anyString())).thenReturn(true, false, true, false, false);
        when(resultSetMock.getString(anyString())).thenReturn(INVITER);

        Message msg = invitationService.getInvitation(INVITEE, GROUP_NAME);
        assertTrue(msg.isInvitationAccepted());
        assertFalse(msg.isInvitationDenied());
        assertTrue(msg.isInvitationApproved());
        assertFalse(msg.isInvitationRejected());
        assertFalse(msg.isInvitationDeleted());
        assertEquals(INVITER, msg.getName());
        assertEquals(INVITEE, msg.getTextOrPassword());
        assertEquals(GROUP_NAME, msg.getReceiverOrPassword());
    }

    /**
     *  Test get Invitations for Invitee when no record is found
     */
    @Test
    public void testGetInvitationsForInviteeNull() throws SQLException {
        when(resultSetMock.next()).thenReturn(false);
        assertTrue(invitationService.getInvitationsForInvitee(INVITEE).isEmpty());
    }

    /**
     * Test the getInvitationForInvitee method when match is found
     */
    @Test
    public void testGetInvitationsForInviteeNotNull() throws SQLException {
        when(resultSetMock.next()).thenReturn(true, false);
        when(resultSetMock.getBoolean(anyString())).thenReturn(true, false, true, false, false);
        when(resultSetMock.getString(anyString())).thenReturn(INVITER);

        Set<Message> msgs = invitationService.getInvitationsForInvitee(INVITEE);
        assertTrue(msgs.size() == 1);
    }

    /**
     * Test the getInvitation method when no match is found for three input overloaded version
     */
    @Test
    public void testGetInvitationThreeParamsNoMatchFound() throws SQLException {
        when(resultSetMock.first()).thenReturn(false);
        assertNull(invitationService.getInvitation(INVITER, INVITEE, GROUP_NAME));
    }

    /**
     * Test the getInvitation method when match is found for three input overloaded version
     */
    @Test
    public void testGetInvitationThreeParamsMatchFound() throws SQLException {
        when(resultSetMock.first()).thenReturn(true);
        when(resultSetMock.getBoolean(anyString())).thenReturn(true, false, true, false, false);
        when(resultSetMock.getString(anyString())).thenReturn(INVITER);

        Message msg = invitationService.getInvitation(INVITER, INVITEE, GROUP_NAME);
        assertTrue(msg.isInvitationAccepted());
        assertFalse(msg.isInvitationDenied());
        assertTrue(msg.isInvitationApproved());
        assertFalse(msg.isInvitationRejected());
        assertFalse(msg.isInvitationDeleted());
        assertEquals(INVITER, msg.getName());
        assertEquals(INVITEE, msg.getTextOrPassword());
        assertEquals(GROUP_NAME, msg.getReceiverOrPassword());
    }

    /**
     * Test Approve Reject Invitation for the Approve scenario
     * where the DB update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testApproveInvitationUpdateSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.approveRejectInvitation(INVITEE, GROUP_NAME, true));
    }

    /**
     * Test Approve Reject Invitation for the Approve scenario
     * where the Db update in unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testApproveInvitationUpdateFail() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.approveRejectInvitation(INVITEE, GROUP_NAME, true));
    }

    /**
     * Test Approve Reject Invitation for the Reject scenario
     * where the DB update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testRejectInvitationUpdateSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.approveRejectInvitation(INVITEE, GROUP_NAME, false));
    }

    /**
     * Test Approve Reject Invitation for the Reject scenario
     * where the Db update in unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testRejectInvitationUpdateFail() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.approveRejectInvitation(INVITEE, GROUP_NAME, false));
    }

    /**
     * Test Accept Deny Invitation for the Accept scenario
     * where the DB update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testAcceptInvitationUpdateSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.acceptDenyInvitation(INVITEE, GROUP_NAME, true));
    }

    /**
     * Test Accept Deny Invitation for the Accept scenario
     * where the Db update in unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testAcceptInvitationUpdateFail() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.acceptDenyInvitation(INVITEE, GROUP_NAME, true));
    }

    /**
     * Test Accept Deny Invitation for the Deny scenario
     * where the DB update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testDenyInvitationUpdateSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.acceptDenyInvitation(INVITEE, GROUP_NAME, false));
    }

    /**
     * Test Accept Deny Invitation for the Deny scenario
     * where the Db update in unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testDenyInvitationUpdateFail() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.acceptDenyInvitation(INVITEE, GROUP_NAME, false));
    }

    /**
     * Test create invitation method when the update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testCreateInvitationSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.createInvitation(INVITER, INVITEE, GROUP_NAME));
    }

    /**
     * Test create invitation method when the update is unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testCreateInvitationUnsuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.createInvitation(INVITER, INVITEE, GROUP_NAME));
    }

    /**
     * Test delete invitation method when the update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testDeleteInvitationSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.deleteInvitation(INVITER, INVITEE, GROUP_NAME));
    }

    /**
     * Test delete invitation method when the update is unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testDeleteInvitationUnsuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.deleteInvitation(INVITER, INVITEE, GROUP_NAME));
    }

    /**
     * Test set invitation sent method when the update is successful
     *
     * @throws SQLException
     */
    @Test
    public void testSetInvitationSentSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        assertTrue(invitationService.setInvitationIsSentToInvitee(INVITEE, GROUP_NAME));
    }

    /**
     * Test set invitation sent method when the update is unsuccessful
     *
     * @throws SQLException
     */
    @Test
    public void testSetInvitationSentUnsuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0);
        assertFalse(invitationService.setInvitationIsSentToInvitee(INVITEE, GROUP_NAME));
    }

    /**
     * Constants used as arguments in the tests.
     */
    private static final String INVITER = "inviter";
    private static final String INVITEE = "invitee";
    private static final String GROUP_NAME = "groupName";

    /**
     * All the mocks used in the tests.
     */
    private IDBConnection dBConnectionMock;
    private PreparedStatement preparedStatementMock;
    private ResultSet resultSetMock;
    /**
     * Invitation service instance used to test the methods
     */
    private InvitationService invitationService;
}

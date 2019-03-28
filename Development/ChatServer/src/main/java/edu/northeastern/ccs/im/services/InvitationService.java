package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines the Invitation Service which facilitates the
 * sending, approval and acceptance of invitations in the slack app.
 *
 * @author Sachin
 * @version 1.0
 */
public class InvitationService implements InvitationDao {

    /**
     * data members of the class used in multiple methods of the service
     */
    private DBConnection connection;
    private DBUtils utils;
    private PreparedStatement preparedStatement;
    private ResultSet result;
    private static InvitationService invitationServiceInstance;

    /**
     * Constants used in multiple methods of the service
     */
    private static final String INVITER = "inviter";
    private static final String INVITEE = "invitee";
    private static final String GROUP_NAME = "group_name";
    private static final String IS_APPROVED = "is_approved";
    private static final String IS_REJECTED = "is_rejected";
    private static final String IS_ACCEPTED = "is_accepted";
    private static final String IS_DENIED = "is_denied";
    private static final String IS_DELETED = "is_deleted";

    /**
     * Instantiates a new invitation service.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the SQL exception is thrown due to some query or database interaction
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    private InvitationService() throws ClassNotFoundException, SQLException, IOException {
        connection = new DBConnection();
        utils = new DBUtils();
    }

    /**
     * Gets the singleton invitation service instance.
     *
     * @return the invitation service instance
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the SQL exception is thrown due to some query or database interaction
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    public static InvitationService getInstance() throws ClassNotFoundException, SQLException, IOException {
        if (invitationServiceInstance == null) {
            invitationServiceInstance = new InvitationService();
        }
        return invitationServiceInstance;
    }

    /**
     * The implementation for the interface method for getting an invitation
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return the message retrieved or null if not record is found
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public Message getInvitation(String inviter, String invitee, String groupName) throws SQLException {
        final String QUERY = "SELECT * from group_invitation where inviter = ? and invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, inviter, invitee, groupName);
        result = preparedStatement.executeQuery();
        Message message = null;
        if (result.first()) {
            boolean isAccepted = result.getBoolean(IS_ACCEPTED);
            boolean isDenied = result.getBoolean(IS_DENIED);
            boolean isApproved = result.getBoolean(IS_APPROVED);
            boolean isRejected = result.getBoolean(IS_REJECTED);
            boolean isDeleted = result.getBoolean(IS_DELETED);
            message = Message.makeCreateInvitationMessage(inviter, invitee, groupName);
            message.setInvitationAccepted(isAccepted);
            message.setInvitationDenied(isDenied);
            message.setInvitationApproved(isApproved);
            message.setInvitationRejected(isRejected);
            message.setInvitationDeleted(isDeleted);
        }
        preparedStatement.close();
        return message;
    }

    /**
     * The implementation for the interface method for getting an invitation
     *
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return the message retrieved or null if not record is found
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public Message getInvitation(String invitee, String groupName) throws SQLException {
        final String QUERY = "SELECT * from group_invitation where invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, invitee, groupName);
        result = preparedStatement.executeQuery();
        Message message = null;
        if (result.first()) {
            boolean isAccepted = result.getBoolean(IS_ACCEPTED);
            boolean isDenied = result.getBoolean(IS_DENIED);
            boolean isApproved = result.getBoolean(IS_APPROVED);
            boolean isRejected = result.getBoolean(IS_REJECTED);
            boolean isDeleted = result.getBoolean(IS_DELETED);
            String inviter = result.getString(INVITER);
            message = Message.makeCreateInvitationMessage(inviter, invitee, groupName);
            message.setInvitationAccepted(isAccepted);
            message.setInvitationDenied(isDenied);
            message.setInvitationApproved(isApproved);
            message.setInvitationRejected(isRejected);
            message.setInvitationDeleted(isDeleted);
        }
        preparedStatement.close();
        return message;
    }

    /**
     * The implementation for the interface method for approving or rejecting an invitation
     *
     * @param invitee   - The person who is invited
     * @param groupName - The group for which the invite is sent
     * @param approved  - The flag denoting true for approval and false for rejection
     * @return true if the update was successful, false if not
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public boolean approveRejectInvitation(String invitee, String groupName, boolean approved) throws SQLException {
        final String QUERY = "UPDATE group_invitation SET is_approved = ?, is_rejected = ? WHERE invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, approved, !approved, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }

    /**
     * The implementation for the interface method for accepting or denying an invitation
     *
     * @param invitee   - The person who is invited
     * @param groupName - The group for which the invite is sent
     * @param accepted  - The flag denoting true for acceptance and false for denial
     * @return true if the update was successful, false if not
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public boolean acceptDenyInvitation(String invitee, String groupName, boolean accepted) throws SQLException {
        final String QUERY = "UPDATE group_invitation SET is_accepted = ?, is_denied = ? WHERE invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, accepted, !accepted, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }

    /**
     * The implementation for the interface method for creating an invitation
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return true if the update was successful, false if not
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public boolean createInvitation(String inviter, String invitee, String groupName) throws SQLException {
        final String QUERY = "INSERT INTO group_invitation (inviter, invitee, group_name) VALUES (?,?,?)";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, inviter, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }

    /**
     * The implementation for the interface method for deleting an invitation
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return true if the update was successful, false if not
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public boolean deleteInvitation(String inviter, String invitee, String groupName) throws SQLException {
        final String QUERY = "UPDATE group_invitation SET is_deleted = ? WHERE inviter = ? and invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, true, inviter, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }

    /**
     * The method to retrieve all messages for an invitee which have not been sent
     *
     * @param invitee - the person who is receiving the invitation
     * @return the set of invitations not sent to the user yet
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public Set<Message> getInvitationsForInvitee(String invitee) throws SQLException {
        final String QUERY = "SELECT * from group_invitation where invitee = ? and is_sent_invitee = 0";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, invitee);
        result = preparedStatement.executeQuery();
        Set<Message> messages = new HashSet<>();
        while (result.next()) {
            boolean isAccepted = result.getBoolean(IS_ACCEPTED);
            boolean isDenied = result.getBoolean(IS_DENIED);
            boolean isApproved = result.getBoolean(IS_APPROVED);
            boolean isRejected = result.getBoolean(IS_REJECTED);
            boolean isDeleted = result.getBoolean(IS_DELETED);
            String inviter = result.getString(INVITER);
            String groupName = result.getString(GROUP_NAME);
            Message message = Message.makeCreateInvitationMessage(inviter, invitee, groupName);
            message.setInvitationAccepted(isAccepted);
            message.setInvitationDenied(isDenied);
            message.setInvitationApproved(isApproved);
            message.setInvitationRejected(isRejected);
            message.setInvitationDeleted(isDeleted);
            messages.add(message);
        }
        preparedStatement.close();
        return messages;
    }

    /**
     * The method to retrieve all invitations for a group which have not been sent to its moderator
     *
     * @param groupName - the group for which the invitation was sent
     * @return the set of invitations not sent to the user yet
     * @throws SQLException - the SQL exception is thrown due to some query or database interaction
     */
    @Override
    public Set<Message> getInvitationsForGroup(String groupName) throws SQLException {
        final String QUERY = "SELECT * from group_invitation where group_name = ? and is_sent_moderator = 0";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, groupName);
        result = preparedStatement.executeQuery();
        Set<Message> messages = new HashSet<>();
        while (result.next()) {
            boolean isAccepted = result.getBoolean(IS_ACCEPTED);
            boolean isDenied = result.getBoolean(IS_DENIED);
            boolean isApproved = result.getBoolean(IS_APPROVED);
            boolean isRejected = result.getBoolean(IS_REJECTED);
            boolean isDeleted = result.getBoolean(IS_DELETED);
            String inviter = result.getString(INVITER);
            String invitee = result.getString(INVITEE);
            Message message = Message.makeCreateInvitationMessage(inviter, invitee, groupName);
            message.setInvitationAccepted(isAccepted);
            message.setInvitationDenied(isDenied);
            message.setInvitationApproved(isApproved);
            message.setInvitationRejected(isRejected);
            message.setInvitationDeleted(isDeleted);
            messages.add(message);
        }
        preparedStatement.close();
        return messages;
    }

    /**
     * Sets the sent flag for an invitation when it is sent to a user
     *
     * @param invitee - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return the
     */
    @Override
    public boolean setInvitationIsSentToInvitee(String invitee, String groupName) throws SQLException {
        final String QUERY = "UPDATE group_invitation SET is_sent_invitee = 1 WHERE invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }

    /**
     * Sets the sent flag for an invitation when it is sent to a moderator
     *
     * @param invitee - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return the
     */
    @Override
    public boolean setInvitationIsSentToModerator(String invitee, String groupName) throws SQLException {
        final String QUERY = "UPDATE group_invitation SET is_sent_moderator = 1 WHERE invitee = ? and group_name = ?";
        preparedStatement = connection.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, invitee, groupName);
        int qResult = preparedStatement.executeUpdate();
        preparedStatement.close();
        return qResult > 0;
    }
}

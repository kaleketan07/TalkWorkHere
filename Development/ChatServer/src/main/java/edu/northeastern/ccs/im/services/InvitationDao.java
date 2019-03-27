package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.Message;

import java.sql.SQLException;
import java.util.Set;

/**
 * The DAO for Invitation Service
 *
 * @author Sachin Haldavanekar
 * @version 1.0
 */
public interface InvitationDao {

    /**
     * The method to get an invitation based on the three inputs provided
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return - returns null or a Message based on whether a record was found in the database for the
     * given search criteria
     * @throws SQLException
     */
    Message getInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method to get an invitation based on the two inputs provided
     *
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being send
     * @return - returns null or a Message based on whether a record was found in the database for the
     * given search criteria
     * @throws SQLException
     */
    Message getInvitation(String invitee, String groupName) throws SQLException;

    /**
     * The method for accepting or denying an invitation. This method will be used by users
     * who have received an invite
     *
     * @param invitee   - The person who is invited
     * @param groupName - The group for which the invite is sent
     * @param accepted  - The flag denoting true for acceptance and false for denial
     * @return - Returns true or false based on whether the update to the database was successful.
     * @throws SQLException
     */
    boolean acceptDenyInvitation(String invitee, String groupName, boolean accepted) throws SQLException;

    /**
     * The method for accepting or denying an invitation. This method will be used by moderators
     * of the group for which the invite was sent.
     *
     * @param invitee   - The person who is invited
     * @param groupName - The group for which the invite is sent
     * @param approved  - The flag denoting true for approval and false for rejection
     * @return - Returns true or false based on whether the update to the database was successful.
     * @throws SQLException
     */
    boolean approveRejectInvitation(String invitee, String groupName, boolean approved) throws SQLException;

    /**
     * The method create a new invitation
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return - returns true if the record was entered in the databasse else false
     * @throws SQLException
     */
    boolean createInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method delete an invitation.
     *
     * @param inviter   - the person who is the sender of the invitation
     * @param invitee   - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return - returns true if the invite was deleted, false if there was any issue that occurred.
     * @throws SQLException
     */
    boolean deleteInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method gets invitations that need to be sent to an invitee
     *
     * @param invitee - the person who is receiving the invitation
     * @return the set of messages which have to be delivered to the user
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    Set<Message> getInvitationsForInvitee(String invitee) throws SQLException;

    /**
     *  The method that gets invitations that need to be sent to a group moderator
     *
     * @param groupName - the group for which the invitations need to be fetched
     * @return the set of messages which have to be delivered to the user
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    Set<Message> getInvitationsForGroup(String groupName) throws SQLException;

    /**
     * @param invitee - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return the set of messages which have to be delivered to the user
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    boolean setInvitationIsSentToInvitee(String invitee, String groupName) throws SQLException;

    /**
     * @param invitee - the person who is receiving the invitation
     * @param groupName - the group for which the invite is being sent
     * @return the set of messages which have to be delivered to the user
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    boolean setInvitationIsSentToModerator(String invitee, String groupName) throws SQLException;

}

/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

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
     * @param inviter   the person who is the sender of the invitation
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being send
     * @return Message      returns null or a Message based on whether a record was found in the database for the
     * given search criteria
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Message getInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method to get an invitation based on the two inputs provided
     *
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being send
     * @return Message      returns null or a Message based on whether a record was found in the database for the
     * given search criteria
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Message getInvitation(String invitee, String groupName) throws SQLException;

    /**
     * The method for accepting or denying an invitation. This method will be used by users
     * who have received an invite
     *
     * @param invitee   The person who is invited
     * @param groupName The group for which the invite is sent
     * @param accepted  The flag denoting true for acceptance and false for denial
     * @return boolean      Returns true or false based on whether the update to the database was successful.
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean acceptDenyInvitation(String invitee, String groupName, boolean accepted) throws SQLException;

    /**
     * The method for accepting or denying an invitation. This method will be used by moderators
     * of the group for which the invite was sent.
     *
     * @param invitee   The person who is invited
     * @param groupName The group for which the invite is sent
     * @param approved  The flag denoting true for approval and false for rejection
     * @return boolean      Returns true or false based on whether the update to the database was successful.
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean approveRejectInvitation(String invitee, String groupName, boolean approved) throws SQLException;

    /**
     * The method create a new invitation
     *
     * @param inviter   the person who is the sender of the invitation
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being sent
     * @return boolean      returns true if the record was entered in the databasse else false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean createInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method delete an invitation.
     *
     * @param inviter   the person who is the sender of the invitation
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being sent
     * @return boolean      returns true if the invite was deleted, false if there was any issue that occurred.
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean deleteInvitation(String inviter, String invitee, String groupName) throws SQLException;

    /**
     * The method gets invitations that need to be sent to an invitee
     *
     * @param invitee the person who is receiving the invitation
     * @return Set          the set of messages which have to be delivered to the user
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Set<Message> getInvitationsForInvitee(String invitee) throws SQLException;

    /**
     * The method that gets invitations that need to be sent to a group moderator
     *
     * @param groupName the group for which the invitations need to be fetched
     * @return Set          the set of messages which have to be delivered to the user
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Set<Message> getInvitationsForGroup(String groupName) throws SQLException;

    /**
     * Sets the is sent flag for the invitation when sent to intended invitee
     *
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being sent
     * @return boolean      true if the invite is successfully set, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean setInvitationIsSentToInvitee(String invitee, String groupName) throws SQLException;

    /**
     * Sets the is sent flag for the invitation when sent to intended moderator
     *
     * @param invitee   the person who is receiving the invitation
     * @param groupName the group for which the invite is being sent
     * @return boolean      true if the invite is successfully set, false otherwise
     * @throws SQLException the SQL exception is thrown due to some query or database interaction
     */
    boolean setInvitationIsSentToModerator(String invitee, String groupName) throws SQLException;

}

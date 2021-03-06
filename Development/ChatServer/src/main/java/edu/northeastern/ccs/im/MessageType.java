/*
 ***************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ***************************************************************************************
 */

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
    MESSAGE_USER("MSU"),

    /**
     * Message whose contents are to add a user to a group with the passed parameters
     */
    ADD_USER_GROUP("AUG"),

    /**
     * Message whose contents are to update a user's profile
     */
    UPDATE_PROFILE_USER("UPU"),

    /**
     * Message to privately reply to a group Message
     */
    PRIVATE_REPLY_MESSAGE("PRE"),

    /**
     * Message whose contents are to delete a user profile
     */
    DELETE_USER("DLU"),

    /**
     * Message to get followers of the followee
     */
    GET_FOLLOWERS("GFR"),

    /**
     * Message to get followees of the follower
     */
    GET_FOLLOWEES("GFE"),

    /**
     * Handle To send prattle messages to the client
     */
    GET_ONLINE_USERS("GOU"),

    /**
     * Message whose contents are to delete a user profile from a group
     */
    REMOVE_USER_GROUP("RUG"),

    /**
     * Message whose contents are to follow a user
     */
    FOLLOW_USER("FWU"),
    /**
     * Message whose contents are to unfollow a user
     */
    UNFOLLOW_USER("UFU"),
    /**
     * Handle To send prattle messages to the client
     */
    PRATTLE_MESSAGE("PRM"),
    /**
     * Handle To send prattle messages to the client
     */
    MESSAGE_GROUP("MSG"),

    /**
     * Handle to Update group settings.
     */
    UPDATE_GROUP("UPG"),

    /**
     * Handle to search a user or a group
     */
    SEARCH_MESSAGE("SRH"),

    /**
     * Handle to delete a private message.
     */
    DELETE_PRIVATE_MESSAGE("DPM"),

    /**
     * Handle to delete a group message.
     */
    DELETE_GROUP_MESSAGE("DGM"),

    /**
     * Handle to add a group to group.
     */
    ADD_GROUP_TO_GROUP("AGG"),

    /**
     * Handle to remove a group from group.
     */
    REMOVE_GROUP_FROM_GROUP("RGG"),

    /**
     * Message handle for Inviting user to a group
     */
    INVITE_USER_GROUP("IUG"),

    /**
     * Message handle for deleting an invitation
     */
    DELETE_USER_INVITATION("DUI"),

    /**
     * Message handle for a user to accept an invite
     */
    ACCEPT_INVITE_USER("AIU"),

    /**
     * Message handle for a user to deny an invite
     */
    DENY_INVITE_USER("DIU"),

    /**
     * Message handle for a moderator to approve an invite.
     */
    APPROVE_INVITE_MODERATOR("AIM"),

    /**
     * Message handle for a moderator to approve an invite.
     */
    REJECT_INVITE_MODERATOR("RIM"),

    /**
     * Message handle for a user to retrieve all their messages
     */
    GET_PAST_MESSAGES("GPM"),

    /**
     * Message handle for a user to leave a group
     */
    LEAVE_GROUP("LGP"),

    /**
     * Message handle for government to get the conversation history for a user of interest
     */
    GET_CONVERSATION_HISTORY("GCH"),

    /**
     * Message handle for the government to tap a certain user of interest
     */
    TAP_USER("TPU");


    /**
     * Store the short name of this message type.
     */
    private String abbreviation;

    /**
     * Define the message type and specify its short name.
     *
     * @param abbrev Short name of this message type, as a String.
     */
    MessageType(String abbrev) {
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

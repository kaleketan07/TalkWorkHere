/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im.models;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.services.ConversationalMessageService;

/**
 * The Group class depicts the concept of a group.
 *
 * @author - team 201 - Ketan Kale
 */
public class Group implements Member {


    /**
     * Instantiates a new group.
     */
    public Group() {
        this.memberUsers = new HashSet<>();
        this.memberGroups = new HashSet<>();
    }

    /**
     * The group name.
     */
    private String groupName;

    /**
     * The moderator name.
     */
    private String moderatorName;

    /**
     * The member users.
     */
    private Set<User> memberUsers;

    /**
     * The member groups.
     */
    private Set<Group> memberGroups;


    /**
     * The static instance of the conversational Message Service
     */
    private static ConversationalMessageService cms;

    static {
        try {
            cms = ConversationalMessageService.getInstance();
        } catch (IOException | SQLException e) {
            ChatLogger.error("Conversational Message Service failed to initialize: " + e);
            e.printStackTrace();
        }
    }


    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Sets the group name.
     *
     * @param name      the new group name
     */
    public void setGroupName(String name) {
        this.groupName = name;
    }

    /**
     * Gets the moderator name.
     *
     * @return String   the moderator name
     */
    public String getModeratorName() {
        return this.moderatorName;
    }

    /**
     * Sets the moderator name.
     *
     * @param name      the new moderator name
     */
    public void setModeratorName(String name) {
        this.moderatorName = name;
    }

    /**
     * Gets the member users.
     *
     * @return Set      the set of member users
     */
    public Set<User> getMemberUsers() {
        return this.memberUsers;
    }

    /**
     * Gets the member groups.
     *
     * @return Set      the set of member groups
     */
    public Set<Group> getMemberGroups() {
        return this.memberGroups;
    }

    /**
     * Sets the member users.
     *
     * @param users     the set of new member users
     */
    public void setMemberUsers(Set<User> users) {
        this.memberUsers = users;
    }

    /**
     * Sets the member groups.
     *
     * @param groups    the set of new member groups
     */
    public void setMemberGroups(Set<Group> groups) {
        this.memberGroups = groups;
    }

    /**
     * Send message to members of this group
     *
     * @param msg           the message to be sent
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    public void groupSendMessage(Message msg, String uniqueGroupKey) throws SQLException {
        // send message to member users
        for (User u : this.memberUsers) {
            // a user can also be a part of a group at a higher level in the hierarchy. Do not send the message again
            if (!msg.messageAlreadySent(u)) {
                msg.addUserToRecipients(u);
                String uniqueMessageKey = u.userSendMessage(msg);
                // add the uniqueMsgKey and the UniqueGroupKey to the new table
                cms.insertGroupConversationalMessage(uniqueGroupKey, uniqueMessageKey);
            }
        }
        // send message to member groups
        for (Group g : this.memberGroups) {
            g.groupSendMessage(msg, uniqueGroupKey);
        }
    }

    /**
     * The overriden equals method to check if the two group objects are equal, based on the group name of the given
     * group objects
     *
     * @param obj       The object to be checked for equality with the current object
     * @return boolean  true if the current and given objects have same group name, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            return this.groupName.equals(((Group) obj).getGroupName());
        }
        return false;
    }

    /**
     * Overridden method to generate unique hashcode for every Group object
     *
     * @return  int     a unique integer for every Group object
     */
    @Override
    public int hashCode() {
        return (this.groupName != null) ? 31 * groupName.hashCode() : 0;
    }
}

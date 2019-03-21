package edu.northeastern.ccs.im.models;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import edu.northeastern.ccs.im.Message;

/**
 * The Group class depicts the concept of a group.
 *
 * @author - team 201 - Ketan Kale
 */
public class Group implements Member {


    /**
     * Instantiates a new group.
     *
     * @param name, the name of the group
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
     * @param name, the new group name
     */
    public void setGroupName(String name) {
        this.groupName = name;
    }

    /**
     * Gets the moderator name.
     *
     * @return the moderator name
     */
    public String getModeratorName() {
        return this.moderatorName;
    }

    /**
     * Sets the moderator name.
     *
     * @param name, the new moderator name
     */
    public void setModeratorName(String name) {
        this.moderatorName = name;
    }

    /**
     * Gets the member users.
     *
     * @return the member users
     */
    public Set<User> getMemberUsers() {
        return this.memberUsers;
    }

    /**
     * Gets the member groups.
     *
     * @return the member groups
     */
    public Set<Group> getMemberGroups() {
        return this.memberGroups;
    }

    /**
     * Sets the member users.
     *
     * @param users the new member users
     */
    public void setMemberUsers(Set<User> users) {
        this.memberUsers = users;
    }

    /**
     * Sets the member groups.
     *
     * @param groups the new member groups
     */
    public void setMemberGroups(Set<Group> groups) {
        this.memberGroups = groups;
    }
    
    /**
     * Send message to members of this group
     *
     * @param msg the message to be sent
     * @throws SQLException the SQL exception
     */
    public void groupSendMessage(Message msg, String uniqueGroupKey) throws SQLException {
    	// send message to member users
    	for (User u : memberUsers) {
    		// a user can also be a part of a group at a higher level in the hierarchy. Do not send the message again
    		if (!msg.messageAlreadySent(u)) {
    			msg.addUserToRecipients(u);
    			u.userSendMessage(msg);
    			// add the uniqueMsgKey and the UniqueGroupKey to the new table
    		}
    	}
    	// send message to member groups
    	for (Group g: memberGroups) {
    		g.groupSendMessage(msg, uniqueGroupKey);
    	}
    }
}

package edu.northeastern.ccs.im.models;

import java.util.Set;

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
        this.groupName = null;
        this.moderatorName = null;
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
}

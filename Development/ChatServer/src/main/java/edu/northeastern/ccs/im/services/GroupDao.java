/*
 ***************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ***************************************************************************************
 */

package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;


/**
 * The Interface GroupDao provides an interface for the users of the application to access the Group schema.
 *
 * @author - Team 201 - Ketan Kale
 */
public interface GroupDao {


    /**
     * Fetches the group from the database.
     *
     * @param groupName the group name
     * @return the group object
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Group getGroup(String groupName) throws SQLException;

    /**
     * Creates the group in the database.
     *
     * @param groupName the group name
     * @param modName   the moderator name of the group
     * @return boolean      true, if successful else return false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean createGroup(String groupName, String modName) throws SQLException;

    /**
     * Delete group.
     *
     * @param groupName the group name
     * @return boolean      true, if successful else return false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean deleteGroup(String groupName) throws SQLException;

    /**
     * Gets the member users.
     *
     * @param groupName the group name
     * @return Set of User objects  the member users in the group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data
     *                      source
     */
    Set<User> getMemberUsers(String groupName) throws SQLException;

    /**
     * Gets the member groups.
     *
     * @param groupName the group name
     * @return Set of Group names   the member groups
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data
     *                      source
     */
    Set<String> getMemberGroups(String groupName) throws SQLException;

    /**
     * Gets the all groups.
     *
     * @return Set of Group objects    all the groups present in the system
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the
     *                      data source
     */
    Set<Group> getAllGroups() throws SQLException;


    /**
     * Checks if the passed username matches the moderator name for the
     * group having the passed group name.
     *
     * @param groupName the group name
     * @param userName  the user name
     * @return boolean      true, if is moderator
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean isModerator(String groupName, String userName) throws SQLException;


    /**
     * Adds the user to this group.
     *
     * @param hostGroupName the host group name
     * @param guestUserName the guest user name
     * @return boolean      true, if user was added successfully, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean addUserToGroup(String hostGroupName, String guestUserName) throws SQLException;

    /**
     * Removes the user from the given group
     *
     * @param hostGroupName the name of the host group
     * @param guestUserName the username of the guest user
     * @return boolean          true, if successful
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data
     *                      source
     */
    boolean removeUserFromGroup(String hostGroupName, String guestUserName) throws SQLException;

    /**
     * Adds the group to group.
     *
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name
     * @return boolean       true, if successful
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean addGroupToGroup(String hostGroupName, String guestGroupName) throws SQLException;


    /**
     * Checks if is user member of the group.
     *
     * @param grpName  the name of the group in which the user name is to be checked
     * @param userName the user name to be checked
     * @return boolean      true, if is user is a member of the group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean isUserMemberOfTheGroup(String grpName, String userName) throws SQLException;


    /**
     * Updates the group settings. For now a moderator can just change the group_searchable attribute
     * as a setting.
     *
     * @param groupName      the group name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the boolean   true if the attribute was successfully updated, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean updateGroupSettings(String groupName, String attributeName, String attributeValue) throws SQLException;

    /**
     * Retrieve all the searchable groups from the given string
     *
     * @param searchString the string to be used in the regex to find all similar groups
     * @return Map           A HashMap containing the group names as keys and their moderator username as corresponding
     * values
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Map<String, String> searchGroup(String searchString) throws SQLException;

    /**
     * Removes the group from the given group
     *
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name that needs to be removed
     * @return boolean       true, if successfully removes the group else returns false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean removeGroupFromGroup(String hostGroupName, String guestGroupName) throws SQLException;


    /**
     * Get groups by moderator name
     *
     * @param moderatorName the moderator whose groups need to be found
     * @return Set          the set of group objects that the user is a moderator of
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    Set<String> getGroupsByModerator(String moderatorName) throws SQLException;

    /**
     * Check if users is a direct member of the group
     *
     * @param hostGroupName the host group name
     * @param guestUserName the username of the user to be checked for group membership
     * @return boolean          true, if user is a member of the group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    boolean checkMembershipInGroup(String hostGroupName, String guestUserName) throws SQLException;
}

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
     * @return the group
     * @throws SQLException the SQL exception
     */
    Group getGroup(String groupName) throws SQLException;

    /**
     * Creates the group in the database.
     *
     * @param groupName the group name
     * @param modName   the mod name
     * @return true, if successful else return false
     * @throws SQLException the SQL exception
     */
    boolean createGroup(String groupName, String modName) throws SQLException;

    /**
     * Delete group.
     *
     * @param groupName the group name
     * @return true, if successful else return false
     * @throws SQLException the SQL exception
     */
    boolean deleteGroup(String groupName) throws SQLException;

    /**
     * Gets the member users.
     *
     * @param groupName the group name
     * @return the member users in the group
     * @throws SQLException the SQL exception
     */
    Set<User> getMemberUsers(String groupName) throws SQLException;

    /**
     * Gets the member groups.
     *
     * @param groupName the group name
     * @return the member groups
     * @throws SQLException the SQL exception
     */
    Set<String> getMemberGroups(String groupName) throws SQLException;

    /**
     * Gets the all groups.
     *
     * @return the all groups
     * @throws SQLException the SQL exception
     */
    Set<Group> getAllGroups() throws SQLException;


    /**
     * Checks if the passed username matches the moderator name for the
     * group having the passed group name.
     *
     * @param groupName the group name
     * @param userName  the user name
     * @return true, if is moderator
     * @throws SQLException the SQL exception
     */
    boolean isModerator(String groupName, String userName) throws SQLException;


    /**
     * Adds the user to this group.
     *
     * @param hostGroupName the host group name
     * @param guestUserName the guest user name
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    boolean addUserToGroup(String hostGroupName, String guestUserName) throws SQLException;

    /**
     * Removes the user from the given group
     *
     * @param hostGroupName
     * @param guestUserName
     * @return true, if successful
     * @throws SQLException
     */
    boolean removeUserFromGroup(String hostGroupName, String guestUserName) throws SQLException;

    /**
     * Adds the group to group.
     *
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    boolean addGroupToGroup(String hostGroupName, String guestGroupName) throws SQLException;

    
    /**
     * Checks if is user member of the group.
     *
     * @param grpName the name of the group in which the user name is to be searched 
     * @param userName the user name to be searched
     * @return true, if is user is a member of the group
     * @throws SQLException the SQL exception
     */
    boolean isUserMemberOfTheGroup(String grpName, String userName) throws SQLException;


    /**
     * Updates the group settings. For now a moderator can just change the group_searchable attribute
     * as a setting.
     *
     * @param groupName      the group name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the boolean   True if the attribute was successfully updated, false otherwise
     * @throws SQLException the sql exception
     */
    boolean updateGroupSettings(String groupName, String attributeName, String attributeValue) throws SQLException;

    /**
     * Retrieve all the searchable groups from the given string
     * @param searchString the string to be used in the regex to find all similar groups
     * @return A hashmap containing the group names as keys and their moderator usernames as corresponding values
     * @throws SQLException the sql exception
     */
    Map<String,String> searchGroup(String searchString) throws SQLException;
}

package edu.northeastern.ccs.im.services;

import java.sql.SQLException;
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
     * @param userName  the user name
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
     * Adds the group to group.
     *
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    boolean addGroupToGroup(String hostGroupName, String guestGroupName) throws SQLException;
}

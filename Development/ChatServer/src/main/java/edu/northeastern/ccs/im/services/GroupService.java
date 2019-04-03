package edu.northeastern.ccs.im.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.db.IDBConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;

/**
 * The Class GroupService implements the Group DAO interface and provides a set of methods that could be performed on the
 * groups in the database
 *
 * @author - Team-201 - Ketan Kale
 */
public class GroupService implements GroupDao {
    private IDBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils;
    private ResultSet result;
    private static GroupService groupServiceInstance;

    private static final String USER_NAME = "username";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String GROUP_NAME = "group_name";
    private static final String GUEST_GROUP_NAME = "guest_group_name";
    private static final String MODERATOR_NAME = "moderator_name";
    private static final String LOGGED_IN = "logged_in";
    private static final String IS_REMOVED = "is_removed";
    

    /**
     * Instantiates a new group service.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the SQL exception
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    private GroupService() throws ClassNotFoundException, SQLException, IOException {
        conn = new DBConnection();
        utils = new DBUtils();

    }

    /**
     * Gets the singleton group service instance.
     *
     * @return the group service instance
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException           the SQL exception
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    public static GroupService getGroupServiceInstance() throws ClassNotFoundException, SQLException, IOException {
        if (groupServiceInstance == null) {
            groupServiceInstance = new GroupService();
        }
        return groupServiceInstance;
    }

    /**
     * Fetches the group from the database.
     *
     * @param groupName the group name
     * @return the group
     * @throws SQLException the SQL exception
     */
    @Override
    public Group getGroup(String groupName) throws SQLException {
        Group g = new Group();
        final String GET_GROUP = "SELECT * FROM prattle.groups WHERE group_name = ?";
        pstmt = conn.getPreparedStatement(GET_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        result = pstmt.executeQuery();
        if (result.first()) {
            String gName = result.getString(GROUP_NAME);
            String modName = result.getString(MODERATOR_NAME);
            g.setGroupName(gName);
            g.setModeratorName(modName);
            Set<User> users = getMemberUsers(groupName);
            Set<Group> groups = new HashSet<>();
            g.setMemberUsers(users);
            Set<String> memberGroupNames = getMemberGroups(groupName);
            for (String group : memberGroupNames) {
                Group temp = getGroup(group);
                groups.add(temp);
            }
            g.setMemberGroups(groups);
        } else {
            g = null;
        }
        pstmt.close();
        return g;
    }

    /**
     * Creates the group in the database.
     *
     * @param groupName the group name
     * @param modName   the mod name
     * @return true, if successful else return false
     * @throws SQLException the SQL exception
     */
    @Override
    public boolean createGroup(String groupName, String modName) throws SQLException {
        final String CREATE_GROUP =
                "INSERT INTO prattle.groups (group_name, moderator_name) VALUES (?,?)";
        pstmt = conn.getPreparedStatement(CREATE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName, modName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Delete group.
     *
     * @param groupName the group name
     * @return true, if successful else return false
     * @throws SQLException the SQL exception
     */
    @Override
    public boolean deleteGroup(String groupName) throws SQLException {
        final String DELETE_GROUP =
                "UPDATE prattle.groups SET is_deleted = 1 WHERE group_name = ?";
        pstmt = conn.getPreparedStatement(DELETE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Gets the member users.
     *
     * @param groupName the group name
     * @return the member users in the group
     * @throws SQLException the SQL exception
     */
    @Override
    public Set<User> getMemberUsers(String groupName) throws SQLException {

        final String FETCH_MEMBER_USERS = "WITH cte AS (SELECT * FROM prattle.groups JOIN prattle.membership_users ON prattle.groups.group_name = prattle.membership_users.host_group_name WHERE prattle.groups.group_name = ?) SELECT user_id, username, first_name, last_name, logged_in FROM cte JOIN prattle.user_profile ON cte.guest_user_name = prattle.user_profile.username WHERE is_removed = 0;";
        pstmt = conn.getPreparedStatement(FETCH_MEMBER_USERS);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        Set<User> users = new HashSet<>();
        result = pstmt.executeQuery();
        while (result.next()) {
            String fName = result.getString(FIRST_NAME);
            String lName = result.getString(LAST_NAME);
            String uName = result.getString(USER_NAME);
            boolean stat = result.getBoolean(LOGGED_IN);
            User user = new User(fName, lName, uName, "", stat);    // do we need a constructor without password here?
            users.add(user);
        }
        pstmt.close();
        return users;
    }

    /**
     * Gets the member groups of the given group.
     *
     * @param groupName the group name
     * @return the member groups
     * @throws SQLException the SQL exception
     */
    @Override
    public Set<String> getMemberGroups(String groupName) throws SQLException {
        final String FETCH_MEMBER_GROUPS = "SELECT prattle.membership_groups.guest_group_name FROM prattle.groups JOIN prattle.membership_groups on prattle.groups.group_name = prattle.membership_groups.host_group_name WHERE prattle.groups.group_name = ? AND is_removed = 0";
        pstmt = conn.getPreparedStatement(FETCH_MEMBER_GROUPS);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        Set<String> groups = new HashSet<>();
        result = pstmt.executeQuery();
        while (result.next()) {
            String gName = result.getString(GUEST_GROUP_NAME);
            groups.add(gName);
        }
        pstmt.close();
        return groups;
    }

    /**
     * Get all the groups.
     *
     * @return the all groups
     * @throws SQLException the SQL exception
     */
    @Override
    public Set<Group> getAllGroups() throws SQLException {
        final String GET_ALL_GROUP_NAMES = "SELECT group_name from prattle.groups";
        pstmt = conn.getPreparedStatement(GET_ALL_GROUP_NAMES);
        Set<Group> groups = new HashSet<>();
        try {
            result = pstmt.executeQuery();
            while (result.next()) {
                String gName = result.getString(GROUP_NAME);
                groups.add(getGroup(gName));
            }

        } catch (Exception e) {
            throw new SQLException();
        }
        pstmt.close();

        return groups;
    }


    /**
     * Checks if the passed username matches the moderator name for the
     * group having the passed group name.
     *
     * @param groupName the group name
     * @param userName  the user name
     * @return true, if is moderator
     * @throws SQLException the SQL exception
     */
    @Override
    public boolean isModerator(String groupName, String userName) throws SQLException {
        final String GET_MODERATOR_NAME = "SELECT moderator_name from prattle.groups where group_name = ?";
        pstmt = conn.getPreparedStatement(GET_MODERATOR_NAME);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        String modName;
        result = pstmt.executeQuery();
        if (!result.first()) {
            return false;
        }
        modName = result.getString(MODERATOR_NAME);
        pstmt.close();
        return userName.equals(modName);
    }


    /**
     * Adds the user to this group.
     *
     * @param hostGroupName the host group name
     * @param guestUserName the guest user name
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    public boolean addUserToGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        Set<User> users = getMemberUsers(hostGroupName);
        for (User u : users) {
            if (u.getUserName().equals(guestUserName)) return false;
        }
        final String ADD_USER_TO_GROUP = "INSERT INTO membership_users (host_group_name, guest_user_name) VALUES (?,?)";
        pstmt = conn.getPreparedStatement(ADD_USER_TO_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Removes the user from the given group
     *
     * @param hostGroupName
     * @param guestUserName
     * @return true, if successful
     * @throws SQLException
     */
    @Override
    public boolean removeUserFromGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        final String REMOVE_USER_FROM_GROUP = "UPDATE membership_users SET is_removed = 1 WHERE host_group_name = ? and guest_user_name = ?";
        pstmt = conn.getPreparedStatement(REMOVE_USER_FROM_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }
    
    
    /**
     * Check if users is a direct member of the group 
     *
     * @param hostGroupName
     * @param guestUserName
     * @return true, if user is a member of the group
     * @throws SQLException
     */
    @Override
    public boolean checkMembershipInGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        final String CHECK_USER_MEMEBERSHIP = "SELECT * FROM membership_users where host_group_name = ? and guest_user_name = ?";
        pstmt = conn.getPreparedStatement(CHECK_USER_MEMEBERSHIP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        result = pstmt.executeQuery();
        pstmt.close();
        if(result.first()) {
        	boolean removed = result.getBoolean(IS_REMOVED);
        	if(!removed){
        		return true;
        	}
        }
        return false;
        
    }
    	
    
    /**
     * Gets the flat list of groups present in this group.
     *
     * @param group      the group in which we are searching for groups
     * @param descGroups the set of descendant groups
     * @return the flat list (set) of groups present in this group
     * @throws SQLException the SQL exception
     */
    private Set<String> getFlatListOfGroups(Group group, Set<String> descGroups) throws SQLException {
        descGroups.add(group.getGroupName());
        for (Group g : group.getMemberGroups()) {
            descGroups = getFlatListOfGroups(g, descGroups);
        }
        return descGroups;
    }

    /**
     * Adds the group to group.
     *
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    @Override
    public boolean addGroupToGroup(String hostGroupName, String guestGroupName) throws SQLException {
        Group grp = getGroup(guestGroupName);
        Set<String> descendantGroups = new HashSet<>();
        descendantGroups = getFlatListOfGroups(grp, descendantGroups);
        if (descendantGroups.contains(hostGroupName)) {
            return false;
        } else {
            final String ADD_GROUP_TO_GROUP = "INSERT INTO membership_users (host_group_name, guest_group_name) VALUES (?,?)";
            pstmt = conn.getPreparedStatement(ADD_GROUP_TO_GROUP);
            pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestGroupName);
            int qResult = pstmt.executeUpdate();
            pstmt.close();
            return (qResult > 0);
        }
    }


    /**
     * Checks if is user member of the group.
     *
     * @param grpName  the name of the group in which the user name is to be checked
     * @param userName the user name to be checked
     * @return true, if is user is a member of the group
     * @throws SQLException the SQL exception
     */
    public boolean isUserMemberOfTheGroup(String grpName, String userName) throws SQLException {
        Group group = getGroup(grpName);
        Set<String> memberUserNames = new HashSet<>();
        memberUserNames = getFlatListOfUsers(group, memberUserNames);
        return memberUserNames.contains(userName);
    }


    /**
     * Gets the flat list of member users of a group.
     *
     * @param grp       the group object of the group
     * @param userNames the set of user names of member users of the group
     * @return the flat list of user names strings
     */
    private Set<String> getFlatListOfUsers(Group grp, Set<String> userNames) {
        for (User u : grp.getMemberUsers()) {
            userNames.add(u.getUserName());
        }
        for (Group g : grp.getMemberGroups()) {
            userNames = getFlatListOfUsers(g, userNames);
        }
        return userNames;
    }

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
    @Override
    public boolean updateGroupSettings(String groupName, String attributeName, String attributeValue)
            throws SQLException {
        final String UPDATE_GROUP = "UPDATE prattle.groups SET " + attributeName + " = ? WHERE group_name = ?";
        String trueOrFalse;
        pstmt = conn.getPreparedStatement(UPDATE_GROUP);
        if (attributeName.compareTo("is_searchable") == 0) {
            if (attributeValue.compareTo(Integer.toString(0)) == 0 || attributeValue.equalsIgnoreCase("false")) {
                trueOrFalse = "0";
                pstmt = utils.setPreparedStatementArgs(pstmt, trueOrFalse, groupName);
            } else if (attributeValue.compareTo(Integer.toString(1)) == 0 || attributeValue.equalsIgnoreCase("true")) {
                trueOrFalse = "1";
                pstmt = utils.setPreparedStatementArgs(pstmt, trueOrFalse, groupName);
            } else
                ChatLogger.error("Searchable values should be boolean (1/0 True/False)");
        }
        else {
            pstmt = utils.setPreparedStatementArgs(pstmt, attributeValue, groupName);
        }
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }

    /**
     * Searches and retrieves all the group that match the regular expression passed as a string.
     *
     * @param searchString the search string
     * @return the hash map containing the group names with their respective moderator names
     * @throws SQLException the sql exception
     */
    @Override
    public Map<String, String> searchGroup(String searchString) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        final String SEARCH_GROUP = "SELECT group_name, moderator_name FROM prattle.groups WHERE" +
                " is_searchable = 1 AND (group_name REGEXP concat(\"^\",?,\".*\"))";
        pstmt = conn.getPreparedStatement(SEARCH_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, searchString);
        result = pstmt.executeQuery();
        while (result.next()) {
            String groupName = result.getString(GROUP_NAME);
            String modName = result.getString(MODERATOR_NAME);
            resultMap.put(groupName, modName);
        }
        return resultMap;
    }

    /**
     * Removes the group from the given group
     *
     * @param hostGroupName
     * @param guestGroupName
     * @return true, if successfully removes the group else returns false
     * @throws SQLException
     */
    @Override
    public boolean removeGroupFromGroup(String hostGroupName, String guestGroupName) throws SQLException {
        Group grp = getGroup(hostGroupName);
        Set<String> descendantGroups = new HashSet<>();
        descendantGroups = getFlatListOfGroups(grp, descendantGroups);
        if (descendantGroups.contains(guestGroupName)) {
            final String REMOVE_GROUP_FROM_GROUP = "UPDATE prattle.membership_groups SET is_removed = 1 WHERE membership_groups.host_group_name = ? AND membership_groups.guest_group_name = ?";
            pstmt = conn.getPreparedStatement(REMOVE_GROUP_FROM_GROUP);
            pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestGroupName);
            int qResult = pstmt.executeUpdate();
            pstmt.close();
            return (qResult > 0);
        } else {
            return false;
        }
    }

    /**
     * The method to get groups based on moderator name
     *
     * @param  moderatorName - the moderator whose groups need to be found
     * @return a set of groups which have the given user as the moderator
     */
    @Override
    public Set<String> getGroupsByModerator(String moderatorName) throws SQLException{
        final String QUERY = "SELECT group_name from prattle.groups where moderator_name = ?";
        PreparedStatement preparedStatement = conn.getPreparedStatement(QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, moderatorName);
        result = preparedStatement.executeQuery();
        Set<String> groups = new HashSet<>();
        while(result.next()) {
            groups.add(result.getString(GROUP_NAME));
        }
        return groups;
    }
}

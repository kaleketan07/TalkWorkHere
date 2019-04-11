/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    private Properties groupProperties;
    
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
     * @throws SQLException           the sql exception thrown in case of an error with jdbc's interaction with the data source
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    private GroupService() throws SQLException, IOException {
        conn = new DBConnection();
        utils = new DBUtils();
        groupProperties = conn.getQueryProperties();

    }

    /**
     * Gets the singleton group service instance.
     *
     * @return GroupService           the group service instance
     * @throws SQLException           the sql exception thrown in case of an error with jdbc's interaction with the data source
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    public static GroupService getGroupServiceInstance() throws SQLException, IOException {
        if (groupServiceInstance == null) {
            groupServiceInstance = new GroupService();
        }
        return groupServiceInstance;
    }

    /**
     * Fetches the group from the database.
     *
     * @param groupName     the group name
     * @return Group        the group object to be fetched
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Group getGroup(String groupName) throws SQLException {
        Group g = new Group();
        final String GET_GROUP = groupProperties.getProperty("GET_GROUP");
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
     * @param groupName     the group name
     * @param modName       the moderator name of the group
     * @return boolean      true, if successful else return false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean createGroup(String groupName, String modName) throws SQLException {
        final String CREATE_GROUP = groupProperties.getProperty("CREATE_GROUP");
        pstmt = conn.getPreparedStatement(CREATE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName, modName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Delete group.
     *
     * @param groupName     the group name
     * @return boolean      true, if successful else return false
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean deleteGroup(String groupName) throws SQLException {
        final String DELETE_GROUP =
        		groupProperties.getProperty("DELETE_GROUP");
        pstmt = conn.getPreparedStatement(DELETE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Gets the member users.
     *
     * @param groupName             the group name
     * @return Set of User objects  the member users in the group
     * @throws SQLException         the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Set<User> getMemberUsers(String groupName) throws SQLException {

        final String FETCH_MEMBER_USERS = groupProperties.getProperty("FETCH_MEMBER_USERS");
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
     * Gets the member groups.
     *
     * @param groupName             the group name
     * @return Set of Group names   the member groups
     * @throws SQLException         the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Set<String> getMemberGroups(String groupName) throws SQLException {
        final String FETCH_MEMBER_GROUPS = groupProperties.getProperty("FETCH_MEMBER_GROUPS");		
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
     * Gets all the groups.
     *
     * @return  Set of Group objects    all the groups present in the system
     * @throws SQLException             the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Set<Group> getAllGroups() throws SQLException {
        final String GET_ALL_GROUP_NAMES = groupProperties.getProperty("GET_ALL_GROUP_NAMES");
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
     * @param groupName     the group name
     * @param userName      the user name
     * @return boolean      true, if is moderator
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean isModerator(String groupName, String userName) throws SQLException {
        final String GET_MODERATOR_NAME = groupProperties.getProperty("GET_MODERATOR_NAME");
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
     * @return boolean      true, if user was added successfully, false otherwise
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    public boolean addUserToGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        Set<User> users = getMemberUsers(hostGroupName);
        for (User u : users) {
            if (u.getUserName().equals(guestUserName)) return false;
        }
        final String ADD_USER_TO_GROUP = groupProperties.getProperty("ADD_USER_TO_GROUP");
        pstmt = conn.getPreparedStatement(ADD_USER_TO_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Removes the user from the given group
     *
     * @param hostGroupName     the name of the host group
     * @param guestUserName     the username of the guest user
     * @return boolean          true, if successful
     * @throws SQLException     the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean removeUserFromGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        final String REMOVE_USER_FROM_GROUP = groupProperties.getProperty("REMOVE_USER_FROM_GROUP");
        pstmt = conn.getPreparedStatement(REMOVE_USER_FROM_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /**
     * Check if users is a direct member of the group
     *
     * @param hostGroupName     the host group name
     * @param guestUserName     the username of the user to be checked for group membership
     * @return boolean          true, if user is a member of the group
     * @throws SQLException     the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean checkMembershipInGroup(String hostGroupName, String guestUserName) throws SQLException { // Assumes that the group name is valid and the group exists
        final String CHECK_USER_MEMEBERSHIP = groupProperties.getProperty("CHECK_USER_MEMEBERSHIP");
        pstmt = conn.getPreparedStatement(CHECK_USER_MEMEBERSHIP);
        pstmt = utils.setPreparedStatementArgs(pstmt, hostGroupName, guestUserName);
        result = pstmt.executeQuery();
        if(result.first()) {
        	return !result.getBoolean(IS_REMOVED);
        }
        pstmt.close();
        return false;
        
    }
    	
    
    /**
     * Gets the flat list of groups present in this group.
     *
     * @param group         the group in which we are searching for groups
     * @param descGroups    the set of descendant groups
     * @return Set          the flat list (set) of groups present in this group
     */
    private Set<String> getFlatListOfGroups(Group group, Set<String> descGroups) {
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
     * @return boolean       true, if successful
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean addGroupToGroup(String hostGroupName, String guestGroupName) throws SQLException {
        Group grp = getGroup(guestGroupName);
        Set<String> descendantGroups = new HashSet<>();
        descendantGroups = getFlatListOfGroups(grp, descendantGroups);
        if (descendantGroups.contains(hostGroupName)) {
            return false;
        } else {
            final String ADD_GROUP_TO_GROUP = groupProperties.getProperty("ADD_GROUP_TO_GROUP");
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
     * @param grpName       the name of the group in which the user name is to be checked
     * @param userName      the user name to be checked
     * @return boolean      true, if is user is a member of the group
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
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
     * @return Set      the flat list of user names strings
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
     * @return the boolean   true if the attribute was successfully updated, false otherwise
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean updateGroupSettings(String groupName, String attributeName, String attributeValue)
            throws SQLException {
        final String UPDATE_GROUP = groupProperties.getProperty("UPDATE_GROUP");
        pstmt = conn.getPreparedStatement(MessageFormat.format(UPDATE_GROUP, attributeName));
        pstmt = utils.setPreparedStatementArgs(pstmt, attributeValue, groupName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return qResult > 0;
    }

    /**
     * Retrieve all the searchable groups from the given string
     *
     * @param searchString   the string to be used in the regex to find all similar groups
     * @return Map           A hashmap containing the group names as keys and their moderator usernames as corresponding values
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Map<String, String> searchGroup(String searchString) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        final String SEARCH_GROUP = groupProperties.getProperty("SEARCH_GROUP");
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
     * @param hostGroupName  the host group name
     * @param guestGroupName the guest group name that needs to be removed
     * @return boolean       true, if successfully removes the group else returns false
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public boolean removeGroupFromGroup(String hostGroupName, String guestGroupName) throws SQLException {
        Group grp = getGroup(hostGroupName);
        Set<String> descendantGroups = new HashSet<>();
        descendantGroups = getFlatListOfGroups(grp, descendantGroups);
        if (descendantGroups.contains(guestGroupName)) {
            final String REMOVE_GROUP_FROM_GROUP = groupProperties.getProperty("REMOVE_GROUP_FROM_GROUP");
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
     * Get groups by moderator name
     *
     * @param moderatorName  the moderator whose groups need to be found
     * @return  Set          the set of group objects that the user is a moderator of
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public Set<String> getGroupsByModerator(String moderatorName) throws SQLException{
        final String GROUPS_BY_MODERATOR_QUERY = groupProperties.getProperty("GROUPS_BY_MODERATOR_QUERY");
        PreparedStatement preparedStatement = conn.getPreparedStatement(GROUPS_BY_MODERATOR_QUERY);
        preparedStatement = utils.setPreparedStatementArgs(preparedStatement, moderatorName);
        result = preparedStatement.executeQuery();
        Set<String> groups = new HashSet<>();
        while(result.next()) {
            groups.add(result.getString(GROUP_NAME));
        }
        return groups;
    }
}

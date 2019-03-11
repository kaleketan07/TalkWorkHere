package edu.northeastern.ccs.im.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;

/**
 * The Class GroupService implements the Group DAO interface and provides a set of methods that could be performed on the
 * groups in the database
 *
 * @author - Team-201 - Ketan Kale
 */
public class GroupService implements GroupDao {
    private DBConnection conn;
    private PreparedStatement pstmt = null;
    private DBUtils utils;
    private ResultSet result;
    private static GroupService groupServiceInstance;

    private static final String USER_NAME = "username";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String GROUP_NAME = "group_name";
    private static final String MODERATOR_NAME = "moderator_name";
    private static final String LOGGED_IN = "logged_in";


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

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#getGroup(java.lang.String)
     */
    @Override
    public Group getGroup(String groupName) throws SQLException {
        Group g = new Group();
        final String GET_GROUP = "SELECT * FROM groups WHERE group_name = ?";
        pstmt = conn.getPreparedStatement(GET_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        result = pstmt.executeQuery();
        if (result.first()) {
            result.first();
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

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#createGroup(edu.northeastern.ccs.im.models.Group)
     */
    @Override
    public boolean createGroup(String groupName, String modName) throws SQLException {
        final String CREATE_GROUP =
                "INSERT INTO groups (group_name, moderator_name) VALUES (?,?)";
        pstmt = conn.getPreparedStatement(CREATE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName, modName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#deleteGroup(edu.northeastern.ccs.im.models.Group)
     */
    @Override
    public boolean deleteGroup(String groupName) throws SQLException {
        final String DELETE_GROUP =
                "DELETE FROM groups WHERE group_name = ?";
        pstmt = conn.getPreparedStatement(DELETE_GROUP);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        int qResult = pstmt.executeUpdate();
        pstmt.close();
        return (qResult > 0);
    }

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#getMemberUsers(edu.northeastern.ccs.im.models.Group)
     */
    @Override
    public Set<User> getMemberUsers(String groupName) throws SQLException {

        final String FETCH_MEMBER_USERS = "WITH cte AS (SELECT * FROM prattle.groups JOIN prattle.membership_users ON prattle.groups.group_name = prattle.membership_users.host_group_name WHERE prattle.groups.group_name = ?) SELECT user_id, username, first_name, last_name FROM cte JOIN prattle.user_profile ON cte.guest_user_name = prattle.user_profile.username;";
        pstmt = conn.getPreparedStatement(FETCH_MEMBER_USERS);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        Set<User> users = new HashSet<>();
        try {
            result = pstmt.executeQuery();
            if (!result.first()) {
                throw new SQLException();
            }
            while (result.next()) {
                result.first();
                String fName = result.getString(FIRST_NAME);
                String lName = result.getString(LAST_NAME);
                String uName = result.getString(USER_NAME);
                boolean stat = result.getBoolean(LOGGED_IN);
                User user = new User(fName, lName, uName, "", stat);    // do we need a constructor without password here?
                users.add(user);
            }

        } catch (Exception e) {
            throw new SQLException();
        }
        pstmt.close();

        return users;
    }

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#getMemberGroups(edu.northeastern.ccs.im.models.Group)
     */
    @Override
    public Set<String> getMemberGroups(String groupName) throws SQLException {
        final String FETCH_MEMBER_GROUPS = "SELECT prattle.membership_groups.guest_group_name FROM prattle.groups JOIN prattle.membership_groups on prattle.groups.group_name = prattle.membership_groups.host_group_name where group_name = ?";
        pstmt = conn.getPreparedStatement(FETCH_MEMBER_GROUPS);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        Set<String> groups = new HashSet<>();
        try {
            result = pstmt.executeQuery();
            if (!result.first()) {
                throw new SQLException();
            }
            while (result.next()) {
                result.first();
                String gName = result.getString(GROUP_NAME);
                groups.add(gName);
            }

        } catch (Exception e) {
            throw new SQLException();
        }
        pstmt.close();

        return groups;
    }

    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#getAllGroups()
     */
    @Override
    public Set<Group> getAllGroups() throws SQLException {
        final String GET_ALL_GROUP_NAMES = "SELECT group_name from groups";
        pstmt = conn.getPreparedStatement(GET_ALL_GROUP_NAMES);
        Set<Group> groups = new HashSet<>();
        try {
            result = pstmt.executeQuery();
            if (!result.first()) {
                throw new SQLException();
            }
            while (result.next()) {
                result.first();
                String gName = result.getString(GROUP_NAME);
                groups.add(getGroup(gName));
            }

        } catch (Exception e) {
            throw new SQLException();
        }
        pstmt.close();

        return groups;
    }


    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#isModerator(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isModerator(String groupName, String userName) throws SQLException {
        final String GET_MODERATOR_NAME = "SELECT moderator_name from prattle.groups where group_name = ?";
        pstmt = conn.getPreparedStatement(GET_MODERATOR_NAME);
        pstmt = utils.setPreparedStatementArgs(pstmt, groupName);
        String modName;
        try {
            result = pstmt.executeQuery();
            if (!result.first()) {
                throw new SQLException();
            }
            result.first();
            modName = result.getString(MODERATOR_NAME);
        } catch (Exception e) {
            throw new SQLException();
        }
        pstmt.close();
        return userName.equals(modName);
    }


    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#addUserToGroup(java.lang.String, java.lang.String)
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


    /* (non-Javadoc)
     * @see edu.northeastern.ccs.im.services.GroupDao#addGroupToGroup(java.lang.String, java.lang.String)
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

}

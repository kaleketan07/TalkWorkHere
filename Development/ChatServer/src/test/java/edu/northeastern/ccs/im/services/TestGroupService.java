package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * The type Test group service.
 *
 * @author Kunal Patil
 */
public class TestGroupService {

    /**
     * The test GS.
     */
    private GroupService testGS;

    /**
     * The Mocked db connection.
     */
    @Mock
    DBConnection mockedDBConnection;

    /**
     * The Mocked db utils.
     */
    @Mock
    DBUtils mockedDBUtils;

    /**
     * The Mocked prepared statement.
     */
    @Mock
    PreparedStatement mockedPreparedStatement;

    /**
     * The Mocked ResultSet.
     */
    @Mock
    ResultSet mockedRS;


    /**
     * Initialise the mock objects and define their behaviors here.
     * This also sets the required reflected fields in the UserService class with
     * the mocked objects.
     *
     * @throws SQLException           the sql exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    @BeforeEach
    public void initMocks() throws SQLException, NoSuchFieldException, IllegalAccessException,
            IOException, ClassNotFoundException {
        MockitoAnnotations.initMocks(this);
        testGS = GroupService.getGroupServiceInstance();
        when(mockedDBConnection.getPreparedStatement(Mockito.anyString())).thenReturn(mockedPreparedStatement);
        when(mockedDBUtils.setPreparedStatementArgs(Mockito.any(PreparedStatement.class),
                Mockito.anyVararg()))
                .thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedRS);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockedRS.first()).thenReturn(true);
        when(mockedRS.getString("first_name")).thenReturn("ABC");
        when(mockedRS.getString("last_name")).thenReturn("BCD");
        when(mockedRS.getString("username")).thenReturn("AB");
        when(mockedRS.getString("user_password")).thenReturn("QWERTY");
        when(mockedRS.getString("group_name")).thenReturn("Group201");
        when(mockedRS.getString("moderator_name")).thenReturn("Alice");
        when(mockedRS.next()).thenReturn(true, false);
        Field rs = GroupService.class.getDeclaredField("result");
        rs.setAccessible(true);
        rs.set(testGS, mockedRS);
        Field ps = GroupService.class.getDeclaredField("pstmt");
        ps.setAccessible(true);
        ps.set(testGS, mockedPreparedStatement);
        Field db = GroupService.class.getDeclaredField("conn");
        db.setAccessible(true);
        db.set(testGS, mockedDBConnection);
        Field ut = GroupService.class.getDeclaredField("utils");
        ut.setAccessible(true);
        ut.set(testGS, mockedDBUtils);
    }

    /**
     * Tear down. Set all mocks to null
     */
    @AfterEach
    public void tearDown() {
        mockedDBConnection = null;
        mockedDBUtils = null;
        mockedPreparedStatement = null;
        mockedRS = null;
    }

    /**
     * Test create group with true.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testCreateGroupWithTrue() throws SQLException {
        Assertions.assertTrue(testGS.createGroup("ABC", "ALICE"));
    }

    /**
     * Test create group with false.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testCreateGroupWithFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(testGS.createGroup("ABC", "ALICE"));
    }

    /**
     * Test delete group with true.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testDeleteGroupWithTrue() throws SQLException {
        Assertions.assertTrue(testGS.deleteGroup("ABC"));
    }

    /**
     * Test delete group with false.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testDeleteGroupWithFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(testGS.deleteGroup("ABC"));
    }


    /**
     * Test no group matching the group name passed.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testNoGroupFetched() throws SQLException {
        when(mockedRS.first()).thenReturn(false);
        assertNull(testGS.getGroup("ABC"));
    }

    /**
     * Test get group.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetGroup() throws SQLException {
        //Will write this test once I have a better understanding of
        //getMemberGroups() and getMemberUsers()
        when(mockedRS.next()).thenReturn(true, true, false, true, true, false);
        when(mockedRS.getString("group_name")).thenReturn("TempGroup", "TestGroup");
        Assertions.assertEquals("TempGroup", testGS.getGroup("TempGroup").getGroupName());

    }

    /**
     * Test get member groups. Should test getting member groups of a group
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMemberGroups() throws SQLException {
        Set<String> testGroupSet = new HashSet<>();
        testGroupSet.add("Group201");
        Assertions.assertEquals(testGroupSet.size(), testGS.getMemberGroups("ABC").size());
    }

    /**
     * Test get member groups check exception.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMemberGroupsCheckException() throws SQLException {
        doThrow(SQLException.class).when(mockedRS).next();
        Assertions.assertThrows(SQLException.class, () -> testGS.getMemberGroups("ABC"));
    }

    /**
     * Test get member groups cover try catch block.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMemberGroupsCoverTryCatchBlock() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> testGS.getMemberGroups("ABC"));
    }

    /**
     * Test getting all groups.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetAllGroups() throws SQLException {
        Set<String> testGroupSet = new HashSet<>();
        testGroupSet.add("Group201");
        Assertions.assertEquals(testGroupSet.size(), testGS.getAllGroups().size());
    }

    /**
     * Test get all groups check exception.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetAllGroupsCheckException() throws SQLException {
        doThrow(SQLException.class).when(mockedRS).next();
        Assertions.assertThrows(SQLException.class, () -> testGS.getAllGroups());
    }

    /**
     * Test get all groups cover try catch block.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetAllGroupsCoverTryCatchBlock() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> testGS.getAllGroups());
    }

    /**
     * Test get member users.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMemberUsers() throws SQLException {
        Set<User> testUserSet = new HashSet<>();
        User testUser = new User("ABC", "BCD", "AB", "QWERTY", true);
        testUserSet.add(testUser);
        Assertions.assertEquals(testUserSet.size(), testGS.getMemberUsers("ABC").size());
    }

    /**
     * Test get members users check exception.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMembersUsersCheckException() throws SQLException {
        doThrow(SQLException.class).when(mockedRS).next();
        Assertions.assertThrows(SQLException.class, () -> testGS.getMemberUsers("ABC"));
    }

    /**
     * Test get member users cover try catch block.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetMemberUsersCoverTryCatchBlock() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> testGS.getMemberUsers("ABC"));
    }

    /**
     * Test is moderator for true.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testIsModeratorForTrue() throws SQLException {
        Assertions.assertTrue(testGS.isModerator("ABC", "Alice"));
    }

    /**
     * Test is moderator for false.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testIsModeratorForFalse() throws SQLException{
        when(mockedRS.first()).thenReturn(false);
        Assertions.assertFalse(testGS.isModerator("ABC","Alice"));
    }

    /**
     * Test is moderator cover try catch block.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testIsModeratorCoverTryCatchBlock() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        Assertions.assertThrows(SQLException.class, () -> testGS.isModerator("ABC", "Alice"));
    }

    /**
     * Test add user to group with existing user.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddUserToGroupWithExistingUser() throws SQLException {
        Assertions.assertFalse(testGS.addUserToGroup("ABC", "AB"));
    }

    /**
     * Test add user to group with existing user.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupWithExistingUser() throws SQLException {
    	Assertions.assertTrue(testGS.removeUserFromGroup("ABC", "AB"));
    }
    
    /**
     * Test add user to group with existing user.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupWithNonUser() throws SQLException {
    	when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
    	Assertions.assertFalse(testGS.removeUserFromGroup("ABC", "AB"));
    }
    
    /**
     * Test add user to group with non existing user.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddUserToGroupWithNonExistingUser() throws SQLException {
        Assertions.assertTrue(testGS.addUserToGroup("ABC", "ABC"));
    }

    /**
     * Test add user to group with no queries affected.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddUserToGroupWithNoQueriesAffected() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(testGS.addUserToGroup("ABC", "ABC"));
    }

    /**
     * Test add group to group with host in descendants of guest.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWithHostInDescendantsOfGuest() throws SQLException {
        Assertions.assertFalse(testGS.addGroupToGroup("Group201", "ABC"));
    }

    /**
     * Test add group to group with host not in descendants of guest.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWithHostNotInDescendantsOfGuest() throws SQLException {
        Assertions.assertTrue(testGS.addGroupToGroup("BCD", "ABC"));
    }

    /**
     * Test add group to group with no queries affected.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWithNoQueriesAffected() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(testGS.addGroupToGroup("ABC", "ABC"));
    }

    /**
     * Test add group to group with nested groups.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWithNestedGroups() throws SQLException {
        when(mockedRS.next()).thenReturn(true, true, false, true, true, false);
        when(mockedRS.getString("group_name")).thenReturn("Group201", "Group202");
        when(mockedRS.getString("moderator_name")).thenReturn("Alice", "Bob");
        Assertions.assertTrue(testGS.addGroupToGroup("ABC", "ABC"));
    }


}

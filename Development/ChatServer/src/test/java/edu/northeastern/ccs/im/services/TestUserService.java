package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.db.DBConnection;
import edu.northeastern.ccs.im.db.DBUtils;
import edu.northeastern.ccs.im.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;


/**
 * The class for testing user services DAO.
 *
 * @author Kunal
 */
public class TestUserService {

    private User testUser;
    private UserService us;

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
     * The Mocked ResultSet
     */
    @Mock
    ResultSet mockedRS;


    /**
     * Initialise the mock objects and define their behaviors here.
     * This also sets the required reflected fields in the UserService class with
     * the mocked objects.
     *
     * @throws SQLException the sql exception
     */
    @BeforeEach
    public void initMocks() throws SQLException, NoSuchFieldException, IllegalAccessException,
            IOException, ClassNotFoundException {
        MockitoAnnotations.initMocks(this);
        us = UserService.getInstance();
        testUser = new User("ABC", "BCD", "AB", "QWERTY", false);
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
        when(mockedRS.getBoolean("logged_in")).thenReturn(false);
        when(mockedRS.next()).thenReturn(true, false);
        Field rs = UserService.class.getDeclaredField("result");
        rs.setAccessible(true);
        rs.set(us, mockedRS);
        Field ps = UserService.class.getDeclaredField("pstmt");
        ps.setAccessible(true);
        ps.set(us, mockedPreparedStatement);
        Field db = UserService.class.getDeclaredField("conn");
        db.setAccessible(true);
        db.set(us, mockedDBConnection);
        Field ut = UserService.class.getDeclaredField("utils");
        ut.setAccessible(true);
        ut.set(us, mockedDBUtils);
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
     * Test get user by user name.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetUserByUserName() throws SQLException {
        Assertions.assertEquals("AB : ABC BCD", us.getUserByUserName("AB").toString());
    }

    /**
     * Test get user by user name and password.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetUserByUserNameAndPassword() throws SQLException {
        Assertions.assertEquals("AB : ABC BCD",
                us.getUserByUserNameAndPassword("AB", "QWERTY").toString());
    }

    /**
     * Test create user.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testCreateUser() throws SQLException {
        Assertions.assertTrue(us.createUser(testUser));
    }

    /**
     * Test delete user.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testDeleteUser() throws SQLException {
        Assertions.assertTrue(us.deleteUser(testUser));
    }

    /**
     * Test update user.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUser() throws SQLException {
        Assertions.assertTrue(us.updateUser(testUser));
    }

    /**
     * Test the getAllUsers() method of UserService class
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetAllUsers() throws SQLException, IllegalAccessException, NoSuchFieldException {
        Assertions.assertEquals(1, us.getAllUsers().size());
    }

    /**
     * Test get all users exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetAllUsersException() throws SQLException {
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, () -> us.getAllUsers());
    }

    /**
     * Test get user by username exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetUserByUsernameException() throws SQLException {
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, () -> us.getUserByUserName("AB"));
    }

    /**
     * Test get user by username and password exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testGetUserByUsernameAndPasswordException() throws SQLException {
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, () -> us.getUserByUserNameAndPassword("AB", "BA"));
    }

    /**
     * Test create user exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testCreateUserException() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, () -> us.createUser(testUser));
    }

    /**
     * Test update user exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserException() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, () -> us.updateUser(testUser));
    }

    /**
     * Test delete user exception.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testDeleteUserException() throws SQLException {
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, () -> us.deleteUser(testUser));
    }

    /**
     * Test delete user for false.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testDeleteUserForFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.deleteUser(testUser));
    }

    /**
     * Test update user false.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.updateUser(testUser));
    }

    /**
     * Test create user false.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testCreateUserFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.createUser(testUser));
    }

    /**
     * Test update user attributes for true.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributesForTrue() throws SQLException {
        Assertions.assertTrue(us.updateUserAttributes("ABC","first_name","XYZ"));
    }

    /**
     * Test update user attributes for false.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributesForFalse() throws SQLException {
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.updateUserAttributes("ABC","last_name","DEF"));
    }

    /**
     * Test if a null is sent instead of user when no matching result is found
     * @throws SQLException thrown by downstream SQL calls
     */
    @Test
    public void testNullConditionGetUserByUsernameAndPassword() throws SQLException{
        when(mockedRS.first()).thenReturn(false);
        Assertions.assertNull(us.getUserByUserNameAndPassword(USER, PASS));
    }

    /**
     * Test if a null is sent instead of user when no matching result is found
     * @throws SQLException thrown by downstream SQL calls
     */
    @Test
    public void testNullConditionGetUserByUsername() throws SQLException{
        when(mockedRS.first()).thenReturn(false);
        Assertions.assertNull(us.getUserByUserName(USER));
    }

    /**
     * Test update user attribute when the given attribute is not searchable attribute
     * when database operations go as expected.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenNotSearchableForTrue() throws SQLException{
        Assertions.assertTrue(us.updateUserAttributes("ABC","first_name","NewABC"));
    }

    /**
     * Test update user attribute when the given attribute is not searchable attribute
     * when database operations go as expected but there no user to be found of that username
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenNotSearchableForFalse() throws SQLException{
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.updateUserAttributes("ABC","first_name","NewABC"));
    }

    /**
     * Test update user attribute when database operations throw an exception
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenException() throws SQLException{
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class,() -> us.updateUserAttributes("ABC","first_name","NewABC"));
    }

    /**
     * Test update user attribute when the given attribute is the searchable attribute and
     * when database operations go as expected and input is "1"
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenSearchableIsTrueForOne() throws SQLException{
        Assertions.assertTrue(us.updateUserAttributes("ABC","user_searchable","1"));
    }

    /**
     * Test update user attribute when the given attribute is the searchable attribute and
     * when database operations go as expected and input is "True"
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenSearchableIsTrueForTrueValue() throws SQLException{
        Assertions.assertTrue(us.updateUserAttributes("ABC","user_searchable","True"));
    }


    /**
     * Test update user attribute when the given attribute is the searchable attribute and
     * when database operations go as expected and input is "False"
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenSearchableIsTrueForFalseValue() throws SQLException{
        Assertions.assertTrue(us.updateUserAttributes("ABC","user_searchable","False"));
    }

    /**
     * Test update user attribute when the given attribute is the searchable attribute and
     * when database operations go as expected and input is "0"
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenSearchableIsTrueFor0Value() throws SQLException{
        Assertions.assertTrue(us.updateUserAttributes("ABC","user_searchable","0"));
    }

    /**
     * Test update user attribute when the given attribute is the searchable attribute but the value passed is
     * not boolean
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUpdateUserAttributeWhenSearchableButInvalidParameterPassed() throws SQLException{
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.updateUserAttributes("ABC","user_searchable",
                "SomethingElse"));
    }

    private static final String USER = "user";
    private static final String PASS = "pass";
}

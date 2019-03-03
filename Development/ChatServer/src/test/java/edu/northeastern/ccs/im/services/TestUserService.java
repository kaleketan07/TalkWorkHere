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
 * @author Kunal
 */
public class TestUserService {

    private User testUser ;
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
        testUser = new User("ABC","BCD","AB","QWERTY");
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
        when(mockedRS.next()).thenReturn(true,false);
        Field rs = UserService.class.getDeclaredField("result");
        rs.setAccessible(true);
        rs.set(us,mockedRS);
        Field ps = UserService.class.getDeclaredField("pstmt");
        ps.setAccessible(true);
        ps.set(us,mockedPreparedStatement);
        Field db = UserService.class.getDeclaredField("conn");
        db.setAccessible(true);
        db.set(us,mockedDBConnection);
        Field ut = UserService.class.getDeclaredField("utils");
        ut.setAccessible(true);
        ut.set(us,mockedDBUtils);
    }

    /**
     * Tear down. Set all mocks to null
     */
    @AfterEach
    public void tearDown(){
        mockedDBConnection = null;
        mockedDBUtils = null;
        mockedPreparedStatement = null;
        mockedRS = null;
    }

    /**
     * Test get user by user name.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetUserByUserName() throws SQLException{
        Assertions.assertEquals("AB : ABC BCD",us.getUserByUserName("AB").toString());
    }

    /**
     * Test get user by user name and password.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetUserByUserNameAndPassword() throws SQLException{
        Assertions.assertEquals("AB : ABC BCD",
                us.getUserByUserNameAndPassword("AB","QWERTY").toString());
    }

    /**
     * Test create user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testCreateUser() throws SQLException{
        Assertions.assertTrue(us.createUser(testUser));
    }

    /**
     * Test delete user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testDeleteUser() throws SQLException{
        Assertions.assertTrue(us.deleteUser(testUser));
    }

    /**
     * Test update user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateUser() throws SQLException{
        Assertions.assertTrue(us.updateUser(testUser));
    }

    /**
     * Test the getAllUsers() method of UserService class
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetAllUsers() throws SQLException,IllegalAccessException,NoSuchFieldException{
        Assertions.assertEquals(1,us.getAllUsers().size());
    }

    /**
     * Test get all users exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetAllUsersException()  throws SQLException{
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, ()->us.getAllUsers());
    }

    /**
     * Test get user by username exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetUserByUsernameException()  throws SQLException{
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, ()->us.getUserByUserName("AB"));
    }

    /**
     * Test get user by username and password exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetUserByUsernameAndPasswordException()  throws SQLException{
        when(mockedPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        Assertions.assertThrows(SQLException.class, ()->us.getUserByUserNameAndPassword("AB","BA"));
    }

    /**
     * Test create user exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testCreateUserException()  throws SQLException{
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, ()->us.createUser(testUser));
    }

    /**
     * Test update user exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateUserException()  throws SQLException{
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, ()->us.updateUser(testUser));
    }

    /**
     * Test delete user exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testDeleteUserException()  throws SQLException{
        doThrow(SQLException.class).when(mockedPreparedStatement).executeUpdate();
        Assertions.assertThrows(SQLException.class, ()->us.deleteUser(testUser));
    }

    /**
     * Test get user by username and password when user not present.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetUserByUsernameAndPasswordUserNotPresent() throws SQLException{
        when(mockedRS.first()).thenReturn(false);
        Assertions.assertThrows(SQLException.class, ()->us.getUserByUserNameAndPassword("AB","QWERTY"));
    }

    @Test
    public void testDeleteUserForFalse() throws SQLException{
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.deleteUser(testUser));
    }

    @Test
    public void testUpdateUserFalse() throws SQLException{
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.updateUser(testUser));
    }

    @Test
    public void testCreateUserFalse() throws SQLException{
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);
        Assertions.assertFalse(us.createUser(testUser));
    }

}

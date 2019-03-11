package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.services.ConversationalMessageService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TestUser {


    /**
     * Test user to string method.
     */
    @Test
    public void testUserToStringMethod() {
        assertEquals("tomharris : Tom Harris", TOM.toString());
    }

    /**
     * Test the set and get method for UserName
     */
    @Test
    public void testSetUserName() {
        ALICE.setUserName(ALICERUBY);
        assertEquals(ALICE.getUserName(), ALICERUBY);
    }

    /**
     * Test the set and get method for FirstName
     */
    @Test
    public void testSetFirstName() {
        ALICE.setFirstName(RUBY);
        assertEquals(ALICE.getFirstName(), RUBY);
    }

    /**
     * Test the set and get method for LastName
     */
    @Test
    public void testSetLastName() {
        ALICE.setLastName(RUBY);
        assertEquals(ALICE.getLastName(), RUBY);
    }

    /**
     * Test the set and get method for UserPassword
     */
    @Test
    public void testSetUserPassword() {
        ALICE.setUserPassword(RUBY);
        assertEquals(ALICE.getUserPassword(), RUBY);
    }

    /**
     * Test the setter and getter for loggedIn attribute when set to true
     */
    @Test
    public void testSetLoggedInTrue() {
        ALICE.setLoggedIn(true);
        assertTrue(ALICE.isLoggedIn());
    }

    /**
     * Test the setter and getter for loggedIn attribute when set to false
     */
    @Test
    public void testSetLoggedInFalse() {
        ALICE.setLoggedIn(false);
        assertFalse(ALICE.isLoggedIn());
    }


    /**
     * Test user send message when ClientRunnable is Null
     *
     * @throws SQLException           the sql exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException            the io exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testUserSendMessageWhenClientRunnableIsNull() throws SQLException, ClassNotFoundException, IOException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        Field fieldCR = User.class.getDeclaredField("clientRunnable");
        fieldCR.setAccessible(true);
        fieldCR.set(ALICE, mockedClientRunnable);
        ALICE.userSendMessage(BROADCAST_FROM_ALICERUBY);
        verify(mockedCMS, times(1)).insertConversationalMessage(ALICERUBY, ALICE.getUserName(),
                "Hello, Alice", false);
    }

    /**
     * Test user send message when clientRunnable is not null.
     *
     * @throws SQLException           the sql exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException            the io exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testUserSendMessageWhenClientRunnableNotNull() throws SQLException, ClassNotFoundException, IOException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        Field fieldCR = User.class.getDeclaredField("clientRunnable");
        fieldCR.setAccessible(true);
        fieldCR.set(ALICE, mockedClientRunnable);
        //Set the mockedMap in ClientRunnable so the static instance will retrieve it
        Map<String, ClientRunnable> userClients = new HashMap<>();
        userClients.put(ALICE.getUserName(), mockedClientRunnable);
        Field mapField = ClientRunnable.class.getDeclaredField("userClients");
        mapField.setAccessible(true);
        mapField.set(mockedClientRunnable, userClients);
        ALICE.userSendMessage(BROADCAST_FROM_ALICERUBY);
        verify(mockedCMS, times(1)).insertConversationalMessage(ALICERUBY, ALICE.getUserName(),
                "Hello, Alice", true);
    }

    private static final User TOM = new User("Tom", "Harris", "tomharris", "123", false);
    private static final User ALICE = new User("Alice", "Bob", "alicebob", "password", false);
    private static final String RUBY = "RUBY";
    private static final String ALICERUBY = "aliceruby";
    private static final Message BROADCAST_FROM_ALICERUBY = Message.makeBroadcastMessage(ALICERUBY, "Hello, Alice");

}

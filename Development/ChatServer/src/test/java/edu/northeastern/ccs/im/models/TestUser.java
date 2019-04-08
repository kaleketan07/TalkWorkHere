package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.services.ConversationalMessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
     * Test is tapped for false.
     */
    @Test
    public void testIsTappedForFalse() {
        ALICE.setTapped(false);
        assertFalse(ALICE.isTapped());
    }
    
    /**
     * Test is tapped for true.
     */
    @Test
    public void testIsTappedForTrue() {
        ALICE.setTapped(true);
        assertTrue(ALICE.isTapped());
    }

    /**
     * Test user send message when ClientRunnable is Null and not initialized
     *
     * @throws SQLException           the sql exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException            the io exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testUserSendMessageWhenClientRunnableIsNull() throws SQLException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        when(mockedClientRunnable.isInitialized()).thenReturn(false);
        Field fieldCR = User.class.getDeclaredField("clientRunnable");
        fieldCR.setAccessible(true);
        fieldCR.set(ALICE, mockedClientRunnable);
        ALICE.userSendMessage(BROADCAST_FROM_ALICERUBY);
        verify(mockedCMS, times(1)).insertConversationalMessage(ALICERUBY, ALICE.getUserName(),
                "Hello, Alice", false);
    }

    /**
     * Test user send message when client runnable is null but initialized.
     *
     * @throws SQLException           the sql exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testUserSendMessageWhenClientRunnableIsNullButInitialized() throws SQLException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        when(mockedClientRunnable.isInitialized()).thenReturn(true);
        Field fieldCR = User.class.getDeclaredField("clientRunnable");
        fieldCR.setAccessible(true);
        fieldCR.set(ALICE, mockedClientRunnable);
        ALICE.userSendMessage(BROADCAST_FROM_ALICERUBY);
        verify(mockedCMS, times(1)).insertConversationalMessage(ALICERUBY, ALICE.getUserName(),
                "Hello, Alice", true);
    }


    /**
     * Test user send message when clientRunnable is not null and initialized.
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
        when(mockedClientRunnable.isInitialized()).thenReturn(true);
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

    /**
     * Test user send message when client runnable not null but not initialized.
     *
     * @throws SQLException           the sql exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException            the io exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testUserSendMessageWhenClientRunnableNotNullButNotInitialized() throws SQLException, ClassNotFoundException, IOException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        when(mockedClientRunnable.isInitialized()).thenReturn(false);
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
                "Hello, Alice", false);
    }

    /**
     * Test setter and getter for searchable attribute when true.
     */
    @Test
    public void testSetterAndGetterForSearchableAttributeWhenTrue() {
        ALICE.setSearchable(true);
        assertTrue(ALICE.isSearchable());
    }

    /**
     * Test setter and getter for searchable attribute when false.
     */
    @Test
    public void testSetterAndGetterForSearchableAttributeWhenFalse() {
        ALICE.setSearchable(false);
        assertFalse(ALICE.isSearchable());
    }

    @Test
    public void testUserSendMessageWhenClientRunnableNotNullWithGroupMessage() throws SQLException, ClassNotFoundException, IOException, NoSuchFieldException,
            IllegalAccessException {
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = User.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(ALICE, mockedCMS);
        ClientRunnable mockedClientRunnable = mock(ClientRunnable.class);
        when(mockedClientRunnable.isInitialized()).thenReturn(true);
        Field fieldCR = User.class.getDeclaredField("clientRunnable");
        fieldCR.setAccessible(true);
        fieldCR.set(ALICE, mockedClientRunnable);
        //Set the mockedMap in ClientRunnable so the static instance will retrieve it
        Map<String, ClientRunnable> userClients = new HashMap<>();
        userClients.put(ALICE.getUserName(), mockedClientRunnable);
        Field mapField = ClientRunnable.class.getDeclaredField("userClients");
        mapField.setAccessible(true);
        mapField.set(mockedClientRunnable, userClients);
        ALICE.userSendMessage(GROUP_MESSAGE_FROM_ALICE);
        verify(mockedCMS, times(1)).insertConversationalMessage(ALICERUBY, ALICE.getUserName(),
                "Hello, Alice", true);
    }


    /**
     * Test equals method for false.
     */
    @Test
    public void testEqualsMethodForFalse() {
        assertFalse(ALICE.equals(TOM));
    }

    /**
     * Test equals method for true.
     */
    @Test
    public void testEqualsMethodForTrue() {
        assertTrue(ALICE.equals(GARY));
    }

    /**
     * Test equals method for different type.
     */
    @Test
    public void testEqualsMethodForDifferentType() {
        assertFalse(ALICE.equals(null));
    }

    /**
     * Test hash code for null user name.
     */
    @Test
    public void testHashCodeForNullGroupName() {
        assertTrue(NULLESH.hashCode() == 0);
    }

    /**
     * Test hash code for non null user name.
     */
    @Test
    public void testHashCodeForNonNullUserName() {
        assertTrue(ALICE.hashCode() == 31 * ALICEBOB.hashCode());
    }


    private static final User TOM = new User("Tom", "Harris", "tomharris", "123", false);
    private static final User ALICE = new User("Alice", "Bob", "alicebob", "password", false);
    private static final User GARY = new User("Alice", "Bob", "alicebob", "password", false);
    private static final User NULLESH = new User("Alice", "Bob", null, "password", false);
    private static final String ALICEBOB = "alicebob";
    private static final String RUBY = "RUBY";
    private static final String ALICERUBY = "aliceruby";
    private static final String DUMMY_GROUP_NAME = "dummy group";
    private static final Message BROADCAST_FROM_ALICERUBY = Message.makeBroadcastMessage(ALICERUBY, "Hello, Alice");
    private static final Message GROUP_MESSAGE_FROM_ALICE = Message.makeGroupMessage(ALICERUBY, "Hello, Alice", DUMMY_GROUP_NAME);

}

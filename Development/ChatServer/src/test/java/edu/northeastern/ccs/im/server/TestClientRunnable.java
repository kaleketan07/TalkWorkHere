package edu.northeastern.ccs.im.server;

import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.channels.SocketChannel;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.NetworkConnection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestClientRunnable class contains all the unit tests for the java class ClientRunnable
 * It has various test for each of the method and also for testing various branch conditions
 * using desired scenarios
 *
 * @author Team 201 - rahul
 * @version 1.0
 */
public class TestClientRunnable {

    /**
     * Sets up the static fields "invalidCounter" and "userClients" before each test,
     * so that they can be reused.
     *
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @BeforeEach
    public void setUp() throws IllegalAccessException,NoSuchFieldException{
        Field hm = ClientRunnable.class.getDeclaredField("userClients");
        hm.setAccessible(true);
        Map<String,ClientRunnable> userClientsEmpty = new HashMap<>();
        userClientsEmpty.clear();
        hm.set(null,userClientsEmpty);
        Field ic = ClientRunnable.class.getDeclaredField("invalidCounter");
        ic.setAccessible(true);
        ic.set(null,0);
    }

    /**
     * Test to check weather run() changes the status of the initialized data member
     * to true in the first run with using a message iterator with one message
     */
    @Test
    public void testRun() {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Test to check weather run() changes the status of the initialized data member
     * to true in the first run with using a message iterator with one message
     */
    @Test
    public void testRunSameClient() {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }




    /**
     * Testing method Set User Name by given a string as input and using
     * get method to assert the test
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
    public void testSetNameandGetName() throws IOException {

        SocketChannel client = SocketChannel.open();
        NetworkConnection networkConnectionMock = new NetworkConnection(client);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.setName(SENDER_NAME);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }


    /**
     * Testing handleIncomingMessage() and HandleOutGoingMessage() method with
     * true Condition of initialized data member by running run() method twice
     */
    @Test
    public void testHandleIncomingMessageandHandleOutGoingMessage() {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Testing handleIncomingMessage() where the Message iterator does
     * not have broadcast message
     */
    @Test
    public void testHandleIncomingMessageWithoutBroadcastMessage() {
        Message login = Message.makeSimpleLoginMessage(TestClientRunnable.SENDER_NAME);
        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(login);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Test to check handleIncomingMessage() with makeHelloMessage in Message iterator
     * Thus enqueue the message in the waitlist, Also tests method sendMessage
     * Test can be done by asserting initialized data variable which is set after first run()
     *
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws IllegalArgumentException  the illegal Argument exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     */
    @Test
    public void testHandleIncomingMessageAndEnqueueMessage() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        List<Message> messageList = new ArrayList<>();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        Message loginMessage = Message.makeSimpleLoginMessage(SENDER_NAME);
        messageList.add(loginMessage);
        messageList.add(helloMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        clientRunnableObject.run();

        assertTrue(clientRunnableObject.isInitialized());
    }


    /**
     * Testing setFuture() using ScheduledFuture Object and also testing the
     * case of a quit(terminate Message as a part of handleIncomingMessages
     * method
     */
    @Test
    public void testHandleIncomingMessageTerminateCondition() {

        List<Message> messageList = new ArrayList<>();
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        messageList.add(BROADCAST);
        messageList.add(quitMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Testing setFuture() using ScheduledFuture
     *
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the underlying reflection method call throws an exception
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     */
    @Test
    public void testSetFutureMethod() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Message login = Message.makeSimpleLoginMessage(TestClientRunnable.SENDER_NAME);
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        List<Message> messageList = new ArrayList<>();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        messageList.add(login);
        messageList.add(helloMessage);
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.enqueueMessage(BROADCAST);
        clientRunnableObject.enqueueMessage(BROADCAST);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Empty waitList
     */
    @Test
    public void testHandleIncomingMessageWithEmptyIterator() {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        messageList.clear();
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();

    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Login waitList
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForInvalidUser() throws NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        messageList.clear();
        messageList.add(LOGIN);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Login in waitList and login successful
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForValidUserSuccessfulLogin() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        User u = Mockito.mock(User.class);
        Mockito.when(mockedUserService.getUserByUserNameAndPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(u);
        Mockito.when(mockedUserService.updateUser(u)).thenReturn(true);
        messageList.clear();
        messageList.add(LOGIN);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Login in waitList and login successful
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForValidUserUnsuccessfulLogin() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        User u = Mockito.mock(User.class);
        Mockito.when(mockedUserService.getUserByUserNameAndPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(u);
        Mockito.when(mockedUserService.updateUser(u)).thenReturn(false);
        messageList.clear();
        messageList.add(LOGIN);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Testing checkForIntialization() Method using with Empty
     * message Iterator from network connection
     */
    @Test
    public void testCheckInitialization() {

        List<Message> messageList = new ArrayList<>();
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        assertEquals(clientRunnableObject.isInitialized(), false);

    }

    /**
     * Testing setUserName() using Null as input
     *
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     */
    @Test
    public void testSetUserName() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        List<Message> messageList = new ArrayList<>();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        messageList.add(helloMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();

    }

    /**
     * Test Enqueue Message WithOut run() thus not changing value of
     * initialized  variable
     *
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException if  the underlying reflection method call throws an exception .
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     */
    @Test
    public void testEnqueueMessage() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        List<Message> messageList = new ArrayList<>();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        messageList.add(helloMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnection = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnection.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnection);
        clientRunnableObject.enqueueMessage(helloMessage);
        assertEquals(clientRunnableObject.isInitialized(), false);

    }


    /**
     * Testing isBehind() condition in run() by setting it to True
     *
     * @throws InvocationTargetException the underlying reflection method call throws an exception
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     * @throws NoSuchFieldException      the no such field exception to be used while using java Reflection
     */
    @Test
    public void testTimerIsBehind() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {

        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        List<Message> messageList = new ArrayList<>();
        messageList.add(helloMessage);
        messageList.add(quitMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ClientTimer ct = Mockito.mock(ClientTimer.class);
        Field privateStringField = ClientRunnable.class.getDeclaredField("timer");
        privateStringField.setAccessible(true);
        privateStringField.set(clientRunnableObject, ct);
        Mockito.when(ct.isBehind()).thenReturn(true);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        assertEquals(clientRunnableObject.isInitialized(), false);

    }


    /**
     * Testing setUserName using a valid string Input Which sets the user name
     */
    @Test
    public void testSetUserNameWithValidString() {

        List<Message> ml = new ArrayList<>();
        Message quit = Message.makeQuitMessage(SENDER_NAME);
        ml.add(BROADCAST);
        ml.add(BROADCAST);
        ml.add(quit);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);

    }

    /**
     * Test getUserId by setting the userId and retrieving it using getUserID()
     *
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testGetUserId() throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = ClientRunnable.class.
                getDeclaredField("userId");
        privateStringField.setAccessible(true);
        List<Message> messageList = new ArrayList<>();
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnection = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnection.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnection);
        privateStringField.set(clientRunnableObject, USER_ID);
        assertEquals(USER_ID, clientRunnableObject.getUserId());
    }

    /**
     * Test messageCheck() with condition where msg.getName is not null
     * and msg.getName() returns same as getName()
     * Condition one True condition two true
     *
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     */
    @Test
    public void testMessageChecksConditionOne() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = ClientRunnable.class.getDeclaredMethod("messageChecks", Message.class);
        method.setAccessible(true);
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.setName("test");
        method.invoke(clientRunnableObject, Message.makeBroadcastMessage("test", "test1"));
    }

    /**
     * Test messageCheck() with condition where msg.getName is not null
     * and msg.getName() does not return same as getName()
     * Condition one True condition two false
     *
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     */
    @Test
    public void testMessageChecksConditionTwo() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = ClientRunnable.class.getDeclaredMethod("messageChecks", Message.class);
        method.setAccessible(true);
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.setName("Test1");
        method.invoke(clientRunnableObject, Message.makeBroadcastMessage("test", "test1"));
    }

    /**
     * Test messageCheck() with condition where msg.getName is null
     * and msg.getName() does not return same as getName()
     * Condition one false condition two false
     *
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws IllegalArgumentException  the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     */
    @Test
    public void testMessageConditionThree() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = ClientRunnable.class.getDeclaredMethod("messageChecks", Message.class);
        method.setAccessible(true);
        NetworkConnection nc = Mockito.mock(NetworkConnection.class);
        ClientRunnable clientRunnableObject = new ClientRunnable(nc);
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, "hello");
        method.invoke(clientRunnableObject, helloMessage);
    }

    /**
     * This test verifies if the correct client is returned based on the given username
     */
    @Test
    public void testGetClientByUsername() {
        List<Message> ml = new ArrayList<>();
        Message quit = Message.makeQuitMessage(SENDER_NAME);
        ml.add(BROADCAST);
        ml.add(BROADCAST);
        ml.add(quit);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        ClientRunnable senderClient = ClientRunnable.getClientByUsername(SENDER_NAME);
        assertEquals(SENDER_NAME, senderClient.getName());
    }

    /**
     * This test verifies if null is returned based on the given username
     */
    @Test
    public void testGetClientByUsernameNonExistingUser() {
        assertNull(ClientRunnable.getClientByUsername("someRandomUsername"));
    }

    @Test
    public void testSetUserNameForAlreadyExistingUsers(){
        List<Message> ml = new ArrayList<>();
        ml.add(BROADCAST);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);

        List<Message> ml2 = new ArrayList<>();
        ml2.add(BROADCAST);
        Iterator<Message> messageIter2 = ml2.iterator();
        NetworkConnection networkConnectionMock2 = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock2.iterator()).thenReturn(messageIter2);
        ClientRunnable clientRunnableObject2 = new ClientRunnable(networkConnectionMock2);
        clientRunnableObject2.run();
        assertEquals(clientRunnableObject2.getUserId(), -1);
        assertNotEquals(clientRunnableObject2.getName(), SENDER_NAME);
        assertEquals("invalid-"+SENDER_NAME+"1",clientRunnableObject2.getName());
    }

    //Private fields to be used in tests
    private static final Message LOGIN = Message.makeLoginMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.PASS);
    private static final Message BROADCAST = Message.makeBroadcastMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.MESSAGE_TEXT);
    private static final int USER_ID = 120000;
    private static final String SENDER_NAME = "Alice";
    private static final String MESSAGE_TEXT = "Hello, I am Alice";
    private static final String PASS = "some_p@$$worD";
}

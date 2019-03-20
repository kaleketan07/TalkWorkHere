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
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.ConversationalMessageService;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mysql.cj.x.protobuf.MysqlxDatatypes.Any;

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
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field hm = ClientRunnable.class.getDeclaredField("userClients");
        hm.setAccessible(true);
        Map<String, ClientRunnable> userClientsEmpty = new HashMap<>();
        userClientsEmpty.clear();
        hm.set(null, userClientsEmpty);
        Field ic = ClientRunnable.class.getDeclaredField("invalidCounter");
        ic.setAccessible(true);
        ic.set(null, 0);
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
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
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
     * Test handleIncomingMessage() empty message Iterator form network connect    ion
     * which also tests the handleOutgoingMessage() with Login in waitList and     login successful
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserUnsuccessfulRegister() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        User u = new User("rahul", "bhat", null, null, true);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(u);
        messageList.clear();
        messageList.add(REGISTER);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        
        clientRunnableObject.run();
    }
    
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Private_User message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateWithValidDestAddress() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        User mockedUser = Mockito.mock(User.class);
        mockedUser.setLoggedIn(true);
        Message mockedMessage = Mockito.mock(Message.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        User u = new User(SENDER_NAME, null, SENDER_NAME,null, true);
        Mockito.doNothing().when(mockedUser).userSendMessage(mockedMessage);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(u,mockedUser);
        
        messageList.clear();
        messageList.add(PRIVATE_MESSAGE);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }
    
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithValidDestAddress() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        ConversationalMessageService mockedcms = Mockito.mock(ConversationalMessageService.class);
        User mockedUser = Mockito.mock(User.class);
        mockedUser.setLoggedIn(true);
        Message mockedMessage = Mockito.mock(Message.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Field cmService = ClientRunnable.class.getDeclaredField("conversationalMessagesService");
        cmService.setAccessible(true);
        cmService.set(clientRunnableObject, mockedcms);
        Mockito.doNothing().when(mockedUser).userSendMessage(mockedMessage);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON,mockedUser);
        Mockito.when(mockedcms.getSender(Mockito.anyString())).thenReturn(SENDER_NAME);
        messageList.clear();
        mockedUser.setLoggedIn(true);
        messageList.add(PRIVATE_REPLY);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }
    
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithInvalidDestAddress() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        ConversationalMessageService mockedcms = Mockito.mock(ConversationalMessageService.class);
        User mockedUser = Mockito.mock(User.class);
        mockedUser.setLoggedIn(true);
        Message mockedMessage = Mockito.mock(Message.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Field cmService = ClientRunnable.class.getDeclaredField("conversationalMessagesService");
        cmService.setAccessible(true);
        cmService.set(clientRunnableObject, mockedcms);
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.doNothing().when(mockedUser).userSendMessage(mockedMessage);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON, USER_LOGGED_ON);
        Mockito.when(mockedcms.getSender(Mockito.anyString())).thenReturn(null);
        messageList.clear();
        mockedUser.setLoggedIn(true);
        messageList.add(PRIVATE_REPLY);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }
    
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithInvalidDestAddress2() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        ConversationalMessageService mockedcms = Mockito.mock(ConversationalMessageService.class);
        User mockedUser = Mockito.mock(User.class);
        mockedUser.setLoggedIn(true);
        Message mockedMessage = Mockito.mock(Message.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Field cmService = ClientRunnable.class.getDeclaredField("conversationalMessagesService");
        cmService.setAccessible(true);
        cmService.set(clientRunnableObject, mockedcms);
        Mockito.doNothing().when(mockedUser).userSendMessage(mockedMessage);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON,null);
        Mockito.when(mockedcms.getSender(Mockito.anyString())).thenReturn(SENDER_NAME);
        messageList.clear();
        mockedUser.setLoggedIn(true);
        messageList.add(PRIVATE_REPLY);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }
     
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Private_User message in waitList and Message not sent 
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateWithInvalidDestAddress() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        User u = new User(SENDER_NAME, null, SENDER_NAME,null, true);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(u,null);
        messageList.clear();
        messageList.add(PRIVATE_MESSAGE);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Register Message as the message type
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserSuccessFulRegister() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(null);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        messageList.clear();
        messageList.add(REGISTER2);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Register Message as the message type
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserRegisterPasswordFail() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(null);
        messageList.clear();
        messageList.add(REGISTER);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }
    
    

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete_Group Message as the message type
     * With invalid group name
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithInvalidGroupName() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        messageList.clear();
        messageList.add(DELETE_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete_Group Message as the message type
     * with invalid User
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithInvalidUserName() throws SQLException, NoSuchFieldException, IllegalAccessException {

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
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        messageList.clear();
        messageList.add(DELETE_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete_Group Message as the message type
     * With Valid User and Group and User being Moderator of the group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithValidUserNameAndGroupName() throws SQLException, NoSuchFieldException, IllegalAccessException {

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
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        Group g = Mockito.mock(Group.class);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(g);
        Mockito.when(mockedGroupService.isModerator(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        messageList.clear();
        messageList.add(DELETE_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
 
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Get_Group Message as the message type
     * With Valid Group
     */ 
    @Test
    public void testHandleIncomingMessageWithIteratorWithGetGroupMessageWithValidGroup() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(GET_GROUP);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        Group g = new Group();
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(g);
        messageList.clear();
        messageList.add(GET_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    
    
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete_Group Message as the message type
     * where user is not the moderator of the group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithInvalidModerator() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        Group g = Mockito.mock(Group.class);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(g);
        Mockito.when(mockedGroupService.isModerator(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        messageList.clear();
        messageList.add(DELETE_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
 
    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Get_Group Message as the message type
     * With Invalid Group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithGetGroupMessageWithInvalidGroup() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(GET_GROUP);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        GroupService mockedGroupService = Mockito.mock(GroupService.class);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        messageList.clear();
        messageList.add(GET_GROUP);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
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
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
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
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete User Message with valid user
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteUserMessage() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedUserService.deleteUser(USER_LOGGED_ON)).thenReturn(true);
        messageList.clear();
        messageList.add(DELETE_USER);
        messageIter = messageList.iterator();
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator form network connection
     * which also tests the handleOutgoingMessage() with Delete User Message with invalid user
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteUserMessageInvalid() throws SQLException, NoSuchFieldException, IllegalAccessException {

        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        UserService mockedUserService = Mockito.mock(UserService.class);
        Field userService = ClientRunnable.class.getDeclaredField("userService");
        userService.setAccessible(true);
        userService.set(clientRunnableObject, mockedUserService);
        Mockito.when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Mockito.when(mockedUserService.deleteUser(USER_LOGGED_ON)).thenReturn(false);
        messageList.clear();
        messageList.add(DELETE_USER);
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
    public void testTimerIsBehind() throws IllegalAccessException,SQLException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
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
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        assertFalse(clientRunnableObject.isInitialized());
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
        ml.add(LOGIN);
        ml.add(LOGIN);
        ml.add(quit);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        ClientRunnable senderClient = ClientRunnable.getClientByUsername(SENDER_NAME);
    }

    /**
     * This test verifies if null is returned based on the given username
     */
    @Test
    public void testGetClientByUsernameNonExistingUser() {
        assertNull(ClientRunnable.getClientByUsername("someRandomUsername"));
    }

    /**
     * Test set user name for already existing users. This takes into account
     * a different connection with different message queue but with same sender name
     * and checks whether the duplicate user name is set to invalid-USERNAME-counter format
     */
    @Test
    public void testSetUserNameForAlreadyExistingUsers() {
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
        assertEquals("invalid-" + SENDER_NAME + "-1", clientRunnableObject2.getName());
    }

    /**
     * Test create group.
     *
     * @throws SQLException             the SQL exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Test
    public void testCreateGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<Message> ml = new ArrayList<>();
        ml.add(CREATE_GROUP);
        ml.add(CREATE_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(null);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        privateGroupService.setAccessible(false);

    }

    /**
     * Test cannot create existing group.
     *
     * @throws SQLException             the SQL exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Test
    public void testCannotCreateExistingGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<Message> ml = new ArrayList<>();
        ml.add(CREATE_GROUP);
        ml.add(CREATE_GROUP);
        Group g = new Group();
        GroupService mgs = Mockito.mock(GroupService.class);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(g);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        privateGroupService.setAccessible(false);

    }

    /**
     * Test non existing user cannot create group.
     *
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws SQLException             the SQL exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Test
    public void testNonExistingUserCannotCreateGroup() throws NoSuchFieldException, SecurityException, SQLException, IllegalArgumentException, IllegalAccessException {
        List<Message> ml = new ArrayList<>();
        ml.add(CREATE_GROUP);
        ml.add(CREATE_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(null);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(null);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test add user to group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddUserToGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test remove user from group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testRemoveUserFromGroupWithUserInGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(REMOVE_USER_TO_GROUP);
        ml.add(REMOVE_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(mgs.removeUserFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test add user to group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddUserToGroupWithUserNotGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(mgs.addUserToGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test remove user from group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testRemoveUserFromGroupWithUserNotInGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(REMOVE_USER_TO_GROUP);
        ml.add(REMOVE_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(mgs.removeUserFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false); 
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    
    /**
     * Test remove user from group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testRemoveUserFromGroupWithNoGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(REMOVE_USER_TO_GROUP);
        ml.add(REMOVE_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(Mockito.anyString())).thenReturn(null);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(mgs.removeUserFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false); 
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    
    /**
     * Test remove user from group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testRemoveUserFromGroupByNonModerator() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(REMOVE_USER_TO_GROUP);
        ml.add(REMOVE_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Josh");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }

    /**
     * Test remove non existing user from group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testRemoveNonExisitingUserToGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(REMOVE_USER_TO_GROUP);
        ml.add(REMOVE_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(null);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test add user to group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddUserToGroupByNonModerator() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Josh");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }

    /**
     * Test add non existing user to group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddNonExisitingUserToGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);  
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(null);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test add non existing user to group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddNonExisitingUserInvalidModerator() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(temp);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(null);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test add user to non existing group.
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testAddUserToNonExistingGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        ml.add(ADD_USER_TO_GROUP);
        ml.add(ADD_USER_TO_GROUP);
        GroupService mgs = Mockito.mock(GroupService.class);
        Group temp = new Group();
        temp.setModeratorName("Carol");
        User tempUser = new User("", "", "Carol", "pass", true);
        User tempUser2 = new User("", "", "Bob", "pass", true);
        Mockito.when(mgs.getGroup(DUMMY_GROUP_NAME)).thenReturn(null);
        Field privateGroupService = ClientRunnable.class.
                getDeclaredField("groupService");
        privateGroupService.setAccessible(true);
        Iterator<Message> messageIter = ml.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        privateGroupService.set(clientRunnableObject, mgs);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(tempUser);
        Mockito.when(us.getUserByUserName(DUMMY_USER)).thenReturn(tempUser2);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        privateGroupService.setAccessible(false);
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }
    
    /**
     * Test invalid message handle
     *
     * @throws SQLException the SQL exception
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testInvalidHandle() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	List<Message> ml = new ArrayList<>();
        Message mockMsg = Mockito.mock(Message.class);
        Mockito.when(mockMsg.isBroadcastMessage()).thenReturn(false);
        Mockito.when(mockMsg.isCreateGroupMessage()).thenReturn(false);
        Mockito.when(mockMsg.isDeleteGroupMessage()).thenReturn(false);
        Mockito.when(mockMsg.isLoginMessage()).thenReturn(true,false);
        Mockito.when(mockMsg.isRegisterMessage()).thenReturn(false);
        Mockito.when(mockMsg.isAddUserToGroupMessage()).thenReturn(false);
        Mockito.when(mockMsg.isUserProfileUpdateMessage()).thenReturn(false);
        Mockito.when(mockMsg.getName()).thenReturn(SENDER_NAME);
        ml.add(mockMsg);
        ml.add(mockMsg);
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
     * Verify logout for loggedIn user
     */
    @Test
    public void testTerminateClientLogoutLoggedInUser() throws NoSuchFieldException, IllegalAccessException, SQLException {

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
        UserService mockedService = Mockito.mock(UserService.class);
        Field f = ClientRunnable.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(clientRunnableObject, mockedService);
        User loggedInUser = new User(null, null, SENDER_NAME, null, true);
        Mockito.when(mockedService.getUserByUserName(Mockito.anyString())).thenReturn(loggedInUser);
        Mockito.when(mockedService.updateUser(Mockito.any())).thenReturn(true);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify logout for loggedIn user but DB update unsuccessful.
     */
    @Test
    public void testTerminateClientLogoutLoggedInUserUpdateFailed() throws NoSuchFieldException, IllegalAccessException, SQLException {

        List<Message> messageList = new ArrayList<>();
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        messageList.add(BROADCAST);
        messageList.add(quitMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        
        UserService mockedService = Mockito.mock(UserService.class);
        Field f = ClientRunnable.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(clientRunnableObject, mockedService);
        User loggedInUser = new User(null, null, SENDER_NAME, null, true);
        Mockito.when(mockedService.getUserByUserName(Mockito.anyString())).thenReturn(loggedInUser);
        Mockito.when(mockedService.updateUser(Mockito.any())).thenReturn(false);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify logout for loggedOut user
     */
    @Test
    public void testTerminateClientLogoutInvalidUser() throws NoSuchFieldException, IllegalAccessException, SQLException {

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
        UserService mockedService = Mockito.mock(UserService.class);
        Field f = ClientRunnable.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(clientRunnableObject, mockedService);
        Mockito.when(mockedService.getUserByUserName(Mockito.anyString())).thenReturn(null);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends broadcast message.
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsBroadcastMessage() throws
            NoSuchFieldException,IllegalAccessException,SQLException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is logged in.
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsLoggedIn() throws
            NoSuchFieldException,IllegalAccessException,SQLException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends login message.
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsLOGINMessage() throws
            NoSuchFieldException,IllegalAccessException,SQLException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(LOGIN);
        messageList.add(LOGIN);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends register message.
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsREGISTERMessage() throws
            NoSuchFieldException,IllegalAccessException,SQLException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(REGISTER);
        messageList.add(REGISTER);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }


    /**
     * Test handle user profile update message when there is a successful update.
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleUserProfileUpdateMessage() throws NoSuchFieldException,IllegalAccessException,SQLException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(REGISTER);
        messageList.add(USER_PROFILE_UPDATE);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        UserService us = Mockito.mock(UserService.class);
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(us.updateUserAttributes(SENDER_NAME,"Alex","Predna")).thenReturn(true);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        clientRunnableObject.run();
        clientRunnableObject.run();
        Assertions.assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for false.
     *
     * @throws SQLException           the sql exception
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @Test
    public void testHandleUserProfileUpdateMessageForFalse() throws
            SQLException,IllegalAccessException,NoSuchFieldException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(REGISTER);
        messageList.add(USER_PROFILE_UPDATE);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        UserService us = Mockito.mock(UserService.class);
        Mockito.when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.when(us.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(us.updateUserAttributes(SENDER_NAME,"Alex","Predna")).thenReturn(false);
        clientRunnableObject.run();
        clientRunnableObject.run();
        Assertions.assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test handle message with null output for msg.getName() (Invalid sender specified).
     *
     * @throws SQLException           the sql exception
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @Test
    public void testNullSenderInMessage() throws SQLException,IllegalAccessException,NoSuchFieldException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(PRIVATE_MESSAGE);
        messageList.add(NULL_PRIVATE_MESSAGE);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        Mockito.when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        UserService us = Mockito.mock(UserService.class);
        USER_LOGGED_ON.setLoggedIn(true);
        Mockito.when(us.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        Mockito.when(us.updateUserAttributes(SENDER_NAME,"Alex","Predna")).thenReturn(false);
        clientRunnableObject.run();
        clientRunnableObject.run();
        Assertions.assertTrue(clientRunnableObject.isInitialized());
        USER_LOGGED_ON.setLoggedIn(true);
    }

    //Private fields to be used in tests
    private static final Message LOGIN = Message.makeLoginMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.PASS);
    private static final Message REGISTER = Message.makeRegisterMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.PASS, TestClientRunnable.PASS);
    private static final Message REGISTER2 = Message.makeRegisterMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.PASS, "");
    private static final Message BROADCAST = Message.makeBroadcastMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.MESSAGE_TEXT);
    private static final Message DELETE_GROUP = Message.makeDeleteGroupMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.GROUP_NAME);
    private static final Message PRIVATE_MESSAGE = Message.makePrivateUserMessage(TestClientRunnable.SENDER_NAME, "hello", "rb");
    private static final Message NULL_PRIVATE_MESSAGE = Message.makePrivateUserMessage(null, "hello", "rb");
    private static final Message DELETE_USER = Message.makeDeleteUserMessage(TestClientRunnable.SENDER_NAME);
    private static final Message GET_GROUP = Message.makeGetGroupMessage(TestClientRunnable.SENDER_NAME, TestClientRunnable.GROUP_NAME);
    private static final Message USER_PROFILE_UPDATE = Message.makeUserProfileUpdateMessage(TestClientRunnable.SENDER_NAME,"Alex","Predna");
    private static final Message PRIVATE_REPLY = Message.makePrivateReplyMessage(TestClientRunnable.SENDER_NAME,"Alex","MESSAGEKEY");    
    private static final String DUMMY_GROUP_NAME = "dummy";
    private static final Message CREATE_GROUP = Message.makeCreateGroupMessage(TestClientRunnable.SENDER_NAME, DUMMY_GROUP_NAME);
    private static final String DUMMY_USER = "Bob";
    private static final Message ADD_USER_TO_GROUP = Message.makeAddUserToGroupMessage(TestClientRunnable.SENDER_NAME, DUMMY_USER, DUMMY_GROUP_NAME);
    private static final Message REMOVE_USER_TO_GROUP = Message.makeRemoveUserFromGroupMessage(TestClientRunnable.SENDER_NAME, DUMMY_USER, DUMMY_GROUP_NAME);
    private static final int USER_ID = 120000;
    private static final String GROUP_NAME = "FAMILY";
    private static final String SENDER_NAME = "Alice";
    private static final String MESSAGE_TEXT = "Hello, I am Alice";
    private static final String PASS = "some_p@$$worD";
    private static final User USER_LOGGED_ON = new User(null,null,SENDER_NAME,"QWERTY",true);
    private static final User USER_LOGGED_OFF = new User(null,null,SENDER_NAME,"QWERTY",false);

}

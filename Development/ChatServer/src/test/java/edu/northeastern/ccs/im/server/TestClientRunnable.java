package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.NetworkConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.ConversationalMessageService;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestClientRunnable class contains all the unit tests for the java class ClientRunnable
 * It has various test for each of the method and also for testing various branch conditions
 * using desired scenarios
 *
 * @author Kunal
 * @version 2.0
 */
public class TestClientRunnable {


    /**
     * Initialize all private fields objects here to be used in mocks and other set up
     */
    private ClientRunnable clientRunnableObject;
    private List<Message> messageList;
    private Iterator<Message> messageIter;

    /**
     * Mock for the network connection class object to be used in ClientRunnable class
     */
    @Mock
    NetworkConnection networkConnectionMock;

    /**
     * Mock for the user service class object to be used in ClientRunnable class
     */
    @Mock
    UserService mockedUserService;

    /**
     * Mock for the conversational message service class object to be used in ClientRunnable class
     */
    @Mock
    ConversationalMessageService mockedcms;

    /**
     * Mock for the user class object to be used in ClientRunnable class
     */
    @Mock
    User mockedUser;

    /**
     * Mock for the group service class object to be used in ClientRunnable class
     */
    @Mock
    GroupService mockedGroupService;

    /**
     * Mock for the invitation service class object to be used in ClientRunnable class
     */
    @Mock
    InvitationService mockedInvitationService;

    /**
     * Mock for the group class object to be used in ClientRunnable class
     */
    @Mock
    Group mockedGroup;

    /**
     * Helper method to reset and set the messageList to be used during tests
     * @param messageList - the message list to be passed that contains all the messages
     * @param msg         - the message object that needs to be added to the above mentioned list
     * @return message iterator - an iterator over the message list that will be used by the ClientRunnable mocked object
     */
    private Iterator<Message> resetAndAddMessages(List<Message> messageList, Message... msg){
        messageList.clear();
        for(Message m : msg)
            messageList.add(m);
        Iterator<Message> messageIter2 = messageList.iterator();
        return  messageIter2;
    }

    /**
     * Sets up all behaviors of different mocks and uses reflection to set them to the clientRunnable class fields
     *
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws SQLException           the sql exception that is thrown when a query doesn't work correctly
     */
    @BeforeEach
    public void setUp() throws IllegalAccessException,NoSuchFieldException,SQLException{
        MockitoAnnotations.initMocks(this);
        //Define behavior for networkConnectionMock
        clientRunnableObject = new ClientRunnable(networkConnectionMock);
        messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);

        //Define behavior for mocked User Service
        when(mockedUserService.getUserByUserNameAndPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.updateUserAttributes(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.deleteUser(Mockito.any())).thenReturn(true);
        when(mockedUserService.updateUserAttributes(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);

        //Define behavior for mocked Conversational Message Service
        when(mockedcms.getSender(Mockito.anyString())).thenReturn(SENDER_NAME);
        when(mockedcms.deleteGroupMessage(Mockito.anyString())).thenReturn(true);
        when(mockedcms.deleteMessage(Mockito.anyString())).thenReturn(true);

        //Define behavior for User:
        mockedUser.setLoggedIn(true);
        when(mockedUser.userSendMessage(Mockito.any(Message.class))).thenReturn(DUMMY_MSG_UNIQUE_KEY);

        //Define behavior for Group Service:
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        when(mockedGroupService.isModerator(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(mockedGroupService.createGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        when(mockedGroupService.removeUserFromGroup(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(mockedGroupService.addUserToGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        when(mockedGroupService.isUserMemberOfTheGroup(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(mockedGroupService.updateGroupSettings(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);

        //Set fields in clientRunnable
        Field ncField = ClientRunnable.class.getDeclaredField("connection");
        ncField.setAccessible(true);
        ncField.set(clientRunnableObject,networkConnectionMock);
        Field userServiceField = ClientRunnable.class.getDeclaredField("userService");
        userServiceField.setAccessible(true);
        userServiceField.set(clientRunnableObject, mockedUserService);
        Field cmService = ClientRunnable.class.getDeclaredField("conversationalMessagesService");
        cmService.setAccessible(true);
        cmService.set(clientRunnableObject, mockedcms);
        Field groupService = ClientRunnable.class.getDeclaredField("groupService");
        groupService.setAccessible(true);
        groupService.set(clientRunnableObject, mockedGroupService);

        //Set fields for userClients and invalidCounter
        Field hm = ClientRunnable.class.getDeclaredField("userClients");
        hm.setAccessible(true);
        Map<String, ClientRunnable> userClientsEmpty = new HashMap<>();
        userClientsEmpty.clear();
        hm.set(null, userClientsEmpty);
        Field ic = ClientRunnable.class.getDeclaredField("invalidCounter");
        ic.setAccessible(true);
        ic.set(null, 0);

        Field invitationService = ClientRunnable.class.getDeclaredField("invitationService");
        invitationService.setAccessible(true);
        invitationService.set(clientRunnableObject, mockedInvitationService);
    }


    /**
     * Test to check weather run() changes the status of the initialized data member
     * to true in the first run with using a message iterator with one message
     */
    @Test
    public void testRun(){
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());

    }

    /**
     * Test to check weather run() changes the status of the initialized data member
     * to true in the first run with using a message iterator with one message
     */
    @Test
    public void testRunSameClient(){
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
    public void testSetNameandGetName() throws IOException{
        SocketChannel client = SocketChannel.open();
        NetworkConnection networkConnection = new NetworkConnection(client);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnection);
        clientRunnableObject.setName(SENDER_NAME);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }

    /**
     * Testing handleIncomingMessage() and HandleOutGoingMessage() method with
     * true Condition of initialized data member by running run() method twice
     */
    @Test
    public void testHandleIncomingMessageandHandleOutGoingMessage() {
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,BROADCAST,BROADCAST));
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Testing handleIncomingMessage() where the Message iterator does
     * not have broadcast message
     */
    @Test
    public void testHandleIncomingMessageWithoutBroadcastMessage(){
        Message login = Message.makeSimpleLoginMessage(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,BROADCAST,login));
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }



    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Empty waitList
     */
    @Test
    public void testHandleIncomingMessageWithEmptyIterator() {
        clientRunnableObject.run();
        messageList.clear();
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Login waitList
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForInvalidUser() throws SQLException {
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserNameAndPassword(Mockito.anyString(),Mockito.anyString())).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,LOGIN));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Login in waitList and login successful
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForValidUserSuccessfulLogin(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,LOGIN));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Login in waitList and login successful
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserUnsuccessfulRegister(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REGISTER));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Private User message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateWithValidDestAddress(){
        clientRunnableObject.run();
        User mockedUser = mock(User.class);
        mockedUser.setLoggedIn(true);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithValidDestAddress() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON, mockedUser);
        when(mockedcms.getSender(Mockito.anyString())).thenReturn(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_REPLY));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithInvalidDestAddress() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON, USER_LOGGED_ON);
        when(mockedcms.getSender(Mockito.anyString())).thenReturn(null);
        mockedUser.setLoggedIn(true);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_REPLY));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with PrivateReply message in waitList and Message sent successfully
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateReplyWithInvalidDestAddress2() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON, (User) null);
        mockedUser.setLoggedIn(true);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_REPLY));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Private User message in waitList and Message not sent
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithPrivateWithInvalidDestAddress() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON,(User) null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_MESSAGE));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Register Message as the message type
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserSuccessFulRegister()
            throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REGISTER));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Register Message as the message type
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithRegisterMessageForValidUserRegisterPasswordFail()
            throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REGISTER2));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Delete Group Message as the message type
     * With invalid group name
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithInvalidGroupName() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Delete Group Message as the message type
     * where user is not the moderator of the group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithInvalidModerator() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        when(mockedGroupService.isModerator(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Delete Group Message as the message type
     * With Valid User and Group and User being Moderator of the group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteGroupMessageWithValidUserNameAndGroupName(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Get Group Message as the message type
     * With Valid Group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithGetGroupMessageWithValidGroup(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_GROUP));
        when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Get Group Message as the message type
     * With Invalid Group
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithGetGroupMessageWithInvalidGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Login in waitList and login unsuccessful - this test
     * is for the time when the user's attributes in the database don't get updated after logging in.
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithLoginMessageForValidUserUnsuccessfulLogin() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.updateUserAttributes(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
                .thenReturn(false);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,LOGIN));
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Delete User Message with invalid user
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteUserMessageInvalid() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.deleteUser(USER_LOGGED_ON)).thenReturn(false);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_USER));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Testing checkForIntialization() Method using with Empty
     * message Iterator from network connection
     */
    @Test
    public void testCheckInitialization(){
        clientRunnableObject.run();
        messageList.clear();
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        assertFalse(clientRunnableObject.isInitialized());
    }

    /**
     * Testing setUserName() using Null as input
     *
     * @throws IllegalAccessException    the illegal access exception to be used while using java Reflection
     * @throws InvocationTargetException the the underlying reflection method call throws an exception .
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     */
    @Test
    public void testSetUserName() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        List<Message> messageList = new ArrayList<>();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        messageList.add(helloMessage);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        clientRunnableObject.run();
        assertFalse(clientRunnableObject.isInitialized());
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
        when(networkConnection.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnection);
        clientRunnableObject.enqueueMessage(helloMessage);
        assertFalse(clientRunnableObject.isInitialized());
    }


    /**
     * Testing setUserName using a valid string Input Which sets the user name
     */
    @Test
    public void testSetUserNameWithValidString(){
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
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        ClientRunnable senderClient = ClientRunnable.getClientByUsername(SENDER_NAME);
        assertEquals(clientRunnableObject.getName(),senderClient.getName());
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
    public void testSetUserNameForAlreadyExistingUsers(){
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
        List<Message> ml2 = new ArrayList<>();
        ml2.add(BROADCAST);
        Iterator<Message> messageIter2 = ml2.iterator();
        NetworkConnection networkConnectionMock2 = Mockito.mock(NetworkConnection.class);
        when(networkConnectionMock2.iterator()).thenReturn(messageIter2);
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
     */
    @Test
    public void testCreateGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(DUMMY_GROUP_NAME)).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,CREATE_GROUP));
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }

    /**
     * Test cannot create existing group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testCannotCreateExistingGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(DUMMY_GROUP_NAME)).thenReturn(mockedGroup);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,CREATE_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add user to group when user is already a part of the group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddUserToGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedGroupService.addUserToGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test remove user from group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupWithUserInGroup() throws SQLException{
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_USER_TO_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add user to group when user is not a part of the group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddUserToGroupWithUserNotGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test remove user from group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupWithUserNotInGroup() throws SQLException{
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_USER_TO_GROUP));
        when(mockedGroupService.removeUserFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test remove user from group when the group is not present.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupWithNoGroup() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_USER_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test remove user from group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testRemoveUserFromGroupByNonModerator() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(DUMMY_USER);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_USER_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test remove non existing user from group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testRemoveNonExisitingUserToGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.getUserByUserName(DUMMY_USER)).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_USER_TO_GROUP));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add user to group by non moderator
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddUserToGroupByNonModerator() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(DUMMY_USER);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add non existing user to group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddNonExisitingUserToGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.getUserByUserName(DUMMY_USER)).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP2));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add non existing user to group and when the moderator is invalid
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddNonExisitingUserInvalidModerator() throws SQLException{
        USER_LOGGED_OFF.setLoggedIn(true);
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        when(mockedUserService.getUserByUserName(DUMMY_USER)).thenReturn(null);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP2));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
        USER_LOGGED_OFF.setLoggedIn(false);
    }

    /**
     * Test add user to non existing group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testAddUserToNonExistingGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_ON);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_USER_TO_GROUP2));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test invalid message handle
     *
     */
    @Test
    public void testInvalidHandle(){
        clientRunnableObject.run();
        Message mockMsg = Mockito.mock(Message.class);
        when(mockMsg.getName()).thenReturn(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,mockMsg));
        clientRunnableObject.run();
        assertNotEquals(clientRunnableObject.getUserId(), -1);
        assertEquals(clientRunnableObject.getName(), SENDER_NAME);
    }

    /**
     * Verify logout for loggedIn user
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testTerminateClientLogoutLoggedInUser() throws SQLException{
        messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(QUIT_MESSAGE);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.updateUserAttributes(USER_LOGGED_ON.getUserName(),
                "logged_in", "0")).thenReturn(true);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify follow user is handled properly for valid followee
     */
    @Test
    public void testUserFollowMessageValid(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,FOLLOW_USER_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify unfollow user is handled properly for valid followee
     */
    @Test
    public void testUserUnfollowMessageValid(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,UNFOLLOW_USER_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify follow user is handled properly for invalid followee
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUserFollowMessageInvalid() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,FOLLOW_USER_MESSAGE));
        when(mockedUserService.getUserByUserName(Mockito.any())).thenReturn(USER_LOGGED_ON, (User) null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Verify unfollow user is handled properly for invalid followee
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testUserUnfollowMessageInvalid() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,UNFOLLOW_USER_MESSAGE));
        when(mockedUserService.getUserByUserName(Mockito.any())).thenReturn(USER_LOGGED_ON, null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends broadcast message.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsBroadcastMessage() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,BROADCAST));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }


    /**
     * Test handle incoming message when user is logged in.
     *
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsLoggedIn(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,BROADCAST));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends login message.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsLOGINMessage() throws SQLException{
        clientRunnableObject.run();
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,LOGIN,LOGIN));
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle incoming message when user is not logged in and user sends register message.
     *
     */
    @Test
    public void testHandleIncomingMessageWhenUserIsNotLoggedInAndUserSendsREGISTERMessage() throws SQLException{
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(USER_LOGGED_OFF);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REGISTER,REGISTER));
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for first name.
     *
     */
    @Test
    public void testHandleUserProfileUpdateMessageForFirstName(){
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "1", "Predna");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for last name.
     *
     */
    @Test
    public void testHandleUserProfileUpdateMessageForLastName(){
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "2", "Predna");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for user password.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleUserProfileUpdateMessageForUserPassword(){
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "3", "Predna");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for user searchability attribute.
     *
     */
    @Test
    public void testHandleUserProfileUpdateMessageForUserSearchability(){
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "4", "True");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for an incorrect number. Should return false
     *
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleUserProfileUpdateMessageForIncorrectNumber() throws SQLException{
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "5", "Random");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle user profile update message for last name when database operations don't get executed
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testHandleUserProfileUpdateMessageForLastNameFalse() throws SQLException{
        clientRunnableObject.run();
        Message someMessage = Message.makeUserProfileUpdateMessage(SENDER_NAME, "2", "Predna");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,someMessage));
        when(mockedUserService.updateUserAttributes(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test handle message with null output for msg.getName() (Invalid sender specified).
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testNullSenderInMessage(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,NULL_PRIVATE_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test group message when group does not exist.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testGroupMessageWhenGroupDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GROUP_MESSAGE));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test update group message when group is not present.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsNotPresent() throws SQLException{
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "NoSuchGroup", "1:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test group message when user not A member of group.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testGroupMessageWhenUserNotAMemberOfGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GROUP_MESSAGE));
        when(mockedGroupService.isUserMemberOfTheGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test group message.
     *
     * @throws SQLException             the SQL exception
     */
    @Test
    public void testGroupMessage(){
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GROUP_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test Get Followers
     *
     */
    @Test
    public void testGetFollowers(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWERS));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test Get Online Users
     *
     */
    @Test
    public void testGetOnlineUsers(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_ONLINE_USER));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test Get Followees .
     *
     */
    @Test
    public void testGetFollowees(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWEES));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test update group message when group is present but user is not moderator.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsPresentButUserIsNotModerator() throws SQLException{
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "PresentGroup", "1:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        when(mockedGroupService.isModerator(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    /**
     * Test update group message when group is present but user is moderator for true.
     *
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsPresentButUserIsModeratorForTrue(){
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "PresentGroup", "1:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test update group message when group is present but user is moderator for false.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsPresentButUserIsModeratorForFalse() throws SQLException{
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "PresentGroup", "1:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        when(mockedGroupService.updateGroupSettings(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
                .thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test update group message when group is present but user is moderator for exception.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsPresentButUserIsModeratorForException() throws SQLException{
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "PresentGroup", "1:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        when(mockedGroupService.updateGroupSettings(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).
                thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test update group message when group is present but user is moderator when attribute number is out of bounds.
     *
     */
    @Test
    public void testUpdateGroupMessageWhenGroupIsPresentButUserIsModeratorWhenAttributeNumberIsOutOfBounds(){
        clientRunnableObject.run();
        Message updateGroupMessage = Message.makeUpdateGroupMessage(SENDER_NAME, "PresentGroup", "2:0");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,updateGroupMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message for user.
     *
     */
    @Test
    public void testSearchMessageForUser() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_USER_MESSAGE));
        HashMap<String, String> testSet = new HashMap<>();
        testSet.put("kp", "some name");
        testSet.put("kp2", "some otherName");
        when(mockedUserService.searchUser("kp")).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message for user when no such user is found.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageForUserWhenNoSuchUserIsFound() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_USER_MESSAGE));
        HashMap<String, String> testSet = new HashMap<>();
        when(mockedUserService.searchUser("kp")).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message when sql exception is thrown due to some error.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageWhenSQLExceptionIsThrownDueToSomeError() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_USER_MESSAGE));
        when(mockedUserService.searchUser("kp")).thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message for neither group or user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageForNeitherGroupOrUser() throws SQLException{
        clientRunnableObject.run();
        Message searchMessage = Message.makeSearchMessage(SENDER_NAME, "WrongInput", "kp");
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,searchMessage));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message for group.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageForGroup() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_GROUP_MESSAGE));
        HashMap<String, String> testSet = new HashMap<>();
        testSet.put("kkg", "kk");
        testSet.put("kkGroup", "kk");
        when(mockedGroupService.searchGroup(Mockito.anyString())).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test get follower message for user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetFollowerOneMessage() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWERS));
        HashMap<String, String> testSet = new HashMap<>();
        testSet.put("kkg", "kk");
        testSet.put("kkGroup", "kk");
        when(mockedUserService.getFollowers(Mockito.any())).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test get online user message.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetTwoOnlineUserMessage() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_ONLINE_USER));
        HashMap<String, String> testSet = new HashMap<>();
        testSet.put("kkg", "kk");
        testSet.put("kkGroup", "kk");
        when(mockedUserService.getOnlineUsers(Mockito.any())).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test get follower message for user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetFollowerMessageForException() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWERS));
        when(mockedUserService.getFollowers(Mockito.any())).thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test get online user for user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetOnlineUserForException() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_ONLINE_USER));
        when(mockedUserService.getOnlineUsers(Mockito.any())).thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test get follower message for user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetFolloweeOneMessage() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWEES));
        HashMap<String, String> testSet = new HashMap<>();
        testSet.put("kkg", "kk");
        testSet.put("kkGroup", "kk");
        when(mockedUserService.getFollowees(Mockito.any())).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test get follower message for user.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testGetFolloweeMessageForException() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,GET_FOLLOWEES));
        when(mockedUserService.getFollowees(Mockito.any())).thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test search message for group when no such group is found.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageForGroupWhenNoSuchGroupIsFound() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_GROUP_MESSAGE));
        HashMap<String, String> testSet = new HashMap<>();
        when(mockedGroupService.searchGroup(Mockito.anyString())).thenReturn(testSet);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test search message when sql exception is thrown due to some error for groups.
     *
     * @throws SQLException           the sql exception
     */
    @Test
    public void testSearchMessageWhenSQLExceptionIsThrownDueToSomeErrorForGroups() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,SEARCH_GROUP_MESSAGE));
        when(mockedGroupService.searchGroup(Mockito.anyString())).thenThrow(SQLException.class);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete group message message when sender is originator.
     *
     */
    @Test
    public void testDeleteGroupMessageMessageWhenSenderIsOriginator(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUPMESSAGE_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete group message message when sender is originator error deleting.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testDeleteGroupMessageMessageWhenSenderIsOriginatorErrorDeleting() throws SQLException{
        clientRunnableObject.run();
        when(mockedcms.deleteGroupMessage(Mockito.anyString())).thenReturn(false);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUPMESSAGE_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete group message message when sender is not the originator.
     *
     */
    @Test
    public void testDeleteGroupMessageMessageWhenSenderIsNotTheOriginator(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_GROUPMSG_MSG_SENDER_NOT_IN_KEY));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete private message message when sender is originator.
     *
     */
    @Test
    public void testDeletePrivateMessageMessageWhenSenderIsOriginator(){
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_PRIVATEMESSAGE_MESSAGE));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete private message message when sender is originator and error deleting.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testDeletePrivateMessageMessageWhenSenderIsOriginatorAndErrorDeleting() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_PRIVATEMESSAGE_MESSAGE));
        when(mockedcms.deleteMessage(Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test delete private message message when sender is not the originator.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testDeletePrivateMessageMessageWhenSenderIsNotTheOriginator() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,DELETE_PRIVATEMESSAGE_MESSAGE));
        when(mockedcms.getSender(Mockito.anyString())).thenReturn(DUMMY_USER);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * This test verifies create Invitation handle when Invitee does not exist
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageNullInvitee() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when the group name provided is invalid.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageGroupIsInvalid() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when The Inviter is not part of the group
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageInviterNotPartOfGroup() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>());
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when Invitee is already added to the group
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageInviteeAlreadyPartOfGroup() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(INVITEE_USER)));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when an invitation is already sent to the user
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageInvitationAlreadySent() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(CREATE_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when create invitation successfully updates the database
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageUpdateSuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(null);
        when(mockedInvitationService.createInvitation(SENDER_NAME, INVITEE, GROUP_NAME)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies create Invitation handle when create Invitation fails to update the database.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageCreateInvitationMessageUpdateUnsuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, CREATE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(null);
        when(mockedInvitationService.createInvitation(SENDER_NAME, INVITEE, GROUP_NAME)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies delete Invitation handle when the invitation does not exist.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDeleteInvitationMessageInvitationDoesNotExist() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DELETE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies delete Invitation handle when the invitation is already deleted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDeleteInvitationMessageInvitationAlreadyDeleted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DELETE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        DELETE_INVITATION_MESSAGE.setInvitationDeleted(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(DELETE_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        DELETE_INVITATION_MESSAGE.setInvitationDeleted(false);
    }

    /**
     * This test verifies delete Invitation handle when the invitation is already deleted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDeleteInvitationInviterNotTheInvitationSender() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DELETE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(DELETE_INVITATION_MESSAGE_INVITER);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * This test verifies delete Invitation handle when the invitation is deleted successfully.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDeleteInvitationMessageInvitationDeletedSuccessfully() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DELETE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(DELETE_INVITATION_MESSAGE);
        when(mockedInvitationService.deleteInvitation(SENDER_NAME, INVITEE, GROUP_NAME)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies delete Invitation handle when the invitation cannot be deleted due to some error.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDeleteInvitationMessageInvitationUnableToDelete() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DELETE_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(USER_LOGGED_ON)));
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(DELETE_INVITATION_MESSAGE);
        when(mockedInvitationService.deleteInvitation(SENDER_NAME, INVITEE, GROUP_NAME)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies accept Invitation handle when invitation is not found.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageDoesNotExist() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies accept Invitation handle when invitation is already accepted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAlreadyAccepted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationAccepted(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationAccepted(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is already denied.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAlreadyDenied() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationDenied(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationDenied(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is already rejected.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAlreadyRejected() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationRejected(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationRejected(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is already deleted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAlreadyDeleted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationDeleted(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationDeleted(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is accepted successful
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAcceptSuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, true)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies accept Invitation handle when invitation is accepted successful and already approved
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAcceptSuccessfulAndApproved() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationApproved(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, true)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationApproved(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is accepted successful
     * and already approved and addition is successful
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAcceptSuccessfulAndApprovedAndAdditionSuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        ACCEPT_INVITATION_MESSAGE.setInvitationApproved(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, true)).thenReturn(true);
        when(mockedGroupService.addUserToGroup(GROUP_NAME, INVITEE)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        ACCEPT_INVITATION_MESSAGE.setInvitationApproved(false);
    }

    /**
     * This test verifies accept Invitation handle when invitation is accepted unsuccessful
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageAcceptInvitationMessageAcceptUnsuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, ACCEPT_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(ACCEPT_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, true)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies deny Invitation handle when invitation is not found.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageDoesNotExist() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies deny Invitation handle when invitation is already accepted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageAlreadyAccepted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        DENY_INVITATION_MESSAGE.setInvitationAccepted(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        DENY_INVITATION_MESSAGE.setInvitationAccepted(false);
    }

    /**
     * This test verifies deny Invitation handle when invitation is already denied.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageAlreadyDenied() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        DENY_INVITATION_MESSAGE.setInvitationDenied(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        DENY_INVITATION_MESSAGE.setInvitationDenied(false);
    }

    /**
     * This test verifies deny Invitation handle when invitation is already rejected.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageAlreadyRejected() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        DENY_INVITATION_MESSAGE.setInvitationRejected(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        DENY_INVITATION_MESSAGE.setInvitationRejected(false);
    }

    /**
     * This test verifies deny Invitation handle when invitation is already deleted.
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageAlreadyDeleted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        DENY_INVITATION_MESSAGE.setInvitationDeleted(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        DENY_INVITATION_MESSAGE.setInvitationDeleted(false);
    }

    /**
     * This test verifies deny Invitation handle when invitation is denied successful
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageDenySuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, false)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies deny Invitation handle when invitation is not denied
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageDenyInvitationMessageDenyunsuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, DENY_INVITATION_MESSAGE));
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(DENY_INVITATION_MESSAGE);
        when(mockedInvitationService.acceptDenyInvitation(SENDER_NAME, GROUP_NAME, false)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when group name is invalid
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageGroupInvalid() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when sender is not the moderator of the group
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageSenderNotModerator() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when invitee is already a member
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageInviteeAlreadyMember() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        when(mockedGroupService.getMemberUsers(GROUP_NAME)).thenReturn(new HashSet<>(Arrays.asList(INVITEE_USER)));
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when invitation does not exist
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageInvitationNotFound() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        when(mockedInvitationService.getInvitation(SENDER_NAME, GROUP_NAME)).thenReturn(null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when invitation is already rejected
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageInvitationAlreadyRejected() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        APPROVE_INVITATION_MESSAGE.setInvitationRejected(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(APPROVE_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        APPROVE_INVITATION_MESSAGE.setInvitationRejected(false);
    }

    /**
     * This test verifies approve Invitation handle when invitation is already deleted
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageInvitationAlreadyDeleted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        APPROVE_INVITATION_MESSAGE.setInvitationDeleted(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(APPROVE_INVITATION_MESSAGE);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        APPROVE_INVITATION_MESSAGE.setInvitationDeleted(false);
    }

    /**
     * This test verifies approve Invitation handle when invitation is approved successfully
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageApproveSuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(APPROVE_INVITATION_MESSAGE);
        when(mockedInvitationService.approveRejectInvitation(INVITEE, GROUP_NAME, true)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when invitation is approved unsuccessfully
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageApproveUnsuccessful() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(APPROVE_INVITATION_MESSAGE);
        when(mockedInvitationService.approveRejectInvitation(INVITEE, GROUP_NAME, true)).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
    }

    /**
     * This test verifies approve Invitation handle when invitation is approved and accepted
     *
     * @throws SQLException - thrown when a downstream database calls fails.
     */
    @Test
    public void testHandleIncomingMessageApproveInvitationMessageApproveSuccessfulAndAccepted() throws SQLException {
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList, APPROVE_INVITATION_MESSAGE));
        when(mockedGroupService.getGroup(GROUP_NAME)).thenReturn(GROUP);
        when(mockedUserService.getUserByUserName(INVITEE)).thenReturn(INVITEE_USER);
        when(mockedGroupService.isModerator(GROUP_NAME, SENDER_NAME)).thenReturn(true);
        APPROVE_INVITATION_MESSAGE.setInvitationAccepted(true);
        when(mockedInvitationService.getInvitation(INVITEE, GROUP_NAME)).thenReturn(APPROVE_INVITATION_MESSAGE);
        when(mockedInvitationService.approveRejectInvitation(INVITEE, GROUP_NAME, true)).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized ());
        APPROVE_INVITATION_MESSAGE.setInvitationAccepted(false);
    }

    /**
     * Test for user does not exist, that is, user is null and msg is not a login message neither a register message
     */
    @Test
    public void testForUserDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,PRIVATE_MESSAGE));
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(null);
        clientRunnableObject.run();
    }

    /**
     * Test for handle incoming messages when user is null and reg and login.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testForHandleIncomingMessagesWhenUserIsNullAndRegAndLogin() throws SQLException{
        clientRunnableObject.run();
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,LOGIN,REGISTER));
        when(mockedUserService.getUserByUserName(SENDER_NAME)).thenReturn(null);
        clientRunnableObject.run();
        clientRunnableObject.run();
    }

    /**
     * Test handleIncomingMessage() empty message Iterator from network connection
     * which also tests the handleOutgoingMessage() with Delete User Message with valid user
     */
    @Test
    public void testHandleIncomingMessageWithIteratorWithDeleteUserMessage() throws SQLException,
            IllegalAccessException,NoSuchFieldException{
        List<Message> messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        Iterator<Message> messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
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
        when(mockedUserService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        when(mockedUserService.deleteUser(USER_LOGGED_ON)).thenReturn(true);
        messageList.clear();
        messageList.add(DELETE_USER);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        clientRunnableObject.run();
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
    public void testHandleIncomingMessageAndEnqueueMessage() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        Message loginMessage = Message.makeSimpleLoginMessage(SENDER_NAME);
        messageList.add(loginMessage);
        messageList.add(helloMessage);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
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
    public void testHandleIncomingMessageTerminateCondition(){
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        messageList.add(BROADCAST);
        messageList.add(quitMessage);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
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
        Message login = Message.makeSimpleLoginMessage(SENDER_NAME);
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        messageList.add(login);
        messageList.add(helloMessage);
        messageList.add(BROADCAST);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
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
     * Testing isBehind() condition in run() by setting it to True
     *
     * @throws InvocationTargetException the underlying reflection method call throws an exception
     * @throws NoSuchMethodException     the no such method exception to be used while using java Reflection
     * @throws NoSuchFieldException      the no such field exception to be used while using java Reflection
     */
    @Test
    public void testTimerIsBehind() throws IllegalAccessException, SQLException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        Method retrieveItems = Message.class.getDeclaredMethod("makeHelloMessage", String.class);
        retrieveItems.setAccessible(true);
        Message.class.getDeclaredMethods();
        Message helloMessage = (Message) retrieveItems.invoke(Message.class, MESSAGE_TEXT);
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        messageList.clear();
        messageList.add(helloMessage);
        messageList.add(quitMessage);
        messageIter = messageList.iterator();
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientTimer ct = Mockito.mock(ClientTimer.class);
        Field privateStringField = ClientRunnable.class.getDeclaredField("timer");
        privateStringField.setAccessible(true);
        privateStringField.set(clientRunnableObject, ct);
        when(ct.isBehind()).thenReturn(true);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        UserService us = Mockito.mock(UserService.class);
        when(us.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        Field privateUserService = ClientRunnable.class.
                getDeclaredField("userService");
        privateUserService.setAccessible(true);
        privateUserService.set(clientRunnableObject, us);
        clientRunnableObject.setFuture(future);
        clientRunnableObject.run();
        assertFalse(clientRunnableObject.isInitialized());
    }


    /**
     * Verify logout for loggedIn user but DB update unsuccessful.
     */
    @Test
    public void testTerminateClientLogoutLoggedInUserUpdateFailed() throws NoSuchFieldException, IllegalAccessException, SQLException {

        messageList = new ArrayList<>();
        messageList.add(BROADCAST);
        messageList.add(QUIT_MESSAGE);
        messageIter = messageList.iterator();
        NetworkConnection networkConnectionMock = Mockito.mock(NetworkConnection.class);
        when(networkConnectionMock.iterator()).thenReturn(messageIter);
        ClientRunnable clientRunnableObject = new ClientRunnable(networkConnectionMock);
        when(networkConnectionMock.sendMessage(Mockito.any())).thenReturn(true);
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
        ScheduledFuture<?> future = threadpool.scheduleAtFixedRate(clientRunnableObject, ServerConstants.CLIENT_CHECK_DELAY,
                ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
        clientRunnableObject.setFuture(future);
        UserService mockedService = Mockito.mock(UserService.class);
        Field f = ClientRunnable.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(clientRunnableObject, mockedService);
        when(mockedService.getUserByUserName(Mockito.anyString())).thenReturn(USER_LOGGED_ON);
        when(mockedService.updateUserAttributes(USER_LOGGED_ON.getUserName(), "logged_in", "0")).thenReturn(false);
        clientRunnableObject.run();
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }

    /**
     * Test add group to group.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_GROUP_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedGroupService.addGroupToGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test add group to group could not add.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupCouldNotAdd() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_GROUP_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedGroupService.addGroupToGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test add group to group when host group does not exist.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWhenHostGroupDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_GROUP_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp, (Group) null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test add group to group when sender not the moderator of the host group.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWhenSenderNotTheModeratorOfTheHostGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroup.getModeratorName()).thenReturn(ANOTHER_USER);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_GROUP_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test add group to group when guest group does not exist.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testAddGroupToGroupWhenGuestGroupDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,ADD_GROUP_TO_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null,temp);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test remove group from group.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveGroupFromGroup() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_GROUP_FROM_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedGroupService.removeGroupFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    
    /**
     * Test remove group from group could not remove.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveGroupFromGroupCouldNotRemove() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_GROUP_FROM_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp);
        when(mockedGroupService.removeGroupFromGroup(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    
    /**
     * Test remove group from group when host group does not exist.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveGroupFromGroupWhenHostGroupDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_GROUP_FROM_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(temp, (Group) null);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    /**
     * Test remove group from group when sender not the moderator of the host group.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveGroupFromGroupWhenSenderNotTheModeratorOfTheHostGroup() throws SQLException{
        clientRunnableObject.run();
        when(mockedGroup.getModeratorName()).thenReturn(ANOTHER_USER);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_GROUP_FROM_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(mockedGroup);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    
    /**
     * Test remove group from group when guest group does not exist.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testRemoveGroupFromGroupWhenGuestGroupDoesNotExist() throws SQLException{
        clientRunnableObject.run();
        Group temp = new Group();
        temp.setModeratorName(SENDER_NAME);
        when(networkConnectionMock.iterator()).thenReturn(resetAndAddMessages(messageList,REMOVE_GROUP_FROM_GROUP));
        when(mockedGroupService.getGroup(Mockito.anyString())).thenReturn(null,temp);
        clientRunnableObject.run();
        assertTrue(clientRunnableObject.isInitialized());
    }
    
    
    //Private fields to be used in tests
    static final String SENDER_NAME = "Alice";
    private static final String MESSAGE_TEXT = "Hello, I am Alice";
    private static final int USER_ID = 120000;
    private static final String GROUP_NAME = "FAMILY";
    private static final String PASS = "some_p@$$worD";
    private static final String INVITEE = "invitee";
    private static final String INVITER = "inviter";
    private static final Message LOGIN = Message.makeLoginMessage(SENDER_NAME, PASS);
    private static final Message REGISTER = Message.makeRegisterMessage(SENDER_NAME, PASS, PASS);
    private static final Message REGISTER2 = Message.makeRegisterMessage(SENDER_NAME, PASS, "");
    private static final Message BROADCAST = Message.makeBroadcastMessage(SENDER_NAME, MESSAGE_TEXT);
    private static final Message CREATE_INVITATION_MESSAGE = Message.makeCreateInvitationMessage(SENDER_NAME, INVITEE, GROUP_NAME);
    private static final Message DELETE_INVITATION_MESSAGE = Message.makeDeleteInvitationMessage(SENDER_NAME, INVITEE, GROUP_NAME);
    private static final Message DELETE_INVITATION_MESSAGE_INVITER = Message.makeDeleteInvitationMessage(INVITER, INVITEE, GROUP_NAME);
    private static final Message ACCEPT_INVITATION_MESSAGE = Message.makeAcceptInviteUserMessage(SENDER_NAME, GROUP_NAME);
    private static final Message DENY_INVITATION_MESSAGE = Message.makeDenyInviteUserMessage(SENDER_NAME, GROUP_NAME);
    private static final Message APPROVE_INVITATION_MESSAGE = Message.makeApproveInviteModeratorMessage(SENDER_NAME, INVITEE, GROUP_NAME);
    private static final Message DELETE_GROUP = Message.makeDeleteGroupMessage(SENDER_NAME, GROUP_NAME);
    private static final Message PRIVATE_MESSAGE = Message.makePrivateUserMessage(SENDER_NAME, "hello", "rb");
    private static final Message NULL_PRIVATE_MESSAGE = Message.makePrivateUserMessage(null, "hello", "rb");
    private static final Message DELETE_USER = Message.makeDeleteUserMessage(SENDER_NAME);
    private static final Message GET_GROUP = Message.makeGetGroupMessage(SENDER_NAME, GROUP_NAME);
    private static final Message PRIVATE_REPLY = Message.makePrivateReplyMessage(SENDER_NAME,"Alex","MESSAGEKEY");
    private static final Message FOLLOW_USER_MESSAGE = Message.makeFollowUserMessage(SENDER_NAME,"Alex");
    private static final Message UNFOLLOW_USER_MESSAGE = Message.makeUnfollowUserMessage(SENDER_NAME,"Alex");
    private static final Message GET_FOLLOWERS = Message.makeGetFollowersMessage(SENDER_NAME);
    private static final Message GET_FOLLOWEES = Message.makeGetFolloweesMessage(SENDER_NAME);
    private static final Message GET_ONLINE_USER = Message.makeGetOnlineUserMessage(SENDER_NAME);
    private static final String DUMMY_GROUP_NAME = "dummy";
    private static final String ANOTHER_DUMMY_GROUP_NAME = "dummy2";
    private static final Message CREATE_GROUP = Message.makeCreateGroupMessage(SENDER_NAME, DUMMY_GROUP_NAME);
    private static final String DUMMY_USER = "Bob";
    private static final Message ADD_USER_TO_GROUP = Message.makeAddUserToGroupMessage(SENDER_NAME, SENDER_NAME, DUMMY_GROUP_NAME);
    private static final Message ADD_USER_TO_GROUP2 = Message.makeAddUserToGroupMessage(SENDER_NAME, DUMMY_USER, DUMMY_GROUP_NAME);
    private static final Message REMOVE_USER_TO_GROUP = Message.makeRemoveUserFromGroupMessage(SENDER_NAME, DUMMY_USER, DUMMY_GROUP_NAME);
    private static final User USER_LOGGED_ON = new User(null,null,SENDER_NAME,"QWERTY",true);
    private static final User INVITEE_USER = new User(null,null,INVITEE,"test",true);
    private static final User USER_LOGGED_OFF = new User(null,null,SENDER_NAME,"QWERTY",false);
    private static final Group GROUP = new Group();
    private static final Message GROUP_MESSAGE = Message.makeGroupMessage(SENDER_NAME, MESSAGE_TEXT, DUMMY_GROUP_NAME);
    private static final String DUMMY_MSG_UNIQUE_KEY = "dummy_key";
    private static final Message QUIT_MESSAGE = Message.makeQuitMessage(SENDER_NAME);
    private static final Message SEARCH_USER_MESSAGE = Message.makeSearchMessage(SENDER_NAME, "User", "kp");
    private static final Message SEARCH_GROUP_MESSAGE = Message.makeSearchMessage(SENDER_NAME, "Group", "kk");
    private static final String DUMMY_GROUP_MESSAGE_KEY = "Alice::Group201:testTime";
    private static final String DUMMY_GROUP_MESSAGE_KEY2 = "Bob::Group201:testTime";
    private static final Message DELETE_GROUPMESSAGE_MESSAGE = Message.makeDeleteGroupMessageMessage(SENDER_NAME, DUMMY_GROUP_MESSAGE_KEY);
    private static final Message DELETE_GROUPMSG_MSG_SENDER_NOT_IN_KEY = Message.makeDeleteGroupMessageMessage(SENDER_NAME, DUMMY_GROUP_MESSAGE_KEY2);
    private static final Message DELETE_PRIVATEMESSAGE_MESSAGE = Message.makeDeletePrivateMessageMessage(SENDER_NAME, DUMMY_MSG_UNIQUE_KEY);
    private static final Message ADD_GROUP_TO_GROUP = Message.makeAddGroupToGroupMessage(SENDER_NAME, DUMMY_GROUP_NAME, ANOTHER_DUMMY_GROUP_NAME);
    private static final String ANOTHER_USER = "another_user";
    private static final Message REMOVE_GROUP_FROM_GROUP = Message.makeRemoveGroupFromGroupMessage(SENDER_NAME, DUMMY_GROUP_NAME, ANOTHER_DUMMY_GROUP_NAME);
}

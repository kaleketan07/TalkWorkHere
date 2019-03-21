package edu.northeastern.ccs.im.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.services.ConversationalMessageService;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The type Test group has the tests for the Group class
 *
 * @author Kunal
 */
public class TestGroup {

    /**
     * Test get group name.
     *
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @Test
    public void testGetGroupName() throws IllegalAccessException, NoSuchFieldException {
        Group testGroup = new Group();
        Field gn = Group.class.getDeclaredField("groupName");
        gn.setAccessible(true);
        gn.set(testGroup, TEST_GROUP_NAME);
        Assertions.assertEquals("Group201", testGroup.getGroupName());
    }

    /**
     * Test set group name.
     */
    @Test
    public void testSetGroupName() {
        Group testGroup = new Group();
        testGroup.setGroupName(TEST_GROUP_NAME);
        Assertions.assertEquals(TEST_GROUP_NAME, testGroup.getGroupName());
    }

    /**
     * Test get moderator name.
     *
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @Test
    public void testGetModeratorName() throws IllegalAccessException, NoSuchFieldException {
        Group testGroup = new Group();
        Field mod = Group.class.getDeclaredField("moderatorName");
        mod.setAccessible(true);
        mod.set(testGroup, TEST_MODERATOR_NAME);
        Assertions.assertEquals("Alice", testGroup.getModeratorName());
    }

    /**
     * Test set moderator name.
     */
    @Test
    public void testSetModeratorName() {
        Group testGroup = new Group();
        testGroup.setModeratorName(TEST_MODERATOR_NAME);
        Assertions.assertEquals(TEST_MODERATOR_NAME, testGroup.getModeratorName());
    }

    /**
     * Test get member users.
     */
    @Test
    public void testGetMemberUsers() {
        Group testGroup = new Group();
        Set<User> testUsers = new HashSet<>();
        testGroup.setMemberUsers(testUsers);
        Assertions.assertEquals(testUsers, testGroup.getMemberUsers());
    }

    /**
     * Test group send message with no member group.
     *
     * @throws SQLException the SQL exception
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test 
    public void testGroupSendMessageWithNoMemberGroup() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Group testGroup = new Group();
        testGroup.setGroupName(TEST_GROUP_NAME);
        testGroup.setModeratorName(TEST_MODERATOR_NAME);
        Set<User> users = new HashSet<>(Arrays.asList(CAROL, DAN));
        testGroup.setMemberUsers(users);
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = Group.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(testGroup, mockedCMS);
        Message msg = Message.makeLoginMessage(TEST_LOGIN, "");
        Mockito.when(DAN.userSendMessage(msg)).thenReturn(DUMMY_MSG_UNIQUE_KEY);
        Mockito.when(CAROL.userSendMessage(msg)).thenReturn(DUMMY_MSG_UNIQUE_KEY);
        testGroup.groupSendMessage(msg, DUMMY_MSG_UNIQUE_KEY);
        Mockito.verify(mockedCMS, Mockito.atLeastOnce()).insertGroupConversationalMessage(DUMMY_MSG_UNIQUE_KEY, DUMMY_MSG_UNIQUE_KEY);
        
        
    }
    
    /**
     * Test group send message with user present in sub groups.
     *
     * @throws SQLException the SQL exception
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test 
    public void testGroupSendMessageWithUserPresentInSubGroups() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Group testGroup = new Group();
        testGroup.setGroupName(TEST_GROUP_NAME);
        testGroup.setModeratorName(TEST_MODERATOR_NAME);
        Set<User> users = new HashSet<>(Arrays.asList(DAN, CAROL));
        testGroup.setMemberUsers(users);
        ConversationalMessageService mockedCMS = mock(ConversationalMessageService.class);
        Field fieldCMS = Group.class.getDeclaredField("cms");
        fieldCMS.setAccessible(true);
        fieldCMS.set(testGroup, mockedCMS);
        Group testGroup2 = new Group();
        testGroup2.setGroupName(TEST_GROUP_NAME_1);
        testGroup2.setModeratorName(TEST_MODERATOR_NAME);
        Set<User> users2 = new HashSet<>(Arrays.asList(DAN, GARY,BOB));
        testGroup2.setMemberUsers(users2);
        Set<Group> groups = new HashSet<>(Arrays.asList(testGroup2));
        testGroup.setMemberGroups(groups);
        Message msg = Message.makeLoginMessage(TEST_LOGIN, "");
        Mockito.when(DAN.userSendMessage(msg)).thenReturn(DUMMY_MSG_UNIQUE_KEY);
        Mockito.when(CAROL.userSendMessage(msg)).thenReturn(DUMMY_MSG_UNIQUE_KEY);
        Mockito.when(GARY.userSendMessage(msg)).thenReturn(DUMMY_MSG_UNIQUE_KEY);
        testGroup.groupSendMessage(msg, DUMMY_MSG_UNIQUE_KEY);
        Mockito.verify(mockedCMS, Mockito.atLeastOnce()).insertGroupConversationalMessage(DUMMY_MSG_UNIQUE_KEY, DUMMY_MSG_UNIQUE_KEY);
        
    }
    
   
    private static final User CAROL = Mockito.mock(User.class);
    private static final User DAN = Mockito.mock(User.class);
    private static final User BOB = Mockito.mock(User.class);
    private static final User GARY = Mockito.mock(User.class);
    private final String TEST_GROUP_NAME = "Group201";
    private final String TEST_GROUP_NAME_1 = "Group202";
    private final String TEST_MODERATOR_NAME = "Alice";
    private final String TEST_LOGIN = "login";
    private final String DUMMY_MSG_UNIQUE_KEY = "dummy_key";
}

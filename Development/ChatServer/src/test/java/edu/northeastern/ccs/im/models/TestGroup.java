package edu.northeastern.ccs.im.models;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

/**
 * The type Test group has the tests for the Group class
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
    public void testGetGroupName() throws IllegalAccessException,NoSuchFieldException{
        Group testGroup = new Group();
        Field gn = Group.class.getDeclaredField("groupName");
        gn.setAccessible(true);
        gn.set(testGroup,TEST_GROUP_NAME);
        Assertions.assertEquals("Group201",testGroup.getGroupName());
    }

    /**
     * Test set group name.
     */
    @Test
    public void testSetGroupName(){
        Group testGroup = new Group();
        testGroup.setGroupName(TEST_GROUP_NAME);
        Assertions.assertEquals(TEST_GROUP_NAME,testGroup.getGroupName());
    }

    /**
     * Test get moderator name.
     *
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchFieldException   the no such field exception
     */
    @Test
    public void testGetModeratorName() throws IllegalAccessException,NoSuchFieldException{
        Group testGroup = new Group();
        Field mod = Group.class.getDeclaredField("moderatorName");
        mod.setAccessible(true);
        mod.set(testGroup,TEST_MODERATOR_NAME);
        Assertions.assertEquals("Alice",testGroup.getModeratorName());
    }

    /**
     * Test set moderator name.
     */
    @Test
    public void testSetModeratorName(){
        Group testGroup = new Group();
        testGroup.setModeratorName(TEST_MODERATOR_NAME);
        Assertions.assertEquals(TEST_MODERATOR_NAME,testGroup.getModeratorName());
    }


    private final String TEST_GROUP_NAME = "Group201";
    private final String  TEST_MODERATOR_NAME = "Alice";
}

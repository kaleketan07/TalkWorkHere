package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.models.User;

/**
 * TestMessage class contains the test suite tests the Message.java class under
 * the im.client package. It tests all the different type of message creations,
 * message object verifications for desired and undesired input and toString
 * checks.
 *
 * @author Team 201 - Sachin Haldavanekar
 * @version 1.0
 */
public class TestMessage {
    /**
     * Test to check if makeQuitMessage creates the necessary object
     * when the desired values are given as input.
     */
    @Test
    public void testMakeQuitMessage() {
        Message quitMessage = Message.makeQuitMessage(SENDER_NAME);
        assertEquals(SENDER_NAME, quitMessage.getName());
    }

    /**
     * Test to check if makeBroadcastMessage creates the necessary object
     * when the desired values are given as input.
     */
    @Test
    public void testMakeBroadcastMessage() {
        Message broadcastMessage = Message.makeBroadcastMessage(SENDER_NAME, MESSAGE_TEXT);
        assertEquals(SENDER_NAME, broadcastMessage.getName());
        assertEquals(MESSAGE_TEXT, broadcastMessage.getTextOrPassword());
    }

    /**
     * Test to check if makeHelloMessage creates the necessary object
     * when the desired values are given as input.
     */
    @Test
    public void testMakeHelloMessage() {
        Message helloMessage = Message.makeHelloMessage(MESSAGE_TEXT);
        assertEquals(MESSAGE_TEXT, helloMessage.getTextOrPassword());
    }

    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Quit.
     */
    @Test
    public void testMakeMessageForQuit() {
        Message message = Message.makeMessage(BYE, SENDER_NAME, "", "");
        assertEquals(SENDER_NAME, message.getName());
    }

    /**
     * Test LoginMessage and toString method to return the expected
     * output for a login message
     */
    @Test
    public void testLoginMessage() {
        Message message = Message.makeLoginMessage(SENDER_NAME, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(LGN);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(PASS));
        strBuild.append(" "+NULL_OUTPUT.length()+" "+NULL_OUTPUT);
        assertEquals(strBuild.toString(), message.toString());
    }

    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Hello.
     */
    @Test
    public void testMakeMessageForHello() {
        Message message = Message.makeMessage(HLO, SENDER_NAME, "", "");
        assertEquals(SENDER_NAME, message.getName());
    }

    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Broadcast.
     */
    @Test
    public void testMakeMessageForBroadcast() {
        Message message = Message.makeMessage(BCT, SENDER_NAME, MESSAGE_TEXT, "");
        assertEquals(SENDER_NAME, message.getName());
        assertEquals(MESSAGE_TEXT, message.getTextOrPassword());
    }

    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Login.
     */
    @Test
    public void testMakeMessageForLogin() {
        Message message = Message.makeMessage(LGN, SENDER_NAME, PASS, "");
        assertEquals(SENDER_NAME, message.getName());
        assertEquals(PASS, message.getTextOrPassword());
        assertTrue(message.isLoginMessage());
        assertFalse(message.isRegisterMessage());
    }

    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Register.
     */
    @Test
    public void testMakeMessageForRegister() {
        Message message = Message.makeMessage(REG, SENDER_NAME, PASS, PASS);
        assertEquals(SENDER_NAME, message.getName());
        assertEquals(PASS, message.getTextOrPassword());
        assertEquals(PASS, message.getReceiverOrPassword());
        assertFalse(message.isLoginMessage());
        assertTrue(message.isRegisterMessage());
        assertFalse(message.isCreateGroupMessage());
        assertFalse(message.isPrivateUserMessage());
    }
    
    /**
     * Test to check if makeMessage creates the correct object
     * based on the first parameter passed - Message_User Message.
     */
    @Test
    public void testMakeMessageForPrivateUserMessage() {
        Message message = Message.makeMessage(MSU, SENDER_NAME, PASS, PASS);
        assertEquals(SENDER_NAME, message.getName());
        assertEquals(PASS, message.getTextOrPassword());
        assertEquals(PASS, message.getReceiverOrPassword());
        assertTrue(message.isPrivateUserMessage());
        assertFalse(message.isGetGroupMessage());
    }

    /**
     * Test condition in makeMessage for type CreateGroupMessage
     */
    @Test
    public void testMakeMessageForCreateGroupMessage() {
        Message message = Message.makeMessage(CRG, SENDER_NAME, GROUP_NAME, "");
        assertEquals(SENDER_NAME, message.getName());
        assertEquals(GROUP_NAME, message.getTextOrPassword());
        assertFalse(message.isLoginMessage());
        assertTrue(message.isCreateGroupMessage());
    }

    /**
     * Test to check if makeMessage returns null when the first parameter
     * is an empty string.
     */
    @Test
    public void testMakeMessageForEmptyString() {
        assertNull(Message.makeMessage("", SENDER_NAME, MESSAGE_TEXT, ""));
    }

    /**
     * Test the isBroadcastMessage method to return true when called on the
     * correct message type.
     */
    @Test
    public void testIsBroadcastMessage() {
        Message message = Message.makeBroadcastMessage(SENDER_NAME, MESSAGE_TEXT);
        assertTrue(message.isBroadcastMessage());
    }

    /**
     * Test the isInitialization method to return true when called on the
     * correct message type.
     */
    @Test
    public void testIsInitialization() {
        Message message = Message.makeHelloMessage(MESSAGE_TEXT);
        assertTrue(message.isInitialization());
    }

    /**
     * Test the terminate method to return true when called on the
     * correct message type.
     */
    @Test
    public void testTerminate() {
        Message message = Message.makeQuitMessage(SENDER_NAME);
        assertTrue(message.terminate());
    }

    /**
     * Test the isBroadcastMessage method to return false when called on the
     * wrong message type.
     */
    @Test
    public void testFailIsBroadcastMessage() {
        Message message = Message.makeHelloMessage(MESSAGE_TEXT);
        assertFalse(message.isBroadcastMessage());
    }

    /**
     * Test the isInitialization method to return false when called on the
     * wrong message type.
     */
    @Test
    public void testFailIsInitialization() {
        Message message = Message.makeBroadcastMessage(SENDER_NAME, MESSAGE_TEXT);
        assertFalse(message.isInitialization());
    }

    /**
     * Test the terminate method to return false when called on the
     * wrong message type.
     */
    @Test
    public void testFailTerminate() {
        Message message = Message.makeHelloMessage(MESSAGE_TEXT);
        assertFalse(message.terminate());
    }

    /**
     * Test toString method to return the expected output when
     * sender is not null and text is null
     */
    @Test
    public void testToStringSenderNotNullTextNull() {
        Message message = Message.makeQuitMessage(SENDER_NAME);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(BYE);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
    }

    /**
     * Test toString method to return the expected output when
     * sender is not null and text is not null
     */
    @Test
    public void testToStringSenderNotNullTextNotNull() {
        Message message = Message.makeBroadcastMessage(SENDER_NAME, MESSAGE_TEXT);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(BCT);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(MESSAGE_TEXT));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
    }

    /**
     * Test toString method to return the expected output when
     * ReceiverorPassword not null
     */
    @Test
    public void testToStringReceiveOrPasswordNotNull() {
        Message message = Message.makeRegisterMessage(SENDER_NAME, PASS, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(REG);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(PASS));
        strBuild.append(toStringHelper(PASS));
        assertEquals(strBuild.toString(), message.toString());
    }

    /**
     * Test makeMessage with Delete_Group as the handle
     */
    @Test
    public void testMakeMessageDeleteGroupCondition() {
        Message message = Message.makeMessage(DEG, SENDER_NAME, PASS, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(DEG);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(PASS));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
        assertTrue(message.isDeleteGroupMessage());
        assertFalse(message.isDeleteUserMessage());
        assertFalse(message.isPrivateReplyMessage());
    }
    
    /**
     * Test makeMessage with Private_Reply as the handle
     */
    @Test
    public void testMakeMessagePrivateReplyCondition() {
        Message message = Message.makeMessage(PRE, SENDER_NAME, PASS, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(PRE);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(PASS));
        strBuild.append(toStringHelper(PASS));
        assertEquals(strBuild.toString(), message.toString());
        assertTrue(message.isPrivateReplyMessage());
    }
    
    
    /**
     * Test makePrattleMessage()
     */
    @Test
    public void testMakeMessagePrattleMessage() {
        Message message = Message.makePrattleMessage(PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(PRM);
        strBuild.append(toStringHelper(PRATTLE));
        strBuild.append(toStringHelper(PASS));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
        
    }
    
    /**
     * Test makeMessage with Delete_User as the handle
     */
    @Test
    public void testMakeMessageDeleteUserCondition() {
        Message message = Message.makeMessage(DLU, SENDER_NAME, PASS, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(DLU);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
        assertTrue(message.isDeleteUserMessage());
        assertFalse(message.isRemoveUserFromGroupMessage());
    }
    
    /**
    * Test addUniqueKey with PrivateReply as the handle
    */
   @Test
   public void testAddUniqueKeyWithPrivateReply() {
       Message message = Message.makeMessage(PRE, SENDER_NAME, PASS,NULL_OUTPUT);
       message = Message.addUniqueKeyToMsg(message, "ABC");
       assertEquals(message.getTextOrPassword(), "ABC");
   }
    
   /**
    * Test addUniqueKey with MessageUser as the handle
    */
   @Test
   public void testAddUniqueKeyWithMessageUser() {
       Message message = Message.makeMessage(MSU, SENDER_NAME, PASS,NULL_OUTPUT);
       message = Message.addUniqueKeyToMsg(message, "ABC");
       assertEquals(message.getTextOrPassword(), "ABC");
   }
   
    
   /**
    * Test addUniqueKey with DeleteUser as the handle
    */
   @Test
   public void testAddUniqueKeyWithInvalidHandle() {
       Message message = Message.makeMessage(DLU, SENDER_NAME, PASS,NULL_OUTPUT);
       String msgText = message.getTextOrPassword();
       message = Message.addUniqueKeyToMsg(message, "ABC");
       assertEquals(message.getTextOrPassword(), msgText);
   }
   
   
    /**
     * Test makeMessage with Remove_user as the handle
     */
    @Test
    public void testMakeMessageRemoveUserFromGroupCondition() {
        Message message = Message.makeMessage(RUG, SENDER_NAME, GROUP_NAME, NULL_OUTPUT);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(RUG);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(GROUP_NAME));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
        assertTrue(message.isRemoveUserFromGroupMessage());
    }
    
    /**
     * Test makeMessage with Get_Group as the handle
     */
    @Test
    public void testMakeMessageGetGroupCondition() {
        Message message = Message.makeMessage(GTG, SENDER_NAME, GROUP_NAME,PASS );
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(GTG);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(GROUP_NAME));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
        assertTrue(message.isGetGroupMessage());
        assertFalse(message.isDeleteGroupMessage());
    }

    /**
     * Test make message for user profile update message.
     */
    @Test
    public void testMakeMessageForUserProfileUpdateMessage(){
        Message message = Message.makeMessage(UPU, SENDER_NAME,"Alex","Predna");
        Assertions.assertEquals(SENDER_NAME, message.getName());
        Assertions.assertEquals("Alex", message.getTextOrPassword());
        Assertions.assertEquals("Predna", message.getReceiverOrPassword());
        Assertions.assertTrue(message.isUserProfileUpdateMessage());
    }

    /**
     * Test make user profile update message.
     */
    @Test
    public void testMakeUserProfileUpdateMessage(){
        Message message = Message.makeUserProfileUpdateMessage(SENDER_NAME,"Alex","Predna");
        Assertions.assertEquals(SENDER_NAME,message.getName());
        Assertions.assertEquals("Alex",message.getTextOrPassword());
        Assertions.assertEquals("Predna",message.getReceiverOrPassword());
    }

    /**
     * Test is user profile update message for false.
     */
    @Test
    public void testIsUserProfileUpdateMessageForFalse(){
        Message message = Message.makeQuitMessage(SENDER_NAME);
        Assertions.assertFalse(message.isUserProfileUpdateMessage());
    }

    @Test
    public void testMakeMessageAddUserGroupMessageCondition() {
        Message message = Message.makeMessage(AUG, SENDER_NAME, GROUP_NAME, ANOTHER_USER);
        Assertions.assertTrue(message.isAddUserToGroupMessage());
    }

    /**
     * Test toString method to return the expected output when
     * sender is null and text is not null
     */
    @Test
    public void testToStringSenderNullTextNotNull() {
        Message message = Message.makeHelloMessage(MESSAGE_TEXT);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(HLO);
        strBuild.append(toStringHelper(NULL_OUTPUT));
        strBuild.append(toStringHelper(MESSAGE_TEXT));
        strBuild.append(toStringHelper(NULL_OUTPUT));
        assertEquals(strBuild.toString(), message.toString());
    }

    
    /**
     * Test message already sent for true.
     */
    @Test
    public void testMessageAlreadySentForTrue() {
    	Message msg = Message.makeHelloMessage(HLO);
    	User u = new User("","",SENDER_NAME, PASS, false);
    	assertTrue(msg.addUserToRecipients(u));
    	assertTrue(msg.messageAlreadySent(u));
    }
    
    /**
     * Test message already sent for false.
     */
    @Test
    public void testMessageAlreadySentForFalse() {
    	Message msg = Message.makeHelloMessage(HLO);
    	User u = new User("","",SENDER_NAME, PASS, false);
    	assertFalse(msg.messageAlreadySent(u));
    }
    
    /**
     * Test add user to recipients for true.
     */
    @Test
    public void testAddUserToRecipientsForTrue() {
    	Message msg = Message.makeHelloMessage(HLO);
    	User u = new User("","",SENDER_NAME, PASS, false);
    	assertTrue(msg.addUserToRecipients(u));
    }

    /**
     * Test add user to recipients for false.
     */
    @Test
    public void testAddUserToRecipientsForFalse() {
    	Message msg = Message.makeHelloMessage(HLO);
    	User u = new User("","",SENDER_NAME, PASS, false);
    	assertTrue(msg.addUserToRecipients(u));
    }
    
    
    

    /**
     * A private helper method to generate toString output for the given
     * parameter.
     *
     * @param parameter - a string which is used while creating the formatted
     *                  return string
     * @return - the string that will be printed for the given parameter.
     */
    private String toStringHelper(String parameter) {
        final String SPACE = " ";
        return SPACE + parameter.length() + SPACE + parameter;
    }
    
    /**
     * CONSTANTS to be used as expected values or method arguments
     **/
    private static final String HLO = "HLO";
    private static final String BYE = "BYE";
    private static final String BCT = "BCT";
    private static final String LGN = "LGN";
    private static final String REG = "REG";
    private static final String DEG = "DEG";
    private static final String CRG = "CRG";
    private static final String MSU = "MSU";
    private static final String GTG = "GTG";
    private static final String UPU = "UPU";
    private static final String DLU = "DLU";
    private static final String RUG = "RUG";
    private static final String AUG = "AUG";
    private static final String PRE = "PRE";
    private static final String PRM = "PRM";
    private static final String NULL_OUTPUT = "--";
    private static final String SENDER_NAME = "Alice";
    private static final String PRATTLE = "Prattle";
    private static final String MESSAGE_TEXT = "Hello, I am Alice";
    private static final String PASS = "some_p@$$worD";
    private static final String GROUP_NAME = "group";
    private static final String ANOTHER_USER = "another-user";
}

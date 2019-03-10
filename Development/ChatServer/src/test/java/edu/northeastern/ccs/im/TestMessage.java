package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
    private void testLoginMessage() {
        Message message = Message.makeLoginMessage(SENDER_NAME, PASS);
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(LGN);
        strBuild.append(toStringHelper(SENDER_NAME));
        strBuild.append(toStringHelper(PASS));
        assertEquals(message.toString(), strBuild.toString());
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
	    Message message = Message.makeMessage(REG, SENDER_NAME, PASS,PASS);
		assertEquals(SENDER_NAME, message.getName());
		assertEquals(PASS, message.getTextOrPassword());
	    assertEquals(PASS, message.getReceiverOrPassword());
	    assertFalse(message.isLoginMessage());
	    assertTrue(message.isRegisterMessage());
	    assertFalse(message.isCreateGroupMessage());
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
    private static final String CRG = "CRG";
    private static final String NULL_OUTPUT = "--";
    private static final String SENDER_NAME = "Alice";
    private static final String MESSAGE_TEXT = "Hello, I am Alice";
    private static final String PASS = "some_p@$$worD";
    private static final String GROUP_NAME = "group";
}

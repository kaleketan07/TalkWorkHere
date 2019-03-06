package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

/**
 * Test for ConversationalMessage class
 * 
 * @author rahul
 *
 */
public class TestConversationalMessage {
	
	private ConversationalMessage TESTMESSAGE = new ConversationalMessage("AB", "ABC", "hello", Timestamp.valueOf("1966-08-30 08:08:08"), "ABABC2018:05:05");

	
	/**
	 * Test for getSourceName() 
	 */
	@Test
	public void testGetSourceName() {
		assertEquals(TESTMESSAGE.getSourceName(), "AB");
	}
	
	/**
	 * Test for getDestination()
	 */
	@Test
	public void testGetDestinationName() {
		assertEquals(TESTMESSAGE.getDestinationName(), "ABC");
	}
	
	/**
	 * Test for getMessageText()
	 */
	@Test
	public void testgetMessageText() {
		assertEquals(TESTMESSAGE.getMessageText(), "hello");
	}
	
	/**
	 * Test for getMessageUniquekey()
	 */
	@Test
	public void testMessageUniquekey() {
		assertEquals(TESTMESSAGE.getMessageUniquekey(), "ABABC2018:05:05");
	}
	
	/**
	 * Test for getMessageTimeStamp()
	 */
	@Test
	public void testGetMessageTimeStamp() {
		assertEquals(TESTMESSAGE.getMessageTimeStamp(), Timestamp.valueOf("1966-08-30 08:08:08"));
	}
	
	
	/**
	 * Test for toString()
	 */
	@Test
	public void testToString() {
		assertEquals(TESTMESSAGE.toString(), "ABABC2018:05:05");
	}

}

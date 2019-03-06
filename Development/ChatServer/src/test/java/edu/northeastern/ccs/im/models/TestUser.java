package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestUser {
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

	private static final User ALICE = new User("Alice", "Bob", "alicebob", "password", false);
	private static final String RUBY = "RUBY";
	private static final String ALICERUBY = "aliceruby";
}

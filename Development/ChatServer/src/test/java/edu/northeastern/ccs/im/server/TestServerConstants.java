package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class contains the test suite for the ServerConstants class. These
 * tests do not increase the test coverage but they ensure that any accidental
 * changes made to the constants get caught in the unit tests period and avoid
 * causing any system failure later.
 * 
 * @author Sachin Haldavanekar
 * @version 1.0
 *
 */
public class TestServerConstants {
	/**
	 * This test verifies the port number
	 */
	@Test
	public void testVerifyPort() {
		assertEquals(4545, ServerConstants.PORT);
	}
	
	/**
	 * This test verifies the delay in milliseconds
	 */
	@Test
	public void testVerifyDelayInMs() {
		assertEquals(50, ServerConstants.DELAY_IN_MS);
	}
	
	/**
	 * This test verifies the thread pool size
	 */
	@Test
	public void testVerifyThreadPoolSize() {
		assertEquals(20, ServerConstants.THREAD_POOL_SIZE);
	}
	
	/**
	 * This test verifies the client check delay
	 */
	@Test
	public void testVerifyClientCheckDelay() {
		assertEquals(200, ServerConstants.CLIENT_CHECK_DELAY);
	}
	
	/**
	 * This test verifies the server name
	 */
	@Test
	public void testVerifyServerName() {
		assertEquals("Prattle", ServerConstants.SERVER_NAME);
	}
	
	/**
	 * This test verifies bouncer id
	 */
	@Test
	public void testVerifyBouncerId() {
		assertEquals("Bouncer", ServerConstants.BOUNCER_ID);
	}

	/**
	 * This test calls the private constructor to ensure it does not throw any exception.
	 */
	@Test
	public void testPrivateConstructor() {
		@SuppressWarnings("unchecked")
		Constructor<ServerConstants> constructor= (Constructor<ServerConstants>) ServerConstants.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		assertDoesNotThrow(() -> constructor.newInstance());
	}
}

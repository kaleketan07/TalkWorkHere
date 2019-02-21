package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.*;

import org.junit.jupiter.api.Test;

/**
 * This class contains the test suite for ChatLogger.java class.
 * @author Sachin Haldavanekar
 * @version 1.0
 */
class TestChatLogger {

	/**
	 * Test the static method error to print the expected error message with SEVERE log level.
	 */
	@Test
	void testError() {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);

		try {
			ChatLogger.error("some error message");

			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("SEVERE: some error message"));
		} finally {
			logger.removeHandler(handler);
		}
	}

	/**
	 * Test the static method warning to print the expected error message with WARNING log level.
	 */
	@Test
	void testWarning() {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);

		try {

			ChatLogger.warning("some warning message");

			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("WARNING: some warning message"));
		} finally {
			logger.removeHandler(handler);
		}
	}

	/**
	 * Test the static method info to print the expected error message with INFO log level.
	 */
	@Test
	void testInfo() throws IllegalArgumentException {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);

		try {

			ChatLogger.info("some info message");

			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("INFO: some info message"));
		} finally {
			logger.removeHandler(handler);
		}
	}

	/**
	 * Test the private constructor and verify if it throws an IllegalStateException.
	 */
	@Test
	void testPrivateConstructor() throws  IllegalArgumentException, IllegalAccessException, InstantiationException {
		@SuppressWarnings("unchecked")
		Constructor<ChatLogger> constructor = (Constructor<ChatLogger>) ChatLogger.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
 		} catch(InvocationTargetException e) {
			assertEquals("ChatLogger not instantiable", e.getCause().getMessage());
			assertEquals(IllegalStateException.class, e.getCause().getClass());
		}
		constructor.setAccessible(false);
	}

	/**
	 * Test the Logger for SetMode only for console handler.
	 */
	@Test
	void testSetModeForConsole() {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ChatLogger.setMode(ChatLogger.HandlerType.CONSOLE);
		assertEquals(ConsoleHandler.class,logger.getHandlers()[logger.getHandlers().length - 1].getClass());
	}

	/**
	 * Test the Logger for SetMode only for file handler.
	 */
	@Test
	void testSetModeForFile() {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ChatLogger.setMode(ChatLogger.HandlerType.FILE);
		assertEquals(FileHandler.class,logger.getHandlers()[logger.getHandlers().length - 1].getClass());
	}
}

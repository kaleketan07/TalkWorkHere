package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import edu.northeastern.ccs.im.ChatLogger;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.client.IMConnection;
 

/**
 * The class TestPrattle contains the unit tests for methods and fields of class Prattle.
 * 
 * @author - Team 201 - Ketan Kale
 */ 
public class TestPrattle {
	
	/**
	 * Test broadcastMessage() sends a BroadCastMessage.
	 *
	 * @throws NoSuchFieldException the no such field exception
	 * @throws SecurityException the security exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	@Test	
	public void testBroadcastMessage() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		
		Field activeField = Prattle.class.getDeclaredField("active");
		activeField.setAccessible(true);
		ClientRunnable testDead1 = Mockito.mock(ClientRunnable.class);
		ClientRunnable testDead2 = Mockito.mock(ClientRunnable.class);
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>(Arrays.asList(testDead1, testDead2)));
		activeField.setAccessible(false);
		Message m = Message.makeBroadcastMessage("Alice", "Hey there");
		Mockito.when(testDead1.isInitialized()).thenReturn(true);
		Prattle.broadcastMessage(m);
		Mockito.verify(testDead1).enqueueMessage(m);
		activeField.setAccessible(true);
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>());
		activeField.setAccessible(false);
	}
	
	/**
	 * Test stopServer() stops the Server.
	 *
	 * @throws NoSuchFieldException the no such field exception
	 * @throws SecurityException the security exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	@Test
	public void testStopServer() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {		
		Field isReadyField = Prattle.class.getDeclaredField("isReady");
		isReadyField.setAccessible(true);
		isReadyField.set(Prattle.class, true);
		Prattle.stopServer();
		isReadyField.setAccessible(true);
		boolean newValue = (boolean) isReadyField.get(Prattle.class.getName());
		assertFalse(newValue);
	}
	
	/**
	 * Test createClientThread() creates an active Client.
	 *
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchFieldException the no such field exception
	 */
	@Test
	public void testCreateClientThread() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		Method method = Prattle.class.getDeclaredMethod("createClientThread", ServerSocketChannel.class, ScheduledExecutorService.class);
		method.setAccessible(true);
		ServerSocketChannel ssc = Mockito.mock(ServerSocketChannel.class);
		ScheduledExecutorService ses = Mockito.mock(ScheduledExecutorService.class);
		SocketChannel actChannel = SocketChannel.open();
		Mockito.when(ssc.accept()).thenReturn(actChannel);
		method.invoke(Prattle.class, ssc, ses);
		Field activeField = Prattle.class.getDeclaredField("active");
		activeField.setAccessible(true);
		ConcurrentLinkedQueue activeList = (ConcurrentLinkedQueue) activeField.get(Prattle.class.getName());
		assertEquals(activeList.size(), 1);
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>());
		activeField.setAccessible(false);
		
	}
	
	/**
	 * Test createClientThread() with null SocketChannel.
	 *
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchFieldException the no such field exception
	 */
	@Test
	public void testCreateClientThreadForNullSocket() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		
		Method method = Prattle.class.getDeclaredMethod("createClientThread", ServerSocketChannel.class, ScheduledExecutorService.class);
		method.setAccessible(true);
		ServerSocketChannel ssc = Mockito.mock(ServerSocketChannel.class);
		ScheduledExecutorService ses = Mockito.mock(ScheduledExecutorService.class);
		Mockito.when(ssc.accept()).thenReturn(null);
		method.invoke(Prattle.class, ssc, ses);
		Field activeField = Prattle.class.getDeclaredField("active");
		activeField.setAccessible(true);
		ConcurrentLinkedQueue activeList = (ConcurrentLinkedQueue) activeField.get(Prattle.class.getName());
		assertEquals(0, activeList.size());
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>());
		activeField.setAccessible(false);
		
	}
	
	/**
	 * Test createClientThread() should throw assertion error.
	 *
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchFieldException the no such field exception
	 */
	@Test
	public void testCreateClientThreadForAssertionError() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);
		Method method = Prattle.class.getDeclaredMethod("createClientThread", ServerSocketChannel.class, ScheduledExecutorService.class);
		method.setAccessible(true);
		ServerSocketChannel ssc = Mockito.mock(ServerSocketChannel.class);
		ScheduledExecutorService ses = Mockito.mock(ScheduledExecutorService.class);
		Error e = new AssertionError();
		Mockito.doThrow(e).when(ssc).accept();
		try {
			method.invoke(Prattle.class, ssc, ses);
			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("Caught Assertion: " + e.toString()));
		} finally {
			logger.removeHandler(handler);
		}
	}
	
	/**
	 * Test createClientThread() method should throw IO exception.
	 *
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchFieldException the no such field exception
	 */
	@Test
	public void testCreateClientThreadForIOException() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);
		Method method = Prattle.class.getDeclaredMethod("createClientThread", ServerSocketChannel.class, ScheduledExecutorService.class);
		method.setAccessible(true);
		ServerSocketChannel ssc = Mockito.mock(ServerSocketChannel.class);
		ScheduledExecutorService ses = Mockito.mock(ScheduledExecutorService.class);
		Exception e = new IOException();
		Mockito.doThrow(e).when(ssc).accept();
		try {
			method.invoke(Prattle.class, ssc, ses);
			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("Caught Exception: " + e.toString()));
		} finally {
			logger.removeHandler(handler);
		}
	}
	
	/**
	 * Test removeClient() removes a client.
	 *
	 * @throws NoSuchFieldException the no such field exception
	 * @throws SecurityException the security exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	@Test
	public void testRemoveClient() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Logger logger = Logger.getLogger(ChatLogger.class.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SimpleFormatter formatter = new SimpleFormatter();
		Handler handler = new StreamHandler(out, formatter);
		logger.addHandler(handler);
		Field activeField = Prattle.class.getDeclaredField("active");
		activeField.setAccessible(true);
		ClientRunnable testDead1 = Mockito.mock(ClientRunnable.class);
		ClientRunnable testDead2 = Mockito.mock(ClientRunnable.class);
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>(Arrays.asList(testDead1)));
		activeField.setAccessible(false);
		Prattle.removeClient(testDead1);
		try {
			Prattle.removeClient(testDead2);
			handler.flush();
			String message = out.toString();
			assertTrue(message.contains("Could not find a thread that I tried to remove!"));
		} finally {
			logger.removeHandler(handler);
			activeField.setAccessible(true);
			activeField.set(Prattle.class, new ConcurrentLinkedQueue<>());
			activeField.setAccessible(false);
		}
		
	}
	
	// create a private class to be used in testMain() as a thread
	private static class PrattleThread implements Runnable {
	    @Override
	    public void run() {
	      String[] args = new String[0];
	      Prattle.main(args);
	    }
	  }
	
	/**
	 * Test main method runs in a thread. Uses the IMConnection class from client
	 *
	 * @throws NoSuchFieldException the no such field exception
	 * @throws SecurityException the security exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	@Test
	public void testMain() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Thread testPrattleThread = new Thread(new PrattleThread());
	    testPrattleThread.start();
	    System.setIn(new ByteArrayInputStream("".getBytes()));
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outStream));
	    IMConnection connection = new IMConnection("localhost", 4545, "Alice");
	    connection.connect();
	    Prattle.stopServer();
	    Field activeField = Prattle.class.getDeclaredField("active");
		activeField.setAccessible(true);
		ConcurrentLinkedQueue activeList = (ConcurrentLinkedQueue) activeField.get(Prattle.class.getName());
		activeField.set(Prattle.class, new ConcurrentLinkedQueue<>());
		activeField.setAccessible(false);
	  }	
}

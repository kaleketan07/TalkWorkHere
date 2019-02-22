package edu.northeastern.ccs.im;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TestNetworkConnection class contains all the unit tests for the java class NetworkConnection
 * It has various test for each of the method and also for testing some branch conditions
 * using desired scenarios. Some more branch conditions will be covered in version 1.1
 *
 * @author Team 201 - Kunal
 * @version 1.0
 */
public class TestNetworkConnection {

    /**
     * Test the constructor of NetworkConnection and make sure a non null object
     * is created.
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
    public void testConstructor() throws IOException{
        NetworkConnection conn;
        SocketChannel channel = SocketChannel.open();
        conn = new NetworkConnection(channel);
        assertNotNull(conn);
    }

    /**
     * Test the constructor's exception thrown, by forcing an exception which
     * is emulated by prematurely closing the SocketChannel before making a new
     * NetworkConnection
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
    public void testConstructorException() throws IOException{
        SocketChannel channel = SocketChannel.open();
        channel.close();
        assertThrows(AssertionError.class, () -> new NetworkConnection(channel));
    }


    /**
     * Test sendMessage method of the class. Ensure that a message was sent over the network.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testSendMessage() throws IOException,NoSuchFieldException,IllegalAccessException{
        SocketChannel chann = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(chann);
        Field f = NetworkConnection.class.getDeclaredField("channel");
        f.setAccessible(true);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        f.set(conn,mockedChannel);
        when(mockedChannel.write(ByteBuffer.wrap("HLO 2 -- 4 Test".getBytes()))).thenReturn(10);
        assertFalse(conn.sendMessage(Message.makeHelloMessage("Hello")));
    }

    /**
     * Test send message exception. Force an exception by prematurely closing the connection
     * before sending the message
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
    public void testSendMessageException() throws IOException{
        SocketChannel channel = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(channel);
        conn.close();
        assertFalse(conn.sendMessage(Message.makeHelloMessage("Test")));
    }

    /**
     * Test close method of the NetworkConnection class. Ensure that the channel is not open.
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
    public void testCloseMethod() throws IOException{
        SocketChannel channel = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(channel);
        conn.close();
        assertFalse(channel.isOpen());
    }

    /**
     * Test close exception. Force an exception by mocking the Selector and force to throw an exception
     * when the selector is closed.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testCloseException() throws IOException, NoSuchFieldException, IllegalAccessException{
        Selector mockedSelector = mock(Selector.class);
        SocketChannel channel = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(channel);
        Field f = NetworkConnection.class.getDeclaredField("selector");
        f.setAccessible(true);
        f.set(conn,mockedSelector);
        doThrow(IOException.class).when(mockedSelector).close();
        assertThrows(AssertionError.class, () -> conn.close());
    }

    /**
     * Test iterator method of the class. Use java Reflection to inject messages in the
     * ConcurrentLinkedQueue and iterate over these messages.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testIteratorMethod() throws IOException, NoSuchFieldException, IllegalAccessException{
        SocketChannel channel = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(channel);
        Field f = NetworkConnection.class.getDeclaredField("messages");
        f.setAccessible(true);
        Message m1 = Message.makeHelloMessage("Test");
        Message m2 = Message.makeHelloMessage("Test");
        f.set(conn, new ConcurrentLinkedQueue<>(Arrays.asList(m1, m2)));
        Field c = NetworkConnection.class.getDeclaredField("channel");
        c.setAccessible(true);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        when(mockedChannel.read(ByteBuffer.allocate(64))).thenReturn(64);
        c.set(conn,mockedChannel);
        Iterator<Message> itr = conn.iterator();
        assertTrue(itr.hasNext());
     }


    /**
     * Test hasNext() throws an exception when the Selector fails to select a key.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
     public void testIteratorHasNextException() throws IOException,NoSuchFieldException,IllegalAccessException{
         SocketChannel channel = SocketChannel.open();
         NetworkConnection conn = new NetworkConnection(channel);
         Field f = NetworkConnection.class.getDeclaredField("selector");
         f.setAccessible(true);
         Selector mockedSelector = mock(Selector.class);
         doThrow(IOException.class).when(mockedSelector).selectNow();
         f.set(conn,mockedSelector);
         Iterator<Message> itr = conn.iterator();
         assertThrows(AssertionError.class , () -> itr.hasNext());
     }

    /**
     * Test if the next() method throws an exception when there are no messages
     * present.
     *
     * @throws IOException the io exception that can be encountered when opening a SocketChannel
     */
    @Test
     public void testIteratorNextException() throws IOException{
         SocketChannel channel = SocketChannel.open();
         NetworkConnection conn = new NetworkConnection(channel);
         Iterator<Message> itr = conn.iterator();
         assertThrows(NoSuchElementException.class,()->itr.next());
     }

    /**
     * Test the iteration of the hasNext() method on the network channel established.
     * Since an actual network connection cannot be emulated, this method injects the
     * expected message into the buffer field (buff) of the NetworkConnection class and
     * reads and iterates over that buffer.
     * NOTE: This test makes heavy use of the mocking and reflection.Field methods to emulate
     * a connected channel and put data into the buffer.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testIterateChannelContent() throws IOException,NoSuchFieldException,IllegalAccessException{
         ByteBuffer myBuff = ByteBuffer.allocate(64*1024);
         SocketChannel channel = SocketChannel.open();
         NetworkConnection connection = new NetworkConnection(channel);
         Iterator<Message> itr = connection.iterator();
         Selector mockedSelector = mock(Selector.class);
         when(mockedSelector.selectNow()).thenReturn(1);
         Field s = NetworkConnection.class.getDeclaredField("selector");
         s.setAccessible(true);
         s.set(connection,mockedSelector);
         SelectionKey mockedKey = mock(SelectionKey.class);
         doReturn(1).when(mockedKey).readyOps();
         Field k = NetworkConnection.class.getDeclaredField("key");
         k.setAccessible(true);
         k.set(connection,mockedKey);
         myBuff.put("HLO 2 -- 4 TestBCT 2 -- 5 Hello".getBytes());
         Field b = NetworkConnection.class.getDeclaredField("buff");
         b.setAccessible(true);
         b.set(connection,myBuff);
         SocketChannel mockedChannel = mock(SocketChannel.class);
         when(mockedChannel.read((ByteBuffer)b.get(connection))).thenReturn(64);
         Field c = NetworkConnection.class.getDeclaredField("channel");
         c.setAccessible(true);
         c.set(connection,mockedChannel);
        assertTrue(itr.hasNext());
     }

    /**
     * Test sendMessage() method to send an empty message over the mocked network.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testSendMessageConditionSendEmptyMessage() throws IOException,NoSuchFieldException,IllegalAccessException{
        Message msg = Message.makeHelloMessage(null);
        SocketChannel channel = SocketChannel.open();
        NetworkConnection conn = new NetworkConnection(channel);
        Field f = NetworkConnection.class.getDeclaredField("channel");
        f.setAccessible(true);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        f.set(conn,mockedChannel);
        when(mockedChannel.write(ByteBuffer.allocate(10))).thenReturn(0);
        assertFalse(conn.sendMessage(msg));
     }

    /**
     * Test hasNext() method of the iterator when there is no message
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testIterateChannelZero() throws IOException,NoSuchFieldException,IllegalAccessException{
        ByteBuffer myBuff = ByteBuffer.allocate(64*1024);
        SocketChannel channel = SocketChannel.open();
        NetworkConnection connection = new NetworkConnection(channel);
        Iterator<Message> itr = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);
        Field s = NetworkConnection.class.getDeclaredField("selector");
        s.setAccessible(true);
        s.set(connection,mockedSelector);
        SelectionKey mockedKey = mock(SelectionKey.class);
        doReturn(1).when(mockedKey).readyOps();
        Field k = NetworkConnection.class.getDeclaredField("key");
        k.setAccessible(true);
        k.set(connection,mockedKey);
        myBuff.put("HLO 0 --".getBytes());
        Field b = NetworkConnection.class.getDeclaredField("buff");
        b.setAccessible(true);
        b.set(connection,myBuff);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        when(mockedChannel.read((ByteBuffer)b.get(connection))).thenReturn(64);
        Field c = NetworkConnection.class.getDeclaredField("channel");
        c.setAccessible(true);
        c.set(connection,mockedChannel);
        assertThrows(AssertionError.class,() -> itr.hasNext());
    }

    /**
     * Test next() method and ensure that it retrieves the first message in the
     * ConcurrentLinkedQueue.
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testNextMethod() throws IOException,NoSuchFieldException,IllegalAccessException{
        SocketChannel channel = SocketChannel.open();
        NetworkConnection connection = new NetworkConnection(channel);
        Field f = NetworkConnection.class.getDeclaredField("messages");
        f.setAccessible(true);
        Message m1 = Message.makeHelloMessage("Test");
        Message m2 = Message.makeHelloMessage("Test");
        f.set(connection, new ConcurrentLinkedQueue<>(Arrays.asList(m1, m2)));
        Iterator<Message> itr = connection.iterator();
        assertNotNull(itr.next());
    }

    /**
     * Test iterate channel content condition selector.
     *
     * @throws IOException            the io exception
     * @throws NoSuchFieldException   the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testIterateChannelContentConditionSelector() throws IOException,NoSuchFieldException,IllegalAccessException{
        ByteBuffer myBuff = ByteBuffer.allocate(64*1024);
        SocketChannel channel = SocketChannel.open();
        NetworkConnection connection = new NetworkConnection(channel);
        Iterator<Message> itr = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(0);
        Field s = NetworkConnection.class.getDeclaredField("selector");
        s.setAccessible(true);
        s.set(connection,mockedSelector);
        SelectionKey mockedKey = mock(SelectionKey.class);
        doReturn(1).when(mockedKey).readyOps();
        Field k = NetworkConnection.class.getDeclaredField("key");
        k.setAccessible(true);
        k.set(connection,mockedKey);
        myBuff.put("HLO 2 -- 4 TestBCT 2 -- 5 Hello".getBytes());
        Field b = NetworkConnection.class.getDeclaredField("buff");
        b.setAccessible(true);
        b.set(connection,myBuff);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        when(mockedChannel.read((ByteBuffer)b.get(connection))).thenReturn(64);
        Field c = NetworkConnection.class.getDeclaredField("channel");
        c.setAccessible(true);
        c.set(connection,mockedChannel);
        assertFalse(itr.hasNext());
    }

    /**
     * Test hasNext() method to cover conditions for selector.selectNow() branch
     *
     * @throws IOException            the io exception that can be encountered when opening a SocketChannel
     * @throws NoSuchFieldException   the no such field exception to be used while using java Reflection
     * @throws IllegalAccessException the illegal access exception to be used while using java Reflection
     */
    @Test
    public void testHasNextConditionOnSelectedKey() throws IOException,NoSuchFieldException,IllegalAccessException{
        ByteBuffer myBuff = ByteBuffer.allocate(64*1024);
        SocketChannel channel = SocketChannel.open();
        NetworkConnection connection = new NetworkConnection(channel);
        Iterator<Message> itr = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);
        Field s = NetworkConnection.class.getDeclaredField("selector");
        s.setAccessible(true);
        s.set(connection,mockedSelector);
        SelectionKey mockedKey = mock(SelectionKey.class);
        doReturn(0).when(mockedKey).readyOps();
        Field k = NetworkConnection.class.getDeclaredField("key");
        k.setAccessible(true);
        k.set(connection,mockedKey);
        myBuff.put("HLO 2 -- 4 TestBCT 2 -- 5 Hello".getBytes());
        Field b = NetworkConnection.class.getDeclaredField("buff");
        b.setAccessible(true);
        b.set(connection,myBuff);
        SocketChannel mockedChannel = mock(SocketChannel.class);
        when(mockedChannel.read((ByteBuffer)b.get(connection))).thenReturn(64);
        Field c = NetworkConnection.class.getDeclaredField("channel");
        c.setAccessible(true);
        c.set(connection,mockedChannel);
        assertThrows(AssertionError.class,() -> itr.hasNext());
    }

}

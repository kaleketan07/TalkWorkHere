/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * This class contains the test suite for the enum MessageType under the
 * im package. The tests validate all the string representations of the
 * enum objects.
 *
 * @author Sachin Haldavanekar
 * @version 1.0
 */
public class TestMessageType {
    /**
     * Test to check if a the MessageType enum returns the correct string
     * for type Hello.
     */
    @Test
    public void testMessageTypeEnumHello() {
        assertEquals(HLO, MessageType.HELLO.toString());
    }

    /**
     * Test to check if a the MessageType enum returns the correct string
     * for type Quit.
     */
    @Test
    public void testMessageTypeEnumQuit() {
        assertEquals(BYE, MessageType.QUIT.toString());
    }

    /**
     * Test to check if a the MessageType enum returns the correct string
     * for type Broadcast.
     */
    @Test
    public void testMessageTypeEnumNoBroadcast() {
        assertEquals(BCT, MessageType.BROADCAST.toString());
    }

    /**
     * CONSTANTS to be used as expected values or method arguments
     **/
    private static final String HLO = "HLO";
    private static final String BYE = "BYE";
    private static final String BCT = "BCT";
}

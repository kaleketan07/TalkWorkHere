/*
 ***************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ***************************************************************************************
 */

package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;

/**
 * TestClientTimer class contains the test suite tests the ClientTimer.java class under
 * the im.server package. It tests all the different time instances after and before initialization and
 * activity
 *
 * @author Team 201 - rahul
 */
public class TestClientTimer {

    /**
     * Test for the constructor of Class ClientTimer
     */
    @Test
    public void testClientTimerConstructor() {

        ClientTimer ct = new ClientTimer();
        assertTrue(ClientTimer.class.isInstance(ct));
    }

    /**
     * Test to check the time update after initialization function
     *
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Test
    public void testUpdateAfterInitialization() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ClientTimer ct = new ClientTimer();
        Field privateStringField = ClientTimer.class.
                getDeclaredField("calendar");
        privateStringField.setAccessible(true);
        GregorianCalendar calendar = (GregorianCalendar) privateStringField.get(ct);
        long timeAfterActivity = calendar.getTimeInMillis();
        ct.updateAfterInitialization();

        assertTrue(Math.abs(calendar.getTimeInMillis() - timeAfterActivity) < 100);
        assertTrue(ClientTimer.class.isInstance(ct));

    }


    /**
     * Test to check the time update after activity
     *
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Test
    public void testupdateAfterActivity() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ClientTimer ct = new ClientTimer();

        Field privateStringField = ClientTimer.class.
                getDeclaredField("calendar");

        privateStringField.setAccessible(true);
        GregorianCalendar calendar = (GregorianCalendar) privateStringField.get(ct);
        long timeAfterActivity = calendar.getTimeInMillis() + 17400000;
        ct.updateAfterActivity();
        assertTrue(Math.abs(calendar.getTimeInMillis() - timeAfterActivity) < 20);
        assertTrue(ClientTimer.class.isInstance(ct));

    }

    /**
     * Test whether the calendar represents a time before the current time.
     */
    @Test
    public void testBehind() {
        ClientTimer ct = new ClientTimer();
        ct.updateAfterInitialization();
        assertTrue(ClientTimer.class.isInstance(ct));
        assertEquals(ct.isBehind(), false);


    }
}

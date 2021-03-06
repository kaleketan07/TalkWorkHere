/*
 ***************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ***************************************************************************************
 */

package edu.northeastern.ccs.im.db;

import java.sql.SQLNonTransientConnectionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestDBConnection {
    /**
     * Test if the constructor creates the DBConnection object
     *
     * @throws SQLException - Thrown if there is an exception while connecting to MySQL
     * @throws IOException  - when config file is not found
     */
    @Test
    public void testConstructor() throws SQLException, IOException {
        IDBConnection dbConn = new DBConnection();
        Assertions.assertTrue(dbConn instanceof DBConnection);
    }

    /**
     * Test if the connection gets closed. Should throw exception when connection is used
     * after closing
     *
     * @throws SQLException - Thrown if there is an exception while connecting to MySQL
     * @throws IOException  - when config file is not found
     */
    @Test
    public void testClose() throws SQLException, IOException {
        IDBConnection dbConn = new DBConnection();
        dbConn.close();
        String sql = "select * from test";
        Assertions.assertThrows(SQLNonTransientConnectionException.class, () -> dbConn.getPreparedStatement(sql));
    }

    /**
     * Test the getPreparedStatement method to retrieve a preparedStatement
     *
     * @throws NoSuchFieldException   - When the given field is not found DBConnection
     * @throws SQLException           - When error occurs during mysql connection
     * @throws IllegalAccessException - when illegally changing an objects access modifier.
     * @throws IOException            - when config file is not found
     */
    @Test
    public void testGetPreparedStatement() throws NoSuchFieldException, SQLException, IllegalAccessException, IOException {
        Connection mockedConn = Mockito.mock(Connection.class);
        IDBConnection dbConn = new DBConnection();
        Field conn = DBConnection.class.getDeclaredField("connection");
        conn.setAccessible(true);
        conn.set(dbConn, mockedConn);
        PreparedStatement mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(mockedConn.prepareStatement(Mockito.anyString())).thenReturn(mockedPreparedStatement);
        Assertions.assertEquals(mockedPreparedStatement, dbConn.getPreparedStatement(""));
    }
}

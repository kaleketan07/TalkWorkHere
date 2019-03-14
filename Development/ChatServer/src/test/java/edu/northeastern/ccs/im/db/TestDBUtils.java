package edu.northeastern.ccs.im.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.mockito.Mockito.doNothing;

/**
 * The type Test db utils.
 *
 * @author Kunal
 */
public class TestDBUtils {


    /**
     * The Mocked prepared statement.
     */
    @Mock
    PreparedStatement mockedPreparedStatement;

    /**
     * Initiate Mock
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tear down.
     */
    @AfterEach
    public void tearDown() {
        mockedPreparedStatement = null;
    }

    /**
     * Test set prepared statement args.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testSetPreparedStatementArgs() throws SQLException {
        DBUtils db = new DBUtils();
        doNothing().when(mockedPreparedStatement).setInt(1, 1);
        doNothing().when(mockedPreparedStatement).setString(2, "AB");
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement, 1, "AB");
        Assertions.assertNotNull(ps);
    }


    /**
     * Test set prepared statement args to cover conditions for condition coverage.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testSetPreparedStatementArgsConditions() throws SQLException {
        DBUtils db = new DBUtils();
        doNothing().when(mockedPreparedStatement).setString(1, "XV");
        doNothing().when(mockedPreparedStatement).setDouble(2, 45.56);
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement, "XV", 45.56);
        Assertions.assertNotNull(ps);
    }

    /**
     * Test set prepared statement args to cover conditions for condition coverage
     * with settimeStamp.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testSetPreparedStatementArgsConditionswithTimeStamp() throws SQLException {
        DBUtils db = new DBUtils();
        doNothing().when(mockedPreparedStatement).setTimestamp(1, Timestamp.valueOf("1966-08-30 08:08:08"));
        doNothing().when(mockedPreparedStatement).setDouble(2, 45.56);
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement, Timestamp.valueOf("1966-08-30 08:08:08"), 45.56);
        Assertions.assertNotNull(ps);
    }

    /**
     * Test set prepared statement args to cover conditions for condition coverage
     * with setObject.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testSetPreparedStatementArgsForNulls() throws SQLException {
        DBUtils db = new DBUtils();
        doNothing().when(mockedPreparedStatement).setString(1, null);
        doNothing().when(mockedPreparedStatement).setString(2, null);
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement, null, null);
        Assertions.assertNotNull(ps);
    }

    /**
     * Test set prepared statement args to cover conditions for condition coverage
     * with setBoolean.
     *
     * @throws SQLException the sql exception
     */
    @Test
    public void testSetPreparedStatementArgsForBoolean() throws SQLException {
        DBUtils db = new DBUtils();
        doNothing().when(mockedPreparedStatement).setBoolean(1, false);
        doNothing().when(mockedPreparedStatement).setBoolean(2, true);
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement, false, true);
        Assertions.assertNotNull(ps);
    }
}

package edu.northeastern.ccs.im.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.mockito.Mockito.doNothing;

/**
 * The type Test db utils.
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
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tear down.
     */
    @AfterEach
    public void tearDown(){
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
        doNothing().when(mockedPreparedStatement).setInt(1,1);
        doNothing().when(mockedPreparedStatement).setString(2,"AB");
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement,1,"AB");
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
        doNothing().when(mockedPreparedStatement).setString(1,"XV");
        doNothing().when(mockedPreparedStatement).setDouble(2,45.56);
        PreparedStatement ps = db.setPreparedStatementArgs(mockedPreparedStatement,"XV",45.56);
        Assertions.assertNotNull(ps);
    }

}

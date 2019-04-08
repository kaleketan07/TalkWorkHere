package edu.northeastern.ccs.im.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The type Db Utils. This class is used for DB related helper functions
 *
 * @author Kunal
 */
public class DBUtils {

    /**
     * Sets prepared statement arguments. This is used to prepare any preparedStatement with
     * any no. of arguments, this replaces the tedious use of preparedStmt.set() methods.
     *
     * @param stmt                  the PreparedStatement to set
     * @param args                  the arguments that need to be set in the statement
     * @return PreparedStatement    the prepared statement with its arguments set according to the parameter data type
     * @throws SQLException         the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    public PreparedStatement setPreparedStatementArgs(PreparedStatement stmt, Object... args) throws SQLException {
        int i = 1;
        for (Object arg : args) {
            if (arg instanceof String)
                stmt.setString(i++, (String) arg);
            else if (arg instanceof Integer)
                stmt.setInt(i++, (int) arg);
            else if (arg instanceof Timestamp)
                stmt.setTimestamp(i++, (Timestamp) arg);
            else if (arg instanceof Boolean)
                stmt.setBoolean(i++, (boolean) arg);
            else if (arg == null)
                stmt.setObject(i++, null);
        }
        return stmt;
    }
}

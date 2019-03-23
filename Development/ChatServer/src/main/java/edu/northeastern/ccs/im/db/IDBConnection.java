package edu.northeastern.ccs.im.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public interface IDBConnection {

    /**
     * Closes the IDBConnection object
     * @throws SQLException - when an error occurs during database interaction
     */
    public void close() throws SQLException;

    /**
     * Get a prepared statement using the query provided.
     *
     * @param sqlQuery - Query associated with the prepared statement.
     * @return - the prepared statement generated
     * @throws SQLException - when an error occurs during database interaction
     */
    public PreparedStatement getPreparedStatement(String sqlQuery) throws SQLException;

}

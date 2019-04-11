/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public interface IDBConnection {

    /**
     * Closes the existing connection
     *
     * @throws SQLException  the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    void close() throws SQLException;

    /**
     * Create a PreparedStatement on the given connection.
     *
     * @param sqlQuery              Query to be executed
     * @return PreparedStatement    the prepared statement used in executing database queries
     * @throws SQLException         the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    PreparedStatement getPreparedStatement(String sqlQuery) throws SQLException;

    /**
     * @return the instance of queryProperties
     */
	Properties getQueryProperties();

}

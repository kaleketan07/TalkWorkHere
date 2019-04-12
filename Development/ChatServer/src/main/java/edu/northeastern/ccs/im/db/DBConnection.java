/****************************************************************************************
 * Copyright (c) 2019 Team 201 - Ketan Kale, Kunal Patil, Rahul Bhat, Sachin Haldavanekar.
 * All rights reserved.
 ****************************************************************************************/

package edu.northeastern.ccs.im.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection implements IDBConnection {

    /**
     * Constructor to create a new Database connection
     *
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     * @throws IOException  the io exception that can be thrown
     */
    public DBConnection() throws SQLException, IOException {
        Properties properties = new Properties();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream input = cl.getResourceAsStream("rdsConfig.properties");
        properties.load(input);
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");
        connection = DriverManager.getConnection(url, username, password);
        queryProperties = new Properties();
        input = cl.getResourceAsStream("queryConfig.properties");
        queryProperties.load(input);
    }

    /**
     * Closes the existing connection
     *
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Create a PreparedStatement on the given connection.
     *
     * @param sqlQuery Query to be executed
     * @return PreparedStatement    the prepared statement used in executing database queries
     * @throws SQLException the sql exception thrown in case of an error with jdbc's interaction with the data source
     */
    @Override
    public PreparedStatement getPreparedStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }

    /**
     * @return the instance of queryProperties
     */
    @Override
    public Properties getQueryProperties() {
        return queryProperties;
    }

    private Connection connection;
    private Properties queryProperties;


}

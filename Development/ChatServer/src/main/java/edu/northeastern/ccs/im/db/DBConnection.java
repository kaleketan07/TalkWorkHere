package edu.northeastern.ccs.im.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    /**
     * getDBConnection - Method to create a new Database connection and return the
     *
     * @return the connection instance created using the constants URL, Username and Password
     */
    public DBConnection() throws ClassNotFoundException, SQLException, IOException {
        Properties properties = new Properties();
        InputStream input = new FileInputStream("src/main/resources/rdsConfig.properties");
        properties.load(input);
        String url = properties.getProperty("jdbc.url");
        String driver = properties.getProperty("jdbc.driver");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
    }

    /**
     * Closes the existing connection
     *
     * @throws SQLException - when an error occurs while closing the connection
     */
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Create a PreparedStatement on the given connection.
     *
     * @param sqlQuery - Query to be executed
     * @return the PreparedStatement
     * @throws SQLException - when an error occurs while generating create statement on the connection
     */
    public PreparedStatement getPreparedStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }

    private Connection connection;
}

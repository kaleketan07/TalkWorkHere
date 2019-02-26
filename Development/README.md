# CS5500-PrattleServer
Prattle is a network server that communicates with IM clients that connect to it.  The capabilities of the server include:
* spawning a new thread to handle each client that connects to it
* broadcast messages to all other on-line clients

This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/. It is based on work originally written by Matthew Hertz and has been adapted for use in a class assignment at Northeastern University.

Version 1.3


# Configuration

Make sure that the following steps are completed before the server is run

1. Setup local configuration for Database connection
	1.1 Create a localConfig.properties file under "src/main/resources" directory
	1.2 Add the following properties:
    ``` properties
    jdbc.url = jdbc:mysql://localhost:3306/<your-db-name>
    jdbc.driver = com.mysql.jdbc.Driver
	jdbc.username = <your-mysql-username>
	jdbc.password = <your-mysql-password>
    ``` 
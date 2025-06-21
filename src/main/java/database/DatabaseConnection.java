package database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Class for connecting to the database
 *
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Connects to the database
     *
     */
    public Connection connectToDatabase() {
        Properties props = new Properties();
        try(FileReader reader = new FileReader("database.properties")) {
            props.load(reader);

            return DriverManager.getConnection(
                    props.getProperty("databaseUrl"),
                    props.getProperty("username"),
                    props.getProperty("password"));

        } catch (SQLException | IOException e) {
            log.error("Database, IO error: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Disconnects to the database
     *
     */
    public void closeConnection(Connection connection)  {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Database error: {}", e.getMessage(), e);
        }
    }
}

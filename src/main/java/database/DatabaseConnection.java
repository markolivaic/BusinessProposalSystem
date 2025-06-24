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
 * Pomoćna klasa za upravljanje konekcijom s bazom podataka.
 * Odgovorna je za uspostavljanje i zatvaranje konekcije.
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Uspostavlja konekciju s bazom podataka.
     * Podaci za spajanje (URL, korisničko ime, lozinka) čitaju se iz datoteke `database.properties`.
     *
     * @return Objekt {@link Connection} koji predstavlja aktivnu konekciju, ili {@code null} ako spajanje ne uspije.
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
            log.error("Database connection or I/O error: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Zatvara proslijeđenu konekciju s bazom podataka.
     * Ako dođe do greške prilikom zatvaranja, ona se logira.
     *
     * @param connection Konekcija koju treba zatvoriti.
     */
    public void closeConnection(Connection connection)  {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Database error while closing connection: {}", e.getMessage(), e);
        }
    }
}
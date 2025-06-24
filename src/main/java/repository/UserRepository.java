package repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import database.DatabaseConnection;
import enums.UserRole;
import exception.RepositoryAccessException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Repozitorij za upravljanje korisničkim podacima.
 * Čita podatke iz tekstualne datoteke za autentifikaciju i inicijalno puni bazu podataka.
 */
public class UserRepository {

    private static final String USERS_FILE_PATH = "dat/users.txt";
    private static final String IO_ERROR = "IO error: {}";
    private static final int NUMBER_OF_ROWS_PER_USER = 4;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    /**
     * Autentificira korisnika provjerom korisničkog imena i lozinke.
     * Čita podatke iz tekstualne datoteke i uspoređuje hashiranu lozinku.
     *
     * @param username Korisničko ime za provjeru.
     * @param password Lozinka za provjeru.
     * @return {@link Pair} koji sadrži ID korisnika i boolean vrijednost (isAdmin), ili {@code null} ako autentifikacija ne uspije.
     * @throws RepositoryAccessException ako dođe do greške pri čitanju datoteke.
     */
    public Pair<Long, Boolean> authenticateUser(String username, String password) {
        try (Stream<String> stream = Files.lines(Path.of(USERS_FILE_PATH))) {
            List<String> fileRows = new ArrayList<>(stream.toList());

            for (int recordNumber = 0; recordNumber < (fileRows.size() / NUMBER_OF_ROWS_PER_USER); recordNumber++) {
                Long id = Long.parseLong(fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER));
                String storedUsername = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 1);
                String storedHash = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 2);
                String roleString = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 3);

                if (storedUsername.equals(username)) {
                    BCrypt.Result verificationResult = BCrypt.verifyer().verify(password.toCharArray(), storedHash);

                    if (verificationResult.verified) {
                        boolean isAdmin = UserRole.valueOf(roleString) == UserRole.ADMIN;
                        return new Pair<>(id, isAdmin);
                    } else {
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException("Error reading users file.", e);
        }

        return null;
    }

    /**
     * Pronalazi korisnika po korisničkom imenu.
     *
     * @param username Korisničko ime koje se traži.
     * @return {@link Pair} koji sadrži ID korisnika i boolean vrijednost (isAdmin), ili {@code null} ako korisnik nije pronađen.
     * @throws RepositoryAccessException ako dođe do greške pri čitanju datoteke.
     */
    public Pair<Long, Boolean> findByUsername(String username) {
        try {
            List<String> fileRows = Files.readAllLines(Path.of(USERS_FILE_PATH));

            for (int recordNumber = 0; recordNumber < (fileRows.size() / NUMBER_OF_ROWS_PER_USER); recordNumber++) {
                Long id = Long.parseLong(fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER));
                String storedUsername = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 1);
                boolean isAdmin = UserRole.valueOf(fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 3)) == UserRole.ADMIN;

                if (storedUsername.equalsIgnoreCase(username)) {
                    return new Pair<>(id, isAdmin);
                }
            }
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException("Error reading users file", e);
        }
        return null;
    }

    /**
     * Uvozi korisnike iz tekstualne datoteke u bazu podataka.
     * Ova metoda se poziva pri pokretanju aplikacije kako bi osigurala da su korisnici prisutni u bazi.
     * Ako korisnik već postoji u bazi, preskače ga.
     *
     * @throws RepositoryAccessException ako dođe do greške pri čitanju datoteke ili pristupu bazi.
     */
    public void importUsersFromFile() {
        try {
            List<String> fileRows = Files.readAllLines(Path.of(USERS_FILE_PATH));
            for (int recordNumber = 0; recordNumber < (fileRows.size() / NUMBER_OF_ROWS_PER_USER); recordNumber++) {
                Long id = Long.parseLong(fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER));
                String username = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 1);
                String hashedPassword = fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 2);
                UserRole role = UserRole.valueOf(fileRows.get(recordNumber * NUMBER_OF_ROWS_PER_USER + 3));

                save(new User.Builder(id)
                        .withUsername(username)
                        .withHashedPassword(hashedPassword)
                        .withIsAdmin(role == UserRole.ADMIN)
                        .build());

            }
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException("Error reading users file", e);
        }
    }

    /**
     * Sprema korisnika u bazu podataka.
     * Prije spremanja provjerava postoji li već korisnik s istim korisničkim imenom.
     *
     * @param user Korisnik koji se sprema.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public void save(User user) {
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement checkStmt = connection.prepareStatement("SELECT id FROM USERS WHERE username = ?");
             PreparedStatement insertStmt = connection.prepareStatement(
                     "INSERT INTO USERS (id, username, hashed_password, role) VALUES (?, ?, ?, ?)")) {

            checkStmt.setString(1, user.getUsername());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                return;
            }

            insertStmt.setLong(1, user.getId());
            insertStmt.setString(2, user.getUsername());
            insertStmt.setString(3, user.getHashedPassword());
            insertStmt.setString(4, user.isAdmin() ? "ADMIN" : "REGULAR_USER");

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Database error while saving user: {}", e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
}
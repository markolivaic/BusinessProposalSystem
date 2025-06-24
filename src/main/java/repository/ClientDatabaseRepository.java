package repository;

import database.DatabaseConnection;
import exception.EmptyRepositoryResultException;
import exception.RepositoryAccessException;
import model.AuditLog;
import model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za upravljanje podacima o klijentima ({@link Client}) u bazi podataka.
 * Nasljeđuje {@link AbstractRepository} i implementira specifične metode za rad s klijentima.
 *
 * @param <T> Tip klijenta, mora nasljeđivati {@link Client}.
 */
public class ClientDatabaseRepository<T extends Client> extends AbstractRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(ClientDatabaseRepository.class);
    private static final String DATABASE_ERROR = "Database error: {}";

    /**
     * {@inheritDoc}
     */
    @Override
    public T findById(Long id) throws EmptyRepositoryResultException, SQLException {
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name, email, phone, company FROM CLIENTS WHERE id = ?"))
        {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return (T) extractClientFromResultSet(resultSet);
            }
            else{
                String errorMessage = "Client with id " + id + " not found";
                log.warn(errorMessage);
                throw new EmptyRepositoryResultException(errorMessage);
            }
        }
        catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll() throws RepositoryAccessException {
        List<T> clients = new ArrayList<>();
        String query = "SELECT id, name, email, phone, company FROM CLIENTS";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Client client = extractClientFromResultSet(resultSet);
                clients.add((T) client);
            }

        }
        catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }

        return clients;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(List<T> entities) throws RepositoryAccessException, SQLException {
        try(Connection connection = new DatabaseConnection().connectToDatabase();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO CLIENTS(NAME, EMAIL, PHONE, COMPANY) VALUES (?, ?, ?, ?)"))
        {
            for (T entity : entities) {
                statement.setString(1, entity.getName());
                statement.setString(2, entity.getEmail());
                statement.setString(3, entity.getPhone());
                statement.setString(4, entity.getCompany());
                statement.addBatch();
            }

            int[] affectedRows = statement.executeBatch();
            if (affectedRows.length == 0) {
                throw new RepositoryAccessException("No rows affected");
            }
        }
        catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }

    /**
     * Dohvaća sljedeći dostupan ID za zapis u audit logu.
     * @return Sljedeći ID.
     */
    private Long getNextAuditLogId() {
        List<AuditLog> logs = new AuditLogRepository().readAuditLogs();
        return logs.isEmpty() ? 1L : logs.get(logs.size() - 1).id() + 1;
    }

    /**
     * Sprema jednog klijenta u bazu podataka i asinkrono zapisuje promjenu u audit log.
     * @param entity Klijent koji se sprema.
     * @throws RepositoryAccessException ako spremanje u bazu ne uspije.
     */
    @Override
    public void save(T entity) {
        List<T> clients = new ArrayList<>();
        clients.add(entity);
        try {
            save(clients);
        } catch (SQLException e) {
            throw new RepositoryAccessException(e);
        }

        AuditLog logEntry = new AuditLog(
                getNextAuditLogId(),
                SessionManager.getLoggedInUserId(),
                SessionManager.isAdmin() ? "Admin" : "User",
                "ADD",
                "Client",
                "N/A",
                entity.toString(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        AuditLogRepository auditLogRepository = new AuditLogRepository();
        new Thread(() -> auditLogRepository.logChange(logEntry)).start();
    }


    /**
     * Pomoćna metoda za kreiranje {@link Client} objekta iz {@link ResultSet}-a.
     * @param resultSet ResultSet iz kojeg se čitaju podaci.
     * @return Kreirani Client objekt.
     * @throws SQLException ako dođe do greške pri čitanju stupaca.
     */
    private static Client extractClientFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        String company = resultSet.getString("company");

        return new Client(id, name, email, phone, company);
    }
}
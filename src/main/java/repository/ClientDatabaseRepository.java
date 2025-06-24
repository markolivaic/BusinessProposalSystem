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

public class ClientDatabaseRepository<T extends Client> extends AbstractRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(ClientDatabaseRepository.class);
    private static final String DATABASE_ERROR = "Database error: {}";

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
                throw new EmptyRepositoryResultException("Client with id " + id + " not found");
            }
        }
        catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e.getMessage(), e);
        }
    }

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

    @Override
    public void save(List<T> entities) throws RepositoryAccessException {
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

    private Long getNextAuditLogId() {
        // Ovdje koristimo novu, sinkroniziranu metodu za čitanje
        List<AuditLog> logs = new AuditLogRepository().readAuditLogs();
        return logs.isEmpty() ? 1L : logs.get(logs.size() - 1).id() + 1;
    }

    @Override
    public void save(T entity) {
        List<T> clients = new ArrayList<>();
        clients.add(entity);
        save(clients);

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

        // IZMJENA: Pozivamo logiranje u novoj, pozadinskoj niti da ne blokiramo UI.
        AuditLogRepository auditLogRepository = new AuditLogRepository();
        new Thread(() -> auditLogRepository.logChange(logEntry)).start();
    }


    private static Client extractClientFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        String company = resultSet.getString("company");

        return new Client(id, name, email, phone, company);
    }
}
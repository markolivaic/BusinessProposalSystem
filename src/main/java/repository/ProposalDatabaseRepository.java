package repository;


import database.DatabaseConnection;
import exception.EmptyRepositoryResultException;
import exception.RepositoryAccessException;
import model.AuditLog;
import model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ProposalDatabaseRepository<T extends Proposal> extends AbstractRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(ProposalDatabaseRepository.class);
    private static final String DATABASE_ERROR = "Database error: {}";
    private static final String DATE_PATTERN =  "yyyy-MM-dd HH:mm:ss";
    private static final String PROPOSAL = "Proposal";
    private static final String ADMIN = "Admin";

    @Override
    public T findById(Long id) throws EmptyRepositoryResultException, SQLException {
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, title, description, status, client_id, user_id FROM proposals WHERE id = ?")) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return (T) extractProposalFromResultSet(resultSet);
            } else {
                throw new EmptyRepositoryResultException("Proposal with id " + id + " not found");
            }
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
    @Override
    public List<T> findAll() throws RepositoryAccessException {
        List<T> proposals = new ArrayList<>();
        String query = "SELECT id, title, description, status, client_id, user_id FROM proposals";
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Proposal proposal = extractProposalFromResultSet(resultSet);
                proposals.add((T) proposal);
            }
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
        return proposals;
    }
    @Override
    public void save(List<T> entities) throws RepositoryAccessException {
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO proposals (title, description, status, client_id, user_id) VALUES (?, ?, ?, ?, ?)")) {
            for (T entity : entities) {
                statement.setString(1, entity.getTitle());
                statement.setString(2, entity.getDescription());
                statement.setString(3, entity.getStatus().toString());
                statement.setLong(4, entity.getClientId());
                statement.setLong(5, entity.getUserId());
                statement.addBatch();
            }
            int[] affectedRows = statement.executeBatch();
            if (affectedRows.length == 0) {
                throw new RepositoryAccessException("No rows affected while saving proposals.");
            }
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
    private Long getNextAuditLogId() {
        List<AuditLog> logs = new AuditLogRepository().readAuditLogs();
        return logs.isEmpty() ? 1L : logs.get(logs.size() - 1).id() + 1;
    }

    @Override
    public void save(T entity) {
        List<T> proposals = new ArrayList<>();
        proposals.add(entity);
        save(proposals);
        AuditLog logEntry = new AuditLog(
                getNextAuditLogId(),
                SessionManager.getLoggedInUserId(),
                SessionManager.isAdmin() ? ADMIN : "User",
                "ADD",
                PROPOSAL,
                "N/A",
                entity.toString(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        );
        new AuditLogRepository().logChangeAsync(logEntry);
    }
    public void update(Proposal proposal) {
        String query = "UPDATE proposals SET title = ?, description = ?, client_id = ? WHERE id = ?";
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {
            Proposal oldProposal = findById(proposal.getId()); // Dohvati staru verziju
            statement.setString(1, proposal.getTitle());
            statement.setString(2, proposal.getDescription());
            statement.setLong(3, proposal.getClientId());
            statement.setLong(4, proposal.getId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryAccessException("No rows updated. Proposal ID might be incorrect.");
            }
            AuditLog logEntry = new AuditLog(
                    getNextAuditLogId(),
                    SessionManager.getLoggedInUserId(),
                    SessionManager.isAdmin() ? ADMIN : "User",
                    "UPDATE",
                    PROPOSAL,
                    oldProposal.toString(),
                    proposal.toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            );
            new AuditLogRepository().logChangeAsync(logEntry);
        } catch (SQLException | EmptyRepositoryResultException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
    private static Proposal extractProposalFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        String status = resultSet.getString("status");
        Long clientId = resultSet.getLong("client_id");
        Long userId = resultSet.getLong("user_id");
        return new Proposal.Builder(id)
                .withTitle(title)
                .withDescription(description)
                .withStatus(Enum.valueOf(enums.ProposalStatus.class, status))
                .withClientId(clientId)
                .withUserId(userId)
                .build();
    }
    public Long getNextProposalId() {
        String query = "SELECT MAX(id) FROM PROPOSALS";
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                Long maxId = resultSet.getLong(1);
                return (maxId != 0) ? maxId + 1 : 1;
            }
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException("Error fetching latest user ID", e);
        }
        return 1L;
    }
    public void deleteProposal(Long proposalId) throws SQLException, EmptyRepositoryResultException {
        Proposal oldProposal = findById(proposalId);
        String query = "DELETE FROM proposals WHERE id = ?";
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, proposalId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryAccessException("Proposal ID not found or already deleted.");
            }
            AuditLog logEntry = new AuditLog(
                    getNextAuditLogId(),
                    SessionManager.getLoggedInUserId(),
                    SessionManager.isAdmin() ? ADMIN : "User",
                    "DELETE",
                    PROPOSAL,
                    oldProposal.toString(),
                    "Deleted",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            );
            new AuditLogRepository().logChangeAsync(logEntry);
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
    public void updateStatus(Long proposalId, enums.ProposalStatus newStatus) throws SQLException, EmptyRepositoryResultException {
        Proposal oldProposal = findById(proposalId);
        String query = "UPDATE proposals SET status = ? WHERE id = ?";
        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newStatus.toString());
            statement.setLong(2, proposalId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryAccessException("No rows updated. Proposal ID might be incorrect.");
            }
            Proposal updatedProposal = findById(proposalId);
            AuditLog logEntry = new AuditLog(
                    getNextAuditLogId(),
                    SessionManager.getLoggedInUserId(),
                    SessionManager.isAdmin() ? ADMIN : "User",
                    "UPDATE STATUS",
                    PROPOSAL,
                    oldProposal.getStatus().toString(),
                    updatedProposal.getStatus().toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            );
            new AuditLogRepository().logChangeAsync(logEntry);
        } catch (SQLException | EmptyRepositoryResultException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
}

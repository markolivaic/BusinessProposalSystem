package repository;


import database.DatabaseConnection;
import exception.EmptyRepositoryResultException;
import exception.RepositoryAccessException;
import model.AuditLog;
import model.Client;
import model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Repository class for handling database operations for Proposal entities.
 * This class provides methods to find, save, update, and delete proposals
 * from the H2 database. It also handles logging of all major operations.
 *
 * @param <T> A type that extends Proposal.
 */
public class ProposalDatabaseRepository<T extends Proposal> extends AbstractRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(ProposalDatabaseRepository.class);

    private static final String DATABASE_ERROR = "Database error: {}";
    private static final String DATE_PATTERN =  "yyyy-MM-dd HH:mm:ss";
    private static final String PROPOSAL = "Proposal";
    private static final String ADMIN = "Admin";

    /**
     * Finds a single Proposal entity by its unique identifier.
     *
     * @param id The ID of the proposal to find.
     * @return The found Proposal object.
     * @throws EmptyRepositoryResultException if no proposal with the given ID is found.
     * @throws RepositoryAccessException if a database access error occurs.
     */
    @Override
    public T findById(Long id) throws EmptyRepositoryResultException, SQLException {
        String sql = "SELECT id, title, description, status, client_id, user_id FROM proposals WHERE id = ?";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return (T) extractProposalFromResultSet(resultSet);
            } else {
                String errorMessage = "Proposal with id " + id + " not found";
                log.warn(errorMessage);
                throw new EmptyRepositoryResultException(errorMessage);
            }
        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }

    /**
     * Retrieves all Proposal entities from the database.
     *
     * @return A list of all proposals.
     * @throws RepositoryAccessException if a database access error occurs.
     */
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

    /**
     * Saves a list of Proposal entities to the database using a batch operation.
     * This method is not directly used for single saves but supports bulk inserts.
     *
     * @param entities The list of proposals to save.
     * @throws RepositoryAccessException if the save operation fails.
     */
    @Override
    public void save(List<T> entities) throws RepositoryAccessException {
        String sql = "INSERT INTO proposals (title, description, status, client_id, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

    /**
     * Retrieves the next available ID for an audit log entry by reading the log file.
     *
     * @return The next sequential ID for the audit log.
     */
    private Long getNextAuditLogId() {
        List<AuditLog> logs = new AuditLogRepository().readAuditLogs();
        return logs.isEmpty() ? 1L : logs.get(logs.size() - 1).id() + 1;
    }

    /**
     * Saves a single Proposal entity to the database and logs the action.
     * The logging operation is performed asynchronously in a new thread.
     *
     * @param entity The proposal to save.
     */
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

        AuditLogRepository auditLogRepository = new AuditLogRepository();
        new Thread(() -> auditLogRepository.logChange(logEntry)).start();
    }

    /**
     * Updates an existing proposal's title, description, and client ID in the database.
     * It logs only the fields that were actually changed, showing real names for clients.
     *
     * @param proposal The proposal object with updated information.
     * @throws RepositoryAccessException if the update fails or the proposal is not found.
     */
    public void update(Proposal proposal) {
        String query = "UPDATE proposals SET title = ?, description = ?, client_id = ? WHERE id = ?";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {

            Proposal oldProposal = findById(proposal.getId());

            statement.setString(1, proposal.getTitle());
            statement.setString(2, proposal.getDescription());
            statement.setLong(3, proposal.getClientId());
            statement.setLong(4, proposal.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryAccessException("No rows updated. Proposal ID might be incorrect.");
            }

            StringBuilder oldValueBuilder = new StringBuilder();
            StringBuilder newValueBuilder = new StringBuilder();
            boolean hasChanges = false;

            if (!oldProposal.getTitle().equals(proposal.getTitle())) {
                oldValueBuilder.append("'").append(oldProposal.getTitle()).append("', ");
                newValueBuilder.append("'").append(proposal.getTitle()).append("', ");
                hasChanges = true;
            }

            if (!oldProposal.getDescription().equals(proposal.getDescription())) {
                oldValueBuilder.append("'").append(oldProposal.getDescription()).append("', ");
                newValueBuilder.append("'").append(proposal.getDescription()).append("', ");
                hasChanges = true;
            }

            if (oldProposal.getClientId() != proposal.getClientId()) {
                ClientDatabaseRepository<Client> clientRepository = new ClientDatabaseRepository<>();
                Client oldClient = clientRepository.findById(oldProposal.getClientId());
                Client newClient = clientRepository.findById(proposal.getClientId());

                oldValueBuilder.append("'").append(oldClient.getName()).append("', ");
                newValueBuilder.append("'").append(newClient.getName()).append("', ");
                hasChanges = true;
            }

            if (!hasChanges) {
                log.info("Update called for proposal ID {}, but no changes were detected.", proposal.getId());
                return;
            }

            // Uklanjanje zadnjeg zareza i razmaka za čist ispis
            String finalOldValue = oldValueBuilder.substring(0, oldValueBuilder.length() - 2);
            String finalNewValue = newValueBuilder.substring(0, newValueBuilder.length() - 2);

            AuditLog logEntry = new AuditLog(
                    getNextAuditLogId(),
                    SessionManager.getLoggedInUserId(),
                    SessionManager.isAdmin() ? ADMIN : "User",
                    "UPDATE",
                    PROPOSAL,
                    finalOldValue,
                    finalNewValue,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            );

            AuditLogRepository auditLogRepository = new AuditLogRepository();
            new Thread(() -> auditLogRepository.logChange(logEntry)).start();

        } catch (SQLException | EmptyRepositoryResultException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }

    /**
     * A helper method to extract a Proposal object from a database ResultSet.
     *
     * @param resultSet The ResultSet from a database query.
     * @return A fully populated Proposal object.
     * @throws SQLException if a column is not found or a data type mismatch occurs.
     */
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

    /**
     * Calculates the next available ID for a new proposal.
     * It queries the database for the current maximum ID and increments it.
     *
     * @return The next ID to be used for a new proposal.
     */
    public Long getNextProposalId() {
        String query = "SELECT MAX(id) FROM PROPOSALS";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                long maxId = resultSet.getLong(1);
                return maxId + 1;
            }
        } catch (SQLException e) {
            log.error("Error fetching latest proposal ID", e);
            throw new RepositoryAccessException("Error fetching latest proposal ID", e);
        }

        return 1L; // Default to 1 if the table is empty
    }

    /**
     * Deletes a proposal from the database by its ID and logs the action.
     *
     * @param proposalId The ID of the proposal to delete.
     * @throws SQLException if a database error occurs.
     * @throws EmptyRepositoryResultException if the proposal to delete is not found.
     * @throws RepositoryAccessException if the deletion fails.
     */
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

            AuditLogRepository auditLogRepository = new AuditLogRepository();
            new Thread(() -> auditLogRepository.logChange(logEntry)).start();

        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }

    /**
     * Updates the status of a specific proposal (e.g., to APPROVED or REJECTED)
     * and logs this specific change.
     *
     * @param proposalId The ID of the proposal to update.
     * @param newStatus The new status for the proposal.
     * @throws SQLException if a database error occurs.
     * @throws EmptyRepositoryResultException if the proposal to update is not found.
     * @throws RepositoryAccessException if the update fails.
     */
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

            AuditLogRepository auditLogRepository = new AuditLogRepository();
            new Thread(() -> auditLogRepository.logChange(logEntry)).start();

        } catch (SQLException | EmptyRepositoryResultException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
}
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
 * Repozitorij za upravljanje podacima o prijedlozima ({@link Proposal}) u bazi podataka.
 * Pruža metode za pronalaženje, spremanje, ažuriranje i brisanje prijedloga.
 * Također, asinkrono logira sve promjene podataka u audit log.
 *
 * @param <T> Tip prijedloga, mora nasljeđivati {@link Proposal}.
 */
public class ProposalDatabaseRepository<T extends Proposal> extends AbstractRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(ProposalDatabaseRepository.class);

    private static final String DATABASE_ERROR = "Database error: {}";
    private static final String DATE_PATTERN =  "yyyy-MM-dd HH:mm:ss";
    private static final String PROPOSAL = "Proposal";
    private static final String ADMIN = "Admin";

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public void save(List<T> entities) throws RepositoryAccessException, SQLException {
        String sql = "INSERT INTO proposals (title, description, status, client_id, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = new DatabaseConnection().connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
     * Dohvaća sljedeći dostupan ID za zapis u audit logu.
     * @return Sljedeći ID.
     */
    private Long getNextAuditLogId() {
        List<AuditLog> logs = new AuditLogRepository().readAuditLogs();
        return logs.isEmpty() ? 1L : logs.get(logs.size() - 1).id() + 1;
    }

    /**
     * Sprema jedan prijedlog u bazu podataka i asinkrono zapisuje promjenu u audit log.
     * @param entity Prijedlog koji se sprema.
     * @throws RepositoryAccessException ako spremanje u bazu ne uspije.
     */
    @Override
    public void save(T entity) {
        List<T> proposals = new ArrayList<>();
        proposals.add(entity);
        try {
            save(proposals);
        } catch (SQLException e) {
            throw new RepositoryAccessException(e);
        }

        AuditLog logEntry = new AuditLog(
                getNextAuditLogId(),
                SessionManager.getLoggedInUserId(),
                SessionManager.isAdmin() ? ADMIN : "User",
                "ADD",
                PROPOSAL,
                "N/A",
                entity.getTitle(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        );

        AuditLogRepository auditLogRepository = new AuditLogRepository();
        new Thread(() -> auditLogRepository.logChange(logEntry)).start();
    }

    /**
     * Ažurira postojeći prijedlog (naslov, opis, klijent) u bazi podataka.
     * Ako su podaci promijenjeni, asinkrono se zapisuje promjena u audit log.
     *
     * @param proposal Prijedlog s ažuriranim informacijama.
     * @throws RepositoryAccessException ako ažuriranje ne uspije.
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
     * Pomoćna metoda za kreiranje {@link Proposal} objekta iz {@link ResultSet}-a.
     *
     * @param resultSet ResultSet iz kojeg se čitaju podaci.
     * @return Kreirani Proposal objekt.
     * @throws SQLException ako dođe do greške pri čitanju stupaca.
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
     * Izračunava i vraća sljedeći slobodan ID za novi prijedlog.
     * @return Sljedeći ID za novi prijedlog.
     * @throws RepositoryAccessException ako dođe do greške pri dohvatu ID-ja.
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

        return 1L;
    }

    /**
     * Briše prijedlog iz baze podataka prema zadanom ID-ju i asinkrono logira akciju.
     *
     * @param proposalId ID prijedloga koji se briše.
     * @throws SQLException ako dođe do greške pri pristupu bazi.
     * @throws EmptyRepositoryResultException ako prijedlog za brisanje nije pronađen.
     * @throws RepositoryAccessException ako brisanje ne uspije.
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
                    oldProposal.getTitle(), // Logira naslov
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
     * Ažurira status određenog prijedloga (npr. u APPROVED ili REJECTED) i asinkrono logira promjenu.
     *
     * @param proposalId ID prijedloga čiji se status mijenja.
     * @param newStatus Novi status prijedloga.
     * @throws SQLException ako dođe do greške pri pristupu bazi.
     * @throws EmptyRepositoryResultException ako prijedlog za ažuriranje nije pronađen.
     * @throws RepositoryAccessException ako ažuriranje ne uspije.
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

            AuditLog logEntry = new AuditLog(
                    getNextAuditLogId(),
                    SessionManager.getLoggedInUserId(),
                    SessionManager.isAdmin() ? ADMIN : "User",
                    "UPDATE STATUS",
                    PROPOSAL,
                    oldProposal.getStatus().toString(),
                    newStatus.toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            );

            AuditLogRepository auditLogRepository = new AuditLogRepository();
            new Thread(() -> auditLogRepository.logChange(logEntry)).start();

        } catch (SQLException e) {
            log.error(DATABASE_ERROR, e.getMessage(), e);
            throw new RepositoryAccessException(e);
        }
    }
}
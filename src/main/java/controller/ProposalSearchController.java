package controller;

import exception.EmptyRepositoryResultException;
import exception.ProposalSearchException;
import exception.SwitchingScreensExcpetion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Proposal;
import model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ProposalDatabaseRepository;
import repository.ClientDatabaseRepository;
import session.SessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Controller for Proposal search screen
 *
 */
public class ProposalSearchController {

    @FXML
    private TextField ideaTextField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField clientTextField;

    @FXML
    private TableView<Proposal> proposalTableView;

    @FXML
    private TableColumn<Proposal, String> ideaTableColumn;

    @FXML
    private TableColumn<Proposal, String> descriptionTableColumn;

    @FXML
    private TableColumn<Proposal, String> statusTableColumn;

    @FXML
    private TableColumn<Proposal, String> clientTableColumn;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    private final ProposalDatabaseRepository<Proposal> proposalRepository = new ProposalDatabaseRepository<>();
    private final ClientDatabaseRepository<Client> clientRepository = new ClientDatabaseRepository<>();
    private final DashboardController dashboardController = new DashboardController();

    private static final Logger log = LoggerFactory.getLogger(ProposalSearchController.class);

    /**
     * Initializes all the necessary stuff
     *
     */
    public void initialize() {
        ideaTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));
        descriptionTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
        statusTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        clientTableColumn.setCellValueFactory(cellData -> {
            Long clientId = cellData.getValue().getClientId();
            Client client = null;
            try {
                client = clientRepository.findById(clientId);
            } catch (EmptyRepositoryResultException | SQLException e) {
                log.error("Input error: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty(client != null ? client.getName() : "Unknown Client");
        });

        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "Approved", "Rejected"));

        boolean isAdmin = SessionManager.isAdmin();

        if(!isAdmin)
        {
            dashboardController.disableButton(approveButton);
            dashboardController.disableButton(rejectButton);
        }

    }

    public void filterProposals() {
        Long currentUserId = SessionManager.getLoggedInUserId();
        boolean isAdmin = SessionManager.isAdmin();

        List<Proposal> proposalList = proposalRepository.findAll();

        if (!isAdmin) {
            proposalList = proposalList.stream()
                    .filter(proposal -> proposal.getUserId() == currentUserId)
                    .toList();
        }

        String idea = ideaTextField.getText();
        if (!idea.isEmpty()) {
            proposalList = new ArrayList<>(proposalList.stream()
                    .filter(proposal -> proposal.getTitle().toLowerCase().contains(idea.toLowerCase()))
                    .toList());
        }

        String status = statusComboBox.getValue();
        if (status != null && !status.isEmpty()) {
            proposalList = new ArrayList<>(proposalList.stream()
                    .filter(proposal -> proposal.getStatus().toString().equalsIgnoreCase(status))
                    .toList());
        }

        String clientName = clientTextField.getText().trim();
        if (!clientName.isEmpty()) {
            List<Proposal> filteredProposals = new ArrayList<>();

            for (Proposal proposal : proposalList) {
                try {
                    Client client = clientRepository.findById(proposal.getClientId());
                    if (client != null && client.getName().toLowerCase().contains(clientName.toLowerCase())) {
                        filteredProposals.add(proposal);
                    }
                } catch (EmptyRepositoryResultException | SQLException e) {
                    throw new ProposalSearchException(e);
                }
            }

            proposalList = filteredProposals;
        }

        ObservableList<Proposal> proposalObservableList =
                FXCollections.observableList(proposalList);

        proposalTableView.setItems(proposalObservableList);
    }

    public void openProposalEditScreen() throws SwitchingScreensExcpetion {
        Proposal selectedProposal = proposalTableView.getSelectionModel().getSelectedItem();
        new MenuController().showProposalEditScreen(selectedProposal);
    }

    public void handleDeleteProposal() throws SQLException, EmptyRepositoryResultException {
        Proposal selectedProposal = proposalTableView.getSelectionModel().getSelectedItem();

        if (selectedProposal == null) {
            showError("No Proposal Selected", "Please select a proposal to delete.");
            return;
        }

        Long currentUserId = SessionManager.getLoggedInUserId();
        boolean isAdmin = SessionManager.isAdmin();

        if (!isAdmin && selectedProposal.getUserId() != currentUserId) {
            showError("Access Denied", "You can only delete your own proposals.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Proposal");
        confirmation.setContentText("Are you sure you want to delete this proposal?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            proposalRepository.deleteProposal(selectedProposal.getId());
            proposalTableView.getItems().remove(selectedProposal);
        }
    }

    public void handleApproveProposal() throws SQLException, EmptyRepositoryResultException {
        updateProposalStatus(enums.ProposalStatus.APPROVED);
    }

    public void handleRejectProposal() throws SQLException, EmptyRepositoryResultException {
        updateProposalStatus(enums.ProposalStatus.REJECTED);
    }

    private void updateProposalStatus(enums.ProposalStatus newStatus) throws SQLException, EmptyRepositoryResultException {
        Proposal selectedProposal = proposalTableView.getSelectionModel().getSelectedItem();

        if (selectedProposal == null) {
            showError("No Proposal Selected", "Please select a proposal.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm " + newStatus + " Action");
        confirmation.setHeaderText("Are you sure you want to " + newStatus.toString().toLowerCase() + " this proposal?");
        confirmation.setContentText("Proposal Details:\n"
                + "Title: " + selectedProposal.getTitle() + "\n"
                + "Description: " + selectedProposal.getDescription() + "\n"
                + "Current Status: " + selectedProposal.getStatus());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            proposalRepository.updateStatus(selectedProposal.getId(), newStatus);
            selectedProposal.setStatus(newStatus);
            proposalTableView.refresh();

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Proposal " + newStatus);
            successAlert.setHeaderText("Proposal successfully " + newStatus.toString().toLowerCase() + "!");
            successAlert.setContentText("Proposal '" + selectedProposal.getTitle() + "' is now marked as " + newStatus + ".");
            successAlert.showAndWait();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

}

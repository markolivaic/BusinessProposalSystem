package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Client;
import model.Proposal;
import repository.ClientDatabaseRepository;
import repository.ProposalDatabaseRepository;

import java.util.List;
/**
 * Controller for Edit proposal screen
 *
 */
public class EditProposalController {

    @FXML
    private TextField newProposalTextField;

    @FXML
    private TextField newDescriptionTextField;

    @FXML
    private ComboBox<Client> newClientComboBox;

    private final ProposalDatabaseRepository<Proposal> proposalRepository = new ProposalDatabaseRepository<>();
    private final ClientDatabaseRepository<Client> clientRepository = new ClientDatabaseRepository<>();

    private Proposal currentProposal;
    private Long currentUserId;

    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
    }

    /**
     * Initializes all the necessary stuff
     *
     */
    public void initialize() {
        List<Client> clients = clientRepository.findAll();
        newClientComboBox.getItems().addAll(clients);
    }

    public void loadProposalForEditing(Proposal proposal) {
        this.currentProposal = proposal;

        newProposalTextField.setText(proposal.getTitle());
        newDescriptionTextField.setText(proposal.getDescription());

        for (Client client : newClientComboBox.getItems()) {
            if (client.getId() == proposal.getClientId()) {
                newClientComboBox.setValue(client);
                break;
            }
        }
    }

    public void loadNewProposalMode() {
        this.currentProposal = null;
        newProposalTextField.clear();
        newDescriptionTextField.clear();
        newClientComboBox.getSelectionModel().clearSelection();
    }

    public void saveProposal() {
        String title = newProposalTextField.getText();
        String description = newDescriptionTextField.getText();
        Client selectedClient = newClientComboBox.getValue();

        if (title.isEmpty() || description.isEmpty() || selectedClient == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Proposal not saved");
            alert.setContentText("All fields must be filled!");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Save");
        confirmation.setHeaderText("Are you sure you want to save this proposal?");
        confirmation.setContentText("Title: " + title + "\nDescription: " + description);

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (currentProposal == null) {
                Proposal newProposal = new Proposal.Builder(proposalRepository.getNextProposalId())
                        .withTitle(title)
                        .withDescription(description)
                        .withStatus(enums.ProposalStatus.PENDING)
                        .withClientId(selectedClient.getId())
                        .withUserId(currentUserId)
                        .build();
                proposalRepository.save(newProposal);
            } else {
                currentProposal.setTitle(title);
                currentProposal.setDescription(description);
                currentProposal.setClientId(selectedClient.getId());
                proposalRepository.update(currentProposal);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Proposal Saved!");
            alert.setContentText("The proposal has been successfully saved.");
            alert.showAndWait();

            ((Stage) newProposalTextField.getScene().getWindow()).close();
        }
    }

}

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
 * Kontroler za ekran dodavanja i uređivanja prijedloga.
 * Upravlja unosom podataka, validacijom te spremanjem (novog) ili ažuriranjem (postojećeg) prijedloga.
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

    /**
     * Postavlja ID trenutno prijavljenog korisnika.
     * @param userId ID korisnika.
     */
    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
    }

    /**
     * Inicijalizira kontroler. Popunjava ComboBox s klijentima iz baze podataka.
     */
    public void initialize() {
        List<Client> clients = clientRepository.findAll();
        newClientComboBox.getItems().addAll(clients);
    }

    /**
     * Učitava podatke postojećeg prijedloga u polja za uređivanje.
     * @param proposal Prijedlog koji se uređuje.
     */
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

    /**
     * Postavlja ekran u mod za unos novog prijedloga, čisteći sva polja.
     */
    public void loadNewProposalMode() {
        this.currentProposal = null;
        newProposalTextField.clear();
        newDescriptionTextField.clear();
        newClientComboBox.getSelectionModel().clearSelection();
    }

    /**
     * Sprema promjene (ili novi prijedlog) nakon validacije i potvrde korisnika.
     * Ako je {@code currentProposal} null, kreira se novi prijedlog. Inače, ažurira se postojeći.
     */
    public void saveProposal() {
        String title = newProposalTextField.getText();
        String description = newDescriptionTextField.getText();
        Client selectedClient = newClientComboBox.getValue();

        if (title.isEmpty() || description.isEmpty() || selectedClient == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Proposal not saved", "All fields must be filled!");
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

            showAlert(Alert.AlertType.INFORMATION, "Success", "Proposal Saved!", "The proposal has been successfully saved.");
            closeWindow();
        }
    }

    /**
     * Zatvara prozor za uređivanje prijedloga.
     */
    private void closeWindow() {
        ((Stage) newProposalTextField.getScene().getWindow()).close();
    }

    /**
     * Pomoćna metoda za prikazivanje dijaloga.
     * @param type Tip alerta.
     * @param title Naslov prozora.s
     * @param header Tekst zaglavlja.
     * @param content Sadržaj poruke.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
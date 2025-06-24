package controller;

import exception.SwitchingScreensExcpetion;
import hr.javafx.business.businessproposalsystem.BusinessProposalApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.io.IOException;

/**
 * Kontroler koji upravlja navigacijom između ekrana putem glavnog izbornika (MenuBar).
 * Svaka FXML datoteka koja sadrži menu.fxml koristi ovaj kontroler.
 */
public class MenuController {

    private static final String ERROR_MESSAGE = "Error occurred";
    private static final String ERROR_ADMIN_MESSAGE = "You don't have administrator privileges";
    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    /**
     * Prikazuje ekran za pretragu prijedloga.
     * @throws IOException ako dođe do greške pri učitavanju FXML datoteke.
     */
    public void showProposalSearchScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("proposalSearchScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 600);
        BusinessProposalApplication.getMainStage().setTitle("Search Proposals");
        BusinessProposalApplication.getMainStage().setScene(scene);
        BusinessProposalApplication.getMainStage().show();
    }

    /**
     * Prikazuje ekran za dodavanje novog prijedloga.
     * @throws SwitchingScreensExcpetion ako dođe do greške pri otvaranju ekrana.
     */
    public void showProposalEditScreen() throws SwitchingScreensExcpetion {
        showProposalEditScreen(null);
    }

    /**
     * Prikazuje ekran za uređivanje postojećeg ili dodavanje novog prijedloga.
     * Otvara se u novom prozoru (Stage).
     * @param selectedProposal Prijedlog za uređivanje. Ako je null, otvara se mod za novi unos.
     * @throws SwitchingScreensExcpetion ako dođe do greške pri učitavanju FXML-a.
     */
    public void showProposalEditScreen(Proposal selectedProposal) throws SwitchingScreensExcpetion {
        try {
            FXMLLoader loader = new FXMLLoader(BusinessProposalApplication.class.getResource("proposalEditScreen.fxml"));
            Scene scene = new Scene(loader.load(), 700, 600);
            EditProposalController controller = loader.getController();

            if (SessionManager.isUserLoggedIn()) {
                controller.setCurrentUser(SessionManager.getLoggedInUserId());
            } else {
                showError("User Not Logged In", "Please log in to edit proposals.");
                return;
            }

            if (selectedProposal != null) {
                controller.loadProposalForEditing(selectedProposal);
            } else {
                controller.loadNewProposalMode();
            }

            Stage stage = new Stage();
            stage.setTitle(selectedProposal == null ? "New Proposal" : "Edit Proposal");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showError(ERROR_MESSAGE, "Error loading proposal edit screen: " + e.getMessage());
            log.error("Input error: {}", e.getMessage(), e);
            throw new SwitchingScreensExcpetion("Failed to load proposal edit screen", e);
        }
    }

    /**
     * Prikazuje ekran za pretragu klijenata, samo ako je korisnik administrator.
     * @throws IOException ako dođe do greške pri učitavanju FXML datoteke.
     */
    public void showClientSearchScreen() throws IOException {
        if (SessionManager.isAdmin()) {
            FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("clientSearchScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 600);
            BusinessProposalApplication.getMainStage().setTitle("Search Clients");
            BusinessProposalApplication.getMainStage().setScene(scene);
            BusinessProposalApplication.getMainStage().show();
        } else {
            showError(ERROR_MESSAGE, ERROR_ADMIN_MESSAGE);
        }
    }

    /**
     * Prikazuje ekran za dodavanje klijenata, samo ako je korisnik administrator.
     * @throws IOException ako dođe do greške pri učitavanju FXML datoteke.
     */
    public void showClientEditScreen() throws IOException {
        if (SessionManager.isAdmin()) {
            FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("clientEditScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 600);
            BusinessProposalApplication.getMainStage().setTitle("Edit Clients");
            BusinessProposalApplication.getMainStage().setScene(scene);
            BusinessProposalApplication.getMainStage().show();
        } else {
            showError(ERROR_MESSAGE, ERROR_ADMIN_MESSAGE);
        }
    }

    /**
     * Vraća korisnika na ekran za prijavu i odjavljuje ga.
     * @throws IOException ako dođe do greške pri učitavanju FXML datoteke.
     */
    public void showLogInScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        BusinessProposalApplication.getMainStage().setTitle("Login");
        BusinessProposalApplication.getMainStage().setScene(scene);
        BusinessProposalApplication.getMainStage().show();
        SessionManager.logout();
    }

    /**
     * Prikazuje ekran s revizijskim tragom, samo ako je korisnik administrator.
     * @throws IOException ako dođe do greške pri učitavanju FXML datoteke.
     */
    public void showAuditLogScreen() throws IOException {
        if (SessionManager.isAdmin()) {
            FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("auditLogScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 600);
            BusinessProposalApplication.getMainStage().setTitle("Audit Log");
            BusinessProposalApplication.getMainStage().setScene(scene);
            BusinessProposalApplication.getMainStage().show();
        } else {
            showError(ERROR_MESSAGE, ERROR_ADMIN_MESSAGE);
        }
    }

    /**
     * Prikazuje dijalog s porukom o grešci.
     * @param title Naslov prozora.
     * @param message Poruka koja se prikazuje.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
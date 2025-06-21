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
 * Controller for Menu screen
 *
 */
public class MenuController {

    private static final String ERROR_MESSAGE = "Error occurred";
    private static final String ERROR_ADMIN_MESSAGE = "You don't have administrator privileges";
    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    public void showProposalSearchScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("proposalSearchScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 600);
        BusinessProposalApplication.getMainStage().setTitle("Search Proposals");
        BusinessProposalApplication.getMainStage().setScene(scene);
        BusinessProposalApplication.getMainStage().show();
    }

    public void showProposalEditScreen() throws SwitchingScreensExcpetion {
        showProposalEditScreen(null);
    }

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

        } catch (IOException  e) {
            showError(ERROR_MESSAGE, "Error loading proposal edit screen: " + e.getMessage());
            log.error("Input error: {}", e.getMessage(), e);
            throw new SwitchingScreensExcpetion();
        }
    }

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

    public void showLogInScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        BusinessProposalApplication.getMainStage().setTitle("Login");
        BusinessProposalApplication.getMainStage().setScene(scene);
        BusinessProposalApplication.getMainStage().show();
        SessionManager.logout();
    }

    public void showAuditLogScreen() throws IOException {
        if (SessionManager.isAdmin()) {
            FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("AuditLogScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 600);
            BusinessProposalApplication.getMainStage().setTitle("Audit Log");
            BusinessProposalApplication.getMainStage().setScene(scene);
            BusinessProposalApplication.getMainStage().show();
        } else {
            showError(ERROR_MESSAGE, ERROR_ADMIN_MESSAGE);
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

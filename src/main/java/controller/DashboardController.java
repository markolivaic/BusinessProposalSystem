package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;
import java.io.IOException;

/**
 * Controller for Dashboard screen
 *
 */
public class DashboardController {

    private final MenuController menuController = new MenuController();
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final String IO_ERROR = "IO error: {}";

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button clientsButton;

    @FXML
    private Button auditLogButton;

    /**
     * Initializes all the necessary stuff
     *
     */
    public void initialize() {

        if (!SessionManager.isUserLoggedIn()) {
            showError("Error", "No user logged in!");
            return;
        }

        boolean isAdmin = SessionManager.isAdmin();

        String role = isAdmin ? "Admin" : "User";
        welcomeLabel.setText("Welcome: " + role);

        if (!isAdmin) {
            disableButton(clientsButton);
            disableButton(auditLogButton);
        }
    }

    public void disableButton(Button button) {
        button.setDisable(true);
        button.setOpacity(0.5);
    }

    public void showProposalSearchScreen() {
        try {
            menuController.showProposalSearchScreen();
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
        }
    }

    public void showClientsSearchScreen(){
        try {
            if (SessionManager.isAdmin()) {
                menuController.showClientSearchScreen();
            } else {
                showError("Access Denied", "Only administrators can access clients!");
            }
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
        }
    }

    public void showAuditLogScreen() {
        try {
            if (SessionManager.isAdmin()) {
                menuController.showAuditLogScreen();
            } else {
                showError("Access Denied", "Only administrators can access the audit log!");
            }
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
        }
    }

    public void handleLogout() {
        try {
            SessionManager.logout();
            menuController.showLogInScreen();
        } catch (IOException e) {
           log.error(IO_ERROR, e.getMessage(), e);
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

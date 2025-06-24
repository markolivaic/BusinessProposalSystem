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
 * Kontroler za glavnu nadzornu ploču (Dashboard).
 * Prikazuje poruku dobrodošlice i omogućuje navigaciju na druge ekrane,
 * uz provjeru korisničkih prava (rola).
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
     * Inicijalizira nadzornu ploču.
     * Provjerava je li korisnik prijavljen, postavlja poruku dobrodošlice
     * i onemogućuje gumbe za administrativne funkcije ako korisnik nije admin.
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

    /**
     * Onemogućuje zadani gumb i smanjuje mu prozirnost.
     * @param button Gumb koji treba onemogućiti.
     */
    public void disableButton(Button button) {
        button.setDisable(true);
        button.setOpacity(0.5);
    }

    /**
     * Prikazuje ekran za pretragu prijedloga.
     */
    public void showProposalSearchScreen() {
        try {
            menuController.showProposalSearchScreen();
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Prikazuje ekran za pretragu klijenata, samo ako je korisnik administrator.
     */
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

    /**
     * Prikazuje ekran s revizijskim tragom, samo ako je korisnik administrator.
     */
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

    /**
     * Upravlja odjavom korisnika i preusmjerava na ekran za prijavu.
     */
    public void handleLogout() {
        try {
            SessionManager.logout();
            menuController.showLogInScreen();
        } catch (IOException e) {
            log.error(IO_ERROR, e.getMessage(), e);
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
package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.Pair;
import repository.UserRepository;
import session.SessionManager;
import hr.javafx.business.businessproposalsystem.BusinessProposalApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

/**
 * Kontroler za ekran za prijavu korisnika.
 * Upravlja unosom korisničkog imena i lozinke, autentifikacijom
 * i preusmjeravanjem na nadzornu ploču.
 */
public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label wrongUsernameLabel;

    @FXML
    private Label wrongPasswordLabel;

    private final UserRepository userRepository = new UserRepository();
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    /**
     * Obrađuje pokušaj prijave korisnika.
     * Provjerava unesene podatke, poziva metodu za autentifikaciju
     * i, ako je uspješna, preusmjerava na nadzornu ploču.
     */
    public void handleLogin() {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Username and Password cannot be empty.");
            return;
        }

        Pair<Long, Boolean> authenticatedUser = userRepository.authenticateUser(username, password);

        if (authenticatedUser != null) {
            Long userId = authenticatedUser.getKey();
            boolean isAdmin = authenticatedUser.getValue();
            SessionManager.login(userId, isAdmin);
            showDashboardScreen();
        } else {
            showLoginError(username);
        }
    }

    /**
     * Prikazuje odgovarajuću poruku o grešci ako prijava nije uspjela.
     * @param username Uneseno korisničko ime, za provjeru postoji li.
     */
    private void showLoginError(String username) {
        wrongUsernameLabel.setVisible(false);
        wrongPasswordLabel.setVisible(false);

        if (userRepository.findByUsername(username) == null) {
            wrongUsernameLabel.setText("Username not found.");
            wrongUsernameLabel.setVisible(true);
        } else {
            wrongPasswordLabel.setText("Incorrect password.");
            wrongPasswordLabel.setVisible(true);
        }
    }

    /**
     * Prikazuje nadzornu ploču nakon uspješne prijave.
     */
    private void showDashboardScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("dashboardScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 400);
            BusinessProposalApplication.getMainStage().setTitle("Dashboard");
            BusinessProposalApplication.getMainStage().setScene(scene);
            BusinessProposalApplication.getMainStage().show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Unable to open Dashboard.");
            log.error("IO error: {}", e.getMessage(), e);
        }
    }

    /**
     * Prikazuje dijalog s porukom o grešci.
     * @param title Naslov prozora.
     * @param message Poruka koja se prikazuje.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Login was not successful");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
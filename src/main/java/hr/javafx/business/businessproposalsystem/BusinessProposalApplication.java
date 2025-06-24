package hr.javafx.business.businessproposalsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.UserRepository;

import java.io.IOException;

/**
 * Glavna ulazna točka za pokretanje JavaFX aplikacije "Business Proposal Management System".
 * Inicijalizira aplikaciju, postavlja glavni prozor (Stage) i prikazuje početni ekran za prijavu.
 */
public class BusinessProposalApplication extends Application {

    private static Stage mainStage;

    /**
     * Metoda koja se poziva pri pokretanju JavaFX aplikacije.
     * Postavlja glavni prozor aplikacije (Stage), inicijalizira korisnike iz datoteke u bazu
     * i prikazuje ekran za prijavu.
     *
     * @param stage Glavni prozor (Stage) koji automatski osigurava JavaFX platforma.
     * @throws IOException Ako dođe do greške pri učitavanju FXML datoteke.
     */
    @Override
    public void start(Stage stage) throws IOException {
        setMainStage(stage);

        UserRepository userRepository = new UserRepository();
        userRepository.importUsersFromFile();

        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        stage.setTitle("Login!");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Glavna metoda koja pokreće JavaFX aplikaciju.
     *
     * @param args Argumenti komandne linije (ne koriste se).
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Vraća referencu na glavni prozor (Stage) aplikacije.
     * Omogućuje drugim dijelovima aplikacije pristup i manipulaciju glavnim prozorom.
     *
     * @return Glavni JavaFX prozor (Stage).
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Postavlja referencu na glavni prozor aplikacije.
     *
     * @param stage Prozor (Stage) koji će biti postavljen kao glavni.
     */
    private static void setMainStage(Stage stage) {
        mainStage = stage;
    }
}
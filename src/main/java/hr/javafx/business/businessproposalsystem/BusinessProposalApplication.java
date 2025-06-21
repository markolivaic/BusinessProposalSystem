package hr.javafx.business.businessproposalsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.AuditLogRepository;
import repository.UserRepository;

import java.io.IOException;

/**
 * This is the main class for launching the Business Proposal Management System.
 * It initializes the JavaFX application and sets up the login screen.
 */
public class BusinessProposalApplication extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        setMainStage(stage);

        UserRepository userRepository = new UserRepository();
        userRepository.importUsersFromFile();

        FXMLLoader fxmlLoader = new FXMLLoader(BusinessProposalApplication.class.getResource("dashBoardScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        stage.setTitle("Login!");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> AuditLogRepository.shutdown());
    }

    /**
     * Starts the application.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Returns the main stage of the application.
     * @return  The main JavaFX stage.
     */
    public static Stage getMainStage() {
        return mainStage;
    }
    /**
     * Sets the main stage of the application.
     * @param stage The JavaFX stage to be set as the main stage.
     */
    private static void setMainStage(Stage stage) {
        mainStage = stage;
    }
}
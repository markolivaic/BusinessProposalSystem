package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import model.Client;
import repository.ClientDatabaseRepository;

/**
 * Controller for Edit client screen
 *
 */
public class EditClientController {

    @FXML
    private TextField newClientTextField;

    @FXML
    private TextField newEmailTextField;

    @FXML
    private TextField newPhoneTextField;

    @FXML
    private TextField newCompanyTextField;


    public void addNewClient() {
        StringBuilder errorMessages = new StringBuilder();

        String name = newClientTextField.getText();
        if (name.isEmpty()) {
            errorMessages.append("You must enter the client's full name!\n");
        }

        String email = newEmailTextField.getText();
        if (email.isEmpty()) {
            errorMessages.append("You must enter the client's email!\n");
        }

        String phone = newPhoneTextField.getText();
        if (phone.isEmpty()) {
            errorMessages.append("You must enter the client's phone number!\n");
        }

        String company = newCompanyTextField.getText();
        if (company.isEmpty()) {
            errorMessages.append("You must enter the client's company name!\n");
        }

        if (!errorMessages.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error while entering a new client");
            alert.setHeaderText("Client " + name + " was not added due to an input error");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
        } else {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Client Addition");
            confirmation.setHeaderText("Are you sure you want to add this client?");
            confirmation.setContentText("Client Details:\n"
                    + "Name: " + name + "\n"
                    + "Email: " + email + "\n"
                    + "Phone number: " + phone + "\n"
                    + "Company: " + company);

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Client client = new Client(name, email, company, phone);
                ClientDatabaseRepository<Client> repository = new ClientDatabaseRepository<>();
                repository.save(client);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Successfully added a new client");
                successAlert.setHeaderText("Client " + name + " was successfully added!");
                successAlert.setContentText("Client Details:\n"
                        + "Name: " + name + "\n"
                        + "Email: " + email + "\n"
                        + "Phone number: " + phone + "\n"
                        + "Company: " + company);
                successAlert.showAndWait();

                newClientTextField.clear();
                newEmailTextField.clear();
                newPhoneTextField.clear();
                newCompanyTextField.clear();
            }
        }
    }
}

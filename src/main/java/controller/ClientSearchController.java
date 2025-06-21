package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Client;
import repository.AbstractRepository;
import repository.ClientDatabaseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Client search screen
 *
 */
public class ClientSearchController {

    @FXML
    private TextField clientNameTextField;

    @FXML
    private TextField clientEmailTextField;

    @FXML
    private TextField clientPhoneTextField;

    @FXML
    private TextField clientCompanyTextField;

    @FXML
    private TableView<Client> clientTableView;

    @FXML
    private TableColumn<Client, String> clientNameTableColumn;

    @FXML
    private TableColumn<Client, String> clientEmailTableColumn;

    @FXML
    private TableColumn<Client, String> clientPhoneTableColumn;

    @FXML
    private TableColumn<Client, String> clientCompanyTableColumn;

    private final AbstractRepository<Client> clientRepository = new ClientDatabaseRepository<>();

    /**
     * Initializes all the necessary stuff
     *
     */
    public void initialize() {
        clientNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        clientEmailTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        clientPhoneTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        clientCompanyTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCompany()));

    }

    public void filterClients(){
        List<Client> clientList;
        clientList = clientRepository.findAll();

        String clientName = clientNameTextField.getText();

        if (!clientName.isEmpty()) {
            clientList = new ArrayList<>(clientList.stream()
                    .filter(client -> client.getName().toLowerCase().contains(clientName.toLowerCase()))
                    .toList());
        }

        String clientEmail = clientEmailTextField.getText();

        if (!clientEmail.isEmpty()) {
            clientList = new ArrayList<>(clientList.stream()
                    .filter(client -> client.getEmail().toLowerCase().contains(clientEmail.toLowerCase()))
                    .toList());
        }

        String clientPhone = clientPhoneTextField.getText();

        if (!clientPhone.isEmpty()) {
            clientList = new ArrayList<>(clientList.stream()
                    .filter(client -> client.getPhone().toLowerCase().contains(clientPhone.toLowerCase()))
                    .toList());
        }

        String clientCompany = clientCompanyTextField.getText();

        if (!clientCompany.isEmpty()) {
            clientList = new ArrayList<>(clientList.stream()
                    .filter(client -> client.getCompany().toLowerCase().contains(clientCompany.toLowerCase()))
                    .toList());
        }


        ObservableList<Client> categoryObservableList =
                FXCollections.observableList(clientList);

        clientTableView.setItems(categoryObservableList);

    }

}

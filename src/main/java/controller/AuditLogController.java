package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AuditLog;
import repository.AuditLogRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Kontroler za ekran prikaza revizijskog traga (Audit Log).
 * Upravlja učitavanjem, prikazom i filtriranjem zapisa o promjenama u sustavu.
 */
public class AuditLogController {

    @FXML
    private TableView<AuditLog> auditLogTable;

    @FXML
    private TableColumn<AuditLog, String> actionColumn;

    @FXML
    private TableColumn<AuditLog, String> entityColumn;

    @FXML
    private TableColumn<AuditLog, String> oldValueColumn;

    @FXML
    private TableColumn<AuditLog, String> newValueColumn;

    @FXML
    private TableColumn<AuditLog, String> roleColumn;

    @FXML
    private TableColumn<AuditLog, String> timestampColumn;

    @FXML
    private ComboBox<String> actionFilterComboBox;

    @FXML
    private DatePicker dateFilterPicker;

    private final AuditLogRepository auditLogRepository = new AuditLogRepository();
    private final ObservableList<AuditLog> auditLogList = FXCollections.observableArrayList();

    /**
     * Inicijalizira kontroler nakon što je FXML datoteka učitana.
     * Postavlja tvornice vrijednosti za stupce tablice, popunjava filtere
     * i pokreće učitavanje podataka.
     */
    public void initialize() {
        actionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().action()));
        entityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().entityName()));
        oldValueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().oldValue()));
        newValueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().newValue()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().userRole()));
        timestampColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().timestamp()));

        actionFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "ADD", "UPDATE", "DELETE", "UPDATE STATUS"));
        actionFilterComboBox.setValue("ALL");

        loadAuditLogs();

        actionFilterComboBox.setOnAction(event -> filterAuditLogs());
        dateFilterPicker.setOnAction(event -> filterAuditLogs());
    }

    /**
     * Učitava zapise iz audit log datoteke koristeći pozadinsku nit (Task).
     * Time se sprječava blokiranje korisničkog sučelja (UI) tijekom I/O operacije.
     * Po završetku, popunjava tablicu s učitanim podacima.
     */
    private void loadAuditLogs() {
        Task<List<AuditLog>> loadTask = new Task<>() {
            @Override
            protected List<AuditLog> call() {
                return auditLogRepository.readAuditLogs();
            }
        };

        loadTask.setOnSucceeded(event -> {
            auditLogList.setAll(loadTask.getValue());
            auditLogTable.setItems(auditLogList);
        });

        new Thread(loadTask).start();
    }

    /**
     * Filtrira prikazane zapise u tablici na temelju odabranih vrijednosti
     * u ComboBox-u za akciju i DatePicker-u za datum.
     */
    private void filterAuditLogs() {
        String selectedAction = actionFilterComboBox.getValue();
        LocalDate selectedDate = dateFilterPicker.getValue();

        List<AuditLog> filteredLogs = auditLogList.stream()
                .filter(log -> "ALL".equalsIgnoreCase(selectedAction) || log.action().equalsIgnoreCase(selectedAction))
                .filter(log -> {
                    if (selectedDate == null) {
                        return true;
                    }
                    return log.timestamp().startsWith(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                })
                .toList();

        auditLogTable.setItems(FXCollections.observableArrayList(filteredLogs));
    }
}
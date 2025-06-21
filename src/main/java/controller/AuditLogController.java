package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AuditLog;
import repository.AuditLogRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for AuditLog screen
 *
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
     * Initializes all the necessary stuff
     *
     */
    public void initialize() {
        actionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().action()));
        entityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().entityName()));
        oldValueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().oldValue()));
        newValueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().newValue()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().userRole()));
        timestampColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().timestamp()));

        actionFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "ADD", "UPDATE", "DELETE"));
        actionFilterComboBox.setValue("ALL");

        loadAuditLogs();

        actionFilterComboBox.setOnAction(event -> filterAuditLogs());
        dateFilterPicker.setOnAction(event -> filterAuditLogs());
    }

    /**
     * Loads the history logs to the table
     *
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
     * Filters the history logs
     *
     */
    private void filterAuditLogs() {
        String selectedAction = actionFilterComboBox.getValue();
        LocalDate selectedDate = dateFilterPicker.getValue();

        List<AuditLog> filteredLogs = new ArrayList<>(auditLogList.stream()
                .filter(log -> "ALL".equals(selectedAction) || log.action().equals(selectedAction))
                .filter(log -> selectedDate == null || log.timestamp().startsWith(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .toList());

        auditLogTable.setItems(FXCollections.observableArrayList(filteredLogs));
    }
}

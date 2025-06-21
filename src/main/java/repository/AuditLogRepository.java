package repository;

import model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditLogRepository {
    private static final String AUDIT_LOG_FILE = "dat/audit_log.dat";
    private static final Logger log = LoggerFactory.getLogger(AuditLogRepository.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public synchronized void logChange(AuditLog auditLog) {
        List<AuditLog> logs = readAuditLogs();
        logs.add(auditLog);

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(AUDIT_LOG_FILE))) {
            out.writeObject(logs);
        } catch (IOException e) {
            log.error("Error writing audit log: {}", e.getMessage(), e);
        }
    }

    public synchronized List<AuditLog> readAuditLogs() {
        File file = new File(AUDIT_LOG_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<AuditLog>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading audit log: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void logChangeAsync(AuditLog auditLog) {
        executor.execute(() -> logChange(auditLog));
    }

    public static void shutdown() {
        executor.shutdown();
    }
}

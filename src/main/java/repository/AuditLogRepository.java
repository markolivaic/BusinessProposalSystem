package repository;

import model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za upravljanje zapisima o promjenama (audit log).
 * Koristi sinkronizirane metode za siguran, konkurentan pristup binarnoj datoteci
 * u kojoj se pohranjuju serijalizirani {@link AuditLog} objekti.
 */
public class AuditLogRepository {
    private static final String AUDIT_LOG_FILE = "dat/audit_log.dat";
    private static final Logger log = LoggerFactory.getLogger(AuditLogRepository.class);

    private static boolean loggedInProgress = false;

    /**
     * Sinkronizirano zapisuje jedan {@link AuditLog} zapis u binarnu datoteku.
     * Ako je druga nit već u procesu pisanja, ova će pričekati koristeći wait/notify mehanizam.
     *
     * @param auditLog Zapis koji se sprema.
     */
    public synchronized void logChange(AuditLog auditLog) {
        while (loggedInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for logging access.", e);
                Thread.currentThread().interrupt();
            }
        }

        loggedInProgress = true;

        try {
            List<AuditLog> logs = readAuditLogsInternal();
            logs.add(auditLog);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(AUDIT_LOG_FILE))) {
                out.writeObject(logs);
            } catch (IOException e) {
                log.error("Error writing audit log: {}", e.getMessage(), e);
            }
        } finally {
            loggedInProgress = false;
            notifyAll();
        }
    }

    /**
     * Sinkronizirano čita sve zapise iz binarne datoteke.
     * Ako je druga nit u procesu pisanja, ova će pričekati.
     *
     * @return Lista svih {@link AuditLog} zapisa.
     */
    public synchronized List<AuditLog> readAuditLogs() {
        while (loggedInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for logging access.", e);
                Thread.currentThread().interrupt();
            }
        }

        loggedInProgress = true;

        try {
            return readAuditLogsInternal();
        } finally {
            loggedInProgress = false;
            notifyAll();
        }
    }

    /**
     * Interna, nesinkronizirana metoda za čitanje koju pozivaju sinkronizirane metode.
     * Čita listu {@link AuditLog} objekata iz datoteke.
     *
     * @return Lista zapisa ili prazna lista ako datoteka ne postoji, prazna je, ili dođe do greške.
     */
    private List<AuditLog> readAuditLogsInternal() {
        File file = new File(AUDIT_LOG_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<AuditLog>) in.readObject();
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading audit log: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
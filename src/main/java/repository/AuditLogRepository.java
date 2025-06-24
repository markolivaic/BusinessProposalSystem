package repository;

import model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    private static final String AUDIT_LOG_FILE = "dat/audit_log.dat";
    private static final Logger log = LoggerFactory.getLogger(AuditLogRepository.class);

    // Zastavica koja, kao i kod profesora, osigurava da samo jedna nit pristupa resursu
    private static boolean loggedInProgress = false;

    /**
     * Sinkronizirano zapisuje promjenu u datoteku. Ako je druga nit već u procesu
     * pisanja, ova će pričekati svoj red.
     * @param auditLog Zapis koji se sprema.
     */
    public synchronized void logChange(AuditLog auditLog) {
        // "Profesorski" mehanizam čekanja dok je resurs zauzet
        while (loggedInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for logging access.", e);
                Thread.currentThread().interrupt(); // Dobra praksa je ponovno postaviti interrupt status
            }
        }

        loggedInProgress = true;

        try {
            // Koristimo internu metodu za čitanje kako bismo izbjegli rekurzivnu sinkronizaciju i deadlock
            List<AuditLog> logs = readAuditLogsInternal();
            logs.add(auditLog);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(AUDIT_LOG_FILE))) {
                out.writeObject(logs);
            } catch (IOException e) {
                log.error("Error writing audit log: {}", e.getMessage(), e);
            }
        } finally {
            // Uvijek oslobađamo resurs i obavještavamo druge niti da mogu nastaviti
            loggedInProgress = false;
            notifyAll();
        }
    }

    /**
     * Sinkronizirano čita sve zapise iz datoteke.
     * @return Lista svih zapisa.
     */
    public synchronized List<AuditLog> readAuditLogs() {
        // "Profesorski" mehanizam čekanja
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
     * @return Lista zapisa.
     */
    private List<AuditLog> readAuditLogsInternal() {
        File file = new File(AUDIT_LOG_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<AuditLog>) in.readObject();
        } catch (EOFException e) {
            // Ovo je očekivano ako je datoteka prazna, nije greška.
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading audit log: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // Metode logChangeAsync() i shutdown() su uklonjene jer više ne koristimo ExecutorService.
}
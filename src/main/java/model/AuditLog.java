package model;

import java.io.Serializable;

/**
 * Predstavlja nepromjenjivi zapis (record) o jednoj promjeni u sustavu (audit log).
 * Implementira {@link Serializable} kako bi se instance mogle spremati u binarnu datoteku.
 *
 * @param id Jedinstveni identifikator zapisa.
 * @param userId ID korisnika koji je izvršio promjenu.
 * @param userRole Rola korisnika koji je izvršio promjenu.
 * @param action Vrsta akcije (npr. ADD, UPDATE, DELETE).
 * @param entityName Naziv entiteta na kojem je izvršena promjena.
 * @param oldValue Stara vrijednost podatka (kao String).
 * @param newValue Nova vrijednost podatka (kao String).
 * @param timestamp Vrijeme kada se promjena dogodila.
 */
public record AuditLog(
        Long id,
        Long userId,
        String userRole,
        String action,
        String entityName,
        String oldValue,
        String newValue,
        String timestamp
) implements Serializable {}
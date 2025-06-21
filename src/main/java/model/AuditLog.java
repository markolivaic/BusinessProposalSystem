package model;

import java.io.Serializable;

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

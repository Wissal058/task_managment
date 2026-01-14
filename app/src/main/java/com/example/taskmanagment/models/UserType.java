package com.example.taskmanagment.models;

/**
 * Énumération représentant les types d'utilisateurs dans le système
 */
public enum UserType {
    ADMIN("Administrateur"),
    EMPLOYEE("Employé");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convertir une chaîne en UserType
     */
    public static UserType fromString(String text) {
        for (UserType type : UserType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return EMPLOYEE; // Valeur par défaut
    }
}
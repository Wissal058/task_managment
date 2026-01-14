package com.example.taskmanagment.models;

/**
 * Énumération représentant les priorités possibles d'une tâche
 */
public enum TaskPriority {
    LOW("Faible"),
    MEDIUM("Moyenne"),
    HIGH("Haute"),
    URGENT("Urgente");

    private final String displayName;

    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convertir une chaîne en TaskPriority
     */
    public static TaskPriority fromString(String text) {
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.name().equalsIgnoreCase(text)) {
                return priority;
            }
        }
        return MEDIUM; // Valeur par défaut
    }
}

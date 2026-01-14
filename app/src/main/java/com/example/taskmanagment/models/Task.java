package com.example.taskmanagment.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modèle représentant une tâche dans le système
 * Implémente Serializable pour pouvoir être passé entre activités
 */
public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String assignedTo;
    private String createdBy;
    private TaskStatus status;
    private TaskPriority priority;
    private long createdDate;
    private long dueDate;
    private long completedDate;

    // Constructeur vide
    public Task() {
        this.createdDate = System.currentTimeMillis();
        this.status = TaskStatus.PENDING;
        this.priority = TaskPriority.MEDIUM;
    }

    // Constructeur complet
    public Task(String id, String title, String description, String assignedTo,
                String createdBy, TaskStatus status, TaskPriority priority,
                long dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.status = status;
        this.priority = priority;
        this.createdDate = System.currentTimeMillis();
        this.dueDate = dueDate;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public long getDueDate() {
        return dueDate;
    }

    public long getCompletedDate() {
        return completedDate;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.COMPLETED && completedDate == 0) {
            this.completedDate = System.currentTimeMillis();
        }
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompletedDate(long completedDate) {
        this.completedDate = completedDate;
    }

    /**
     * Obtenir la date de création formatée
     */
    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdDate));
    }

    /**
     * Obtenir la date d'échéance formatée
     */
    public String getFormattedDueDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(dueDate));
    }

    /**
     * Vérifier si la tâche est en retard
     */
    public boolean isOverdue() {
        return System.currentTimeMillis() > dueDate &&
                status != TaskStatus.COMPLETED &&
                status != TaskStatus.CANCELLED;
    }

    /**
     * Vérifier si la tâche est terminée
     */
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", assignedTo='" + assignedTo + '\'' +
                '}';
    }
}
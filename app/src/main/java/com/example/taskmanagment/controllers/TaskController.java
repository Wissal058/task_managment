package com.example.taskmanagment.controllers;

import android.content.Context;


import com.example.taskmanagment.database.TaskDAO;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur de gestion des tâches
 * Gère toutes les opérations liées aux tâches
 */
public class TaskController {
    private TaskDAO taskDAO;
    private Context context;

    public TaskController(Context context) {
        this.context = context;
        this.taskDAO = new TaskDAO(context);
    }

    /**
     * Obtient toutes les tâches
     */
    public List<Task> getAllTasks() {
        return taskDAO.getAllTasks();
    }

    /**
     * Obtient une tâche par son ID
     */
    public Task getTaskById(String taskId) {
        return taskDAO.getTaskById(taskId);
    }

    /**
     * Obtient les tâches d'un utilisateur
     */
    public List<Task> getTasksByUser(String userId) {
        return taskDAO.getTasksByUser(userId);
    }

    /**
     * Obtient les tâches créées par un utilisateur
     */
    public List<Task> getTasksCreatedBy(String userId) {
        return taskDAO.getTasksCreatedBy(userId);
    }

    /**
     * Obtient les tâches par statut
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskDAO.getTasksByStatus(status);
    }

    /**
     * Obtient les tâches en retard
     */
    public List<Task> getOverdueTasks() {
        return taskDAO.getOverdueTasks();
    }

    /**
     * Obtient les tâches en retard d'un utilisateur
     */
    public List<Task> getOverdueTasksByUser(String userId) {
        return taskDAO.getOverdueTasksByUser(userId);
    }

    /**
     * Crée une nouvelle tâche
     */
    public boolean createTask(String title, String description, String assignedTo,
                              String createdBy, TaskPriority priority, long dueDate) {
        Task task = new Task();
        task.setId(generateTaskId());
        task.setTitle(title);
        task.setDescription(description);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(priority);
        task.setCreatedDate(System.currentTimeMillis());
        task.setDueDate(dueDate);

        return taskDAO.insertTask(task);
    }

    /**
     * Crée une nouvelle tâche avec un objet Task
     */
    public boolean createTask(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(generateTaskId());
        }
        return taskDAO.insertTask(task);
    }

    /**
     * Met à jour une tâche
     */
    public boolean updateTask(Task task) {
        return taskDAO.updateTask(task);
    }

    /**
     * Met à jour le statut d'une tâche
     */
    public boolean updateTaskStatus(String taskId, TaskStatus status) {
        return taskDAO.updateTaskStatus(taskId, status);
    }

    /**
     * Met à jour la priorité d'une tâche
     */
    public boolean updateTaskPriority(String taskId, TaskPriority priority) {
        return taskDAO.updateTaskPriority(taskId, priority);
    }

    /**
     * Marque une tâche comme terminée
     */
    public boolean completeTask(String taskId) {
        return updateTaskStatus(taskId, TaskStatus.COMPLETED);
    }

    /**
     * Annule une tâche
     */
    public boolean cancelTask(String taskId) {
        return updateTaskStatus(taskId, TaskStatus.CANCELLED);
    }

    /**
     * Démarre le travail sur une tâche
     */
    public boolean startTask(String taskId) {
        return updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    /**
     * Supprime une tâche
     */
    public boolean deleteTask(String taskId) {
        return taskDAO.deleteTask(taskId);
    }

    /**
     * Obtient les tâches triées par date
     */
    public List<Task> getTasksSortedByDate() {
        return taskDAO.getTasksSortedByDate();
    }

    /**
     * Obtient les tâches triées par date d'échéance
     */
    public List<Task> getTasksSortedByDueDate() {
        return taskDAO.getTasksSortedByDueDate();
    }

    /**
     * Obtient les tâches triées par priorité
     */
    public List<Task> getTasksSortedByPriority() {
        return taskDAO.getTasksSortedByPriority();
    }

    /**
     * Obtient les statistiques des tâches d'un utilisateur
     */
    public TaskDAO.TaskStatistics getUserTaskStatistics(String userId) {
        return taskDAO.getUserTaskStatistics(userId);
    }

    /**
     * Réassigne une tâche à un autre utilisateur
     */
    public boolean reassignTask(String taskId, String newAssignedTo) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setAssignedTo(newAssignedTo);
            return updateTask(task);
        }
        return false;
    }

    /**
     * Génère un ID unique pour une tâche
     */
    private String generateTaskId() {
        return "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Compte le nombre total de tâches
     */
    public int getTotalTaskCount() {
        return taskDAO.getTotalTaskCount();
    }

    /**
     * Compte les tâches par statut
     */
    public int getTaskCountByStatus(TaskStatus status) {
        return taskDAO.getTaskCountByStatus(status);
    }
}

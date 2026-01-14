package com.example.taskmanagment.database;

import android.content.Context;

import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Data Access Object pour les tâches
 * Design Pattern: DAO (Data Access Object)
 */
public class TaskDAO {
    private XMLDatabaseManager dbManager;

    public TaskDAO(Context context) {
        this.dbManager = XMLDatabaseManager.getInstance(context);
    }

    /**
     * Récupère toutes les tâches
     */
    public List<Task> getAllTasks() {
        return dbManager.getAllTasks();
    }

    /**
     * Récupère une tâche par son ID
     */
    public Task getTaskById(String taskId) {
        return dbManager.getTaskById(taskId);
    }

    /**
     * Récupère les tâches assignées à un utilisateur
     */
    public List<Task> getTasksByUser(String userId) {
        List<Task> allTasks = dbManager.getAllTasks();
        List<Task> userTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getAssignedTo().equals(userId)) {
                userTasks.add(task);
            }
        }

        return userTasks;
    }

    /**
     * Récupère les tâches créées par un utilisateur
     */
    public List<Task> getTasksCreatedBy(String userId) {
        List<Task> allTasks = dbManager.getAllTasks();
        List<Task> createdTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getCreatedBy().equals(userId)) {
                createdTasks.add(task);
            }
        }

        return createdTasks;
    }

    /**
     * Récupère les tâches par statut
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        List<Task> allTasks = dbManager.getAllTasks();
        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getStatus() == status) {
                filteredTasks.add(task);
            }
        }

        return filteredTasks;
    }

    /**
     * Récupère les tâches par priorité
     */
    public List<Task> getTasksByPriority(TaskPriority priority) {
        List<Task> allTasks = dbManager.getAllTasks();
        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getPriority() == priority) {
                filteredTasks.add(task);
            }
        }

        return filteredTasks;
    }

    /**
     * Récupère les tâches en retard
     */
    public List<Task> getOverdueTasks() {
        List<Task> allTasks = dbManager.getAllTasks();
        List<Task> overdueTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.isOverdue()) {
                overdueTasks.add(task);
            }
        }

        return overdueTasks;
    }

    /**
     * Récupère les tâches en retard d'un utilisateur
     */
    public List<Task> getOverdueTasksByUser(String userId) {
        List<Task> userTasks = getTasksByUser(userId);
        List<Task> overdueTasks = new ArrayList<>();

        for (Task task : userTasks) {
            if (task.isOverdue()) {
                overdueTasks.add(task);
            }
        }

        return overdueTasks;
    }

    /**
     * Insère une nouvelle tâche
     */
    public boolean insertTask(Task task) {
        return dbManager.addTask(task);
    }

    /**
     * Met à jour une tâche existante
     */
    public boolean updateTask(Task task) {
        return dbManager.updateTask(task);
    }

    /**
     * Met à jour le statut d'une tâche
     */
    public boolean updateTaskStatus(String taskId, TaskStatus newStatus) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setStatus(newStatus);
            return updateTask(task);
        }
        return false;
    }

    /**
     * Met à jour la priorité d'une tâche
     */
    public boolean updateTaskPriority(String taskId, TaskPriority newPriority) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setPriority(newPriority);
            return updateTask(task);
        }
        return false;
    }

    /**
     * Supprime une tâche
     */
    public boolean deleteTask(String taskId) {
        return dbManager.deleteTask(taskId);
    }

    /**
     * Trie les tâches par date de création (plus récentes en premier)
     */
    public List<Task> getTasksSortedByDate() {
        List<Task> tasks = new ArrayList<>(getAllTasks());
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Long.compare(t2.getCreatedDate(), t1.getCreatedDate());
            }
        });
        return tasks;
    }

    /**
     * Trie les tâches par date d'échéance
     */
    public List<Task> getTasksSortedByDueDate() {
        List<Task> tasks = new ArrayList<>(getAllTasks());
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Long.compare(t1.getDueDate(), t2.getDueDate());
            }
        });
        return tasks;
    }

    /**
     * Trie les tâches par priorité
     */
    public List<Task> getTasksSortedByPriority() {
        List<Task> tasks = new ArrayList<>(getAllTasks());
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Integer.compare(t2.getPriority().ordinal(), t1.getPriority().ordinal());
            }
        });
        return tasks;
    }

    /**
     * Compte le nombre total de tâches
     */
    public int getTotalTaskCount() {
        return getAllTasks().size();
    }

    /**
     * Compte les tâches par statut
     */
    public int getTaskCountByStatus(TaskStatus status) {
        return getTasksByStatus(status).size();
    }

    /**
     * Compte les tâches d'un utilisateur
     */
    public int getTaskCountByUser(String userId) {
        return getTasksByUser(userId).size();
    }

    /**
     * Obtient les statistiques des tâches d'un utilisateur
     */
    public TaskStatistics getUserTaskStatistics(String userId) {
        List<Task> userTasks = getTasksByUser(userId);

        int pending = 0;
        int inProgress = 0;
        int completed = 0;
        int cancelled = 0;
        int overdue = 0;

        for (Task task : userTasks) {
            switch (task.getStatus()) {
                case PENDING:
                    pending++;
                    break;
                case IN_PROGRESS:
                    inProgress++;
                    break;
                case COMPLETED:
                    completed++;
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
            }

            if (task.isOverdue()) {
                overdue++;
            }
        }

        return new TaskStatistics(userTasks.size(), pending, inProgress, completed, cancelled, overdue);
    }

    /**
     * Classe interne pour les statistiques de tâches
     */
    public static class TaskStatistics {
        public final int total;
        public final int pending;
        public final int inProgress;
        public final int completed;
        public final int cancelled;
        public final int overdue;

        public TaskStatistics(int total, int pending, int inProgress, int completed, int cancelled, int overdue) {
            this.total = total;
            this.pending = pending;
            this.inProgress = inProgress;
            this.completed = completed;
            this.cancelled = cancelled;
            this.overdue = overdue;
        }
    }
}
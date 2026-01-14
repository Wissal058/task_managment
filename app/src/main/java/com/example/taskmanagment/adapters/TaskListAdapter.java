package com.example.taskmanagment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;

import java.util.List;

/**
 * Adapter pour afficher la liste des tâches dans un RecyclerView
 * Design Pattern: Adapter + ViewHolder
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> tasks;
    private OnTaskClickListener listener;
    private UserController userController;

    /**
     * Interface pour gérer les clics sur les items
     */
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskListAdapter(Context context, List<Task> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.userController = new UserController(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    /**
     * Met à jour la liste des tâches
     */
    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder pour les items de tâches
     * Design Pattern: ViewHolder
     */
    class TaskViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTaskTitle;
        private TextView tvTaskDescription;
        private TextView tvStatus;
        private TextView tvPriority;
        private TextView tvDueDate;
        private TextView tvAssignedTo;
        private View viewStatusIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);

            // Gérer le clic sur l'item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }

        public void bind(Task task) {
            // Titre
            tvTaskTitle.setText(task.getTitle());

            // Description
            tvTaskDescription.setText(task.getDescription());

            // Statut
            tvStatus.setText(getStatusText(task.getStatus()));
            setStatusColor(task.getStatus());

            // Priorité
            tvPriority.setText(getPriorityText(task.getPriority()));
            setPriorityColor(task.getPriority());

            // Date d'échéance
            tvDueDate.setText(task.getFormattedDueDate());

            // Colorer en rouge si en retard
            if (task.isOverdue()) {
                tvDueDate.setTextColor(context.getResources().getColor(R.color.error));
            } else {
                tvDueDate.setTextColor(context.getResources().getColor(R.color.textSecondary));
            }

            // Utilisateur assigné
            User assignedUser = userController.getUserById(task.getAssignedTo());
            if (assignedUser != null) {
                tvAssignedTo.setText(context.getString(R.string.assigned_to) + ": " +
                        assignedUser.getFullName());
            } else {
                tvAssignedTo.setText("");
            }
        }

        /**
         * Obtient le texte du statut traduit
         */
        private String getStatusText(TaskStatus status) {
            switch (status) {
                case PENDING:
                    return context.getString(R.string.status_pending);
                case IN_PROGRESS:
                    return context.getString(R.string.status_in_progress);
                case COMPLETED:
                    return context.getString(R.string.status_completed);
                case CANCELLED:
                    return context.getString(R.string.status_cancelled);
                default:
                    return "";
            }
        }

        /**
         * Obtient le texte de la priorité traduit
         */
        private String getPriorityText(TaskPriority priority) {
            switch (priority) {
                case LOW:
                    return context.getString(R.string.priority_low);
                case MEDIUM:
                    return context.getString(R.string.priority_medium);
                case HIGH:
                    return context.getString(R.string.priority_high);
                case URGENT:
                    return context.getString(R.string.priority_urgent);
                default:
                    return "";
            }
        }

        /**
         * Définit la couleur de l'indicateur de statut
         */
        private void setStatusColor(TaskStatus status) {
            int color;
            switch (status) {
                case PENDING:
                    color = context.getResources().getColor(R.color.statusPending);
                    break;
                case IN_PROGRESS:
                    color = context.getResources().getColor(R.color.statusInProgress);
                    break;
                case COMPLETED:
                    color = context.getResources().getColor(R.color.statusCompleted);
                    break;
                case CANCELLED:
                    color = context.getResources().getColor(R.color.statusCancelled);
                    break;
                default:
                    color = context.getResources().getColor(R.color.textSecondary);
            }

            viewStatusIndicator.setBackgroundColor(color);
        }

        /**
         * Définit la couleur du badge de priorité
         */
        private void setPriorityColor(TaskPriority priority) {
            int color;
            int backgroundColor;

            switch (priority) {
                case LOW:
                    color = context.getResources().getColor(R.color.textWhite);
                    backgroundColor = context.getResources().getColor(R.color.priorityLow);
                    break;
                case MEDIUM:
                    color = context.getResources().getColor(R.color.textPrimary);
                    backgroundColor = context.getResources().getColor(R.color.priorityMedium);
                    break;
                case HIGH:
                    color = context.getResources().getColor(R.color.textWhite);
                    backgroundColor = context.getResources().getColor(R.color.priorityHigh);
                    break;
                case URGENT:
                    color = context.getResources().getColor(R.color.textWhite);
                    backgroundColor = context.getResources().getColor(R.color.priorityUrgent);
                    break;
                default:
                    color = context.getResources().getColor(R.color.textSecondary);
                    backgroundColor = context.getResources().getColor(R.color.gray_light);
            }

            tvPriority.setTextColor(color);
            tvPriority.setBackgroundColor(backgroundColor);
        }
    }
}

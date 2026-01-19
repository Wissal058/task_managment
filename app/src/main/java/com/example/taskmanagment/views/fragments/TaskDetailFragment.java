package com.example.taskmanagment.views.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.XMLWriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private static final String ARG_TASK_ID = "TASK_ID";

    public static TaskDetailFragment newInstance(String taskId) {
        TaskDetailFragment f = new TaskDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TASK_ID, taskId);
        f.setArguments(b);
        return f;
    }

    private TaskController taskController;
    private UserController userController;
    private AuthController authController;
    private User currentUser;

    // Views
    private TextView tvTitle, tvOverdueIndicator;
    private TextView tvStatus, tvPriority, tvDescription;
    private TextView tvCreatedDate, tvDueDate;
    private TextView tvAssignedTo, tvCreatedBy;

    private View cardStatus, cardPriority;

    private Button btnStartTask, btnCompleteTask, btnCancelTask;
    private Button btnEditTask, btnDeleteTask;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private Task currentTask;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_task_detail, container, false);

        // Controllers
        taskController = new TaskController(requireContext());
        userController = new UserController(requireContext());
        authController = new AuthController(requireContext());
        currentUser = authController.getCurrentUser();

        bindViews(v);

        String taskId = getArguments() != null ? getArguments().getString(ARG_TASK_ID) : null;
        currentTask = findTaskById(taskId);

        if (currentTask == null) {
            tvTitle.setText(getString(R.string.task_not_found));
            return v;
        }

        fillUI(currentTask);
        setupButtons();

        return v;
    }

    private void bindViews(View v) {
        tvTitle = v.findViewById(R.id.tvTitle);
        tvOverdueIndicator = v.findViewById(R.id.tvOverdueIndicator);

        tvStatus = v.findViewById(R.id.tvStatus);
        tvPriority = v.findViewById(R.id.tvPriority);
        tvDescription = v.findViewById(R.id.tvDescription);

        tvCreatedDate = v.findViewById(R.id.tvCreatedDate);
        tvDueDate = v.findViewById(R.id.tvDueDate);

        tvAssignedTo = v.findViewById(R.id.tvAssignedTo);
        tvCreatedBy = v.findViewById(R.id.tvCreatedBy);

        cardStatus = v.findViewById(R.id.cardStatus);
        cardPriority = v.findViewById(R.id.cardPriority);

        btnStartTask = v.findViewById(R.id.btnStartTask);
        btnCompleteTask = v.findViewById(R.id.btnCompleteTask);
        btnCancelTask = v.findViewById(R.id.btnCancelTask);

        btnEditTask = v.findViewById(R.id.btnEditTask);
        btnDeleteTask = v.findViewById(R.id.btnDeleteTask);
    }

    private Task findTaskById(String id) {
        if (id == null) return null;
        List<Task> tasks = taskController.getAllTasks();
        for (Task t : tasks) {
            if (id.equals(t.getId())) return t;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fillUI(Task t) {
        tvTitle.setText(nonNull(t.getTitle(), "-"));
        tvDescription.setText(nonNull(t.getDescription(), "-"));

        // Dates
        tvCreatedDate.setText(formatDate(t.getCreatedDate()));
        tvDueDate.setText(formatDate(t.getDueDate()));

        // Users
        User assigned = userController.getUserById(t.getAssignedTo());
        User created = userController.getUserById(t.getCreatedBy());

        tvAssignedTo.setText(assigned != null ? assigned.getFullName() : ("userId=" + t.getAssignedTo()));
        tvCreatedBy.setText(created != null ? created.getFullName() : ("userId=" + t.getCreatedBy()));

        // Status / Priority
        TaskStatus status = t.getStatus();
        TaskPriority priority = t.getPriority();

        tvStatus.setText(status != null ? status.getDisplayName() : "-");
        tvPriority.setText(priority != null ? priority.getDisplayName() : "-");

        // Couleurs sur card (statut/priorité)
        applyStatusStyle(status);
        applyPriorityStyle(priority);

        // Indicateur de retard
        boolean overdue = isOverdue(t);
        tvOverdueIndicator.setVisibility(overdue ? View.VISIBLE : View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupButtons() {
        // Tout cacher au début
        hideAllActionButtons();

        if (currentUser == null) return;

        boolean isAdmin = currentUser.isAdmin();

        if (isAdmin) {
            // Admin: boutons éditer/supprimer
            btnEditTask.setVisibility(View.VISIBLE);
            btnDeleteTask.setVisibility(View.VISIBLE);

            btnEditTask.setOnClickListener(v -> {
                // Si tu as un EditTaskFragment, tu peux le lancer ici
                // Exemple :
                // requireActivity().getSupportFragmentManager()
                //     .beginTransaction()
                //     .replace(R.id.fragment_container, EditTaskFragment.newInstance(currentTask.getId()))
                //     .addToBackStack("edit_task")
                //     .commit();
            });

            btnDeleteTask.setOnClickListener(v -> deleteTask());

        } else {
            // Employé: actions selon statut
            TaskStatus st = currentTask.getStatus();

            // start si pending
            if (st == TaskStatus.PENDING) {
                btnStartTask.setVisibility(View.VISIBLE);
                btnStartTask.setOnClickListener(v -> updateStatus(TaskStatus.IN_PROGRESS));
            }

            // complete si in progress
            if (st == TaskStatus.IN_PROGRESS) {
                btnCompleteTask.setVisibility(View.VISIBLE);
                btnCompleteTask.setOnClickListener(v -> updateStatus(TaskStatus.COMPLETED));
            }

            // cancel (si tu supportes CANCELLED, sinon tu peux le retirer)
            // Si tu n’as pas TaskStatus.CANCELLED dans ton enum, commente ce bloc
            try {
                TaskStatus cancelled = TaskStatus.valueOf("CANCELLED");
                if (st == TaskStatus.PENDING || st == TaskStatus.IN_PROGRESS) {
                    btnCancelTask.setVisibility(View.VISIBLE);
                    btnCancelTask.setOnClickListener(v -> updateStatus(cancelled));
                }
            } catch (Exception ignored) {
                // pas de CANCELLED dans l'enum -> on ne montre pas
            }
        }
    }

    private void hideAllActionButtons() {
        btnStartTask.setVisibility(View.GONE);
        btnCompleteTask.setVisibility(View.GONE);
        btnCancelTask.setVisibility(View.GONE);

        btnEditTask.setVisibility(View.GONE);
        btnDeleteTask.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateStatus(TaskStatus newStatus) {
        List<Task> tasks = taskController.getAllTasks();

        for (Task t : tasks) {
            if (t.getId().equals(currentTask.getId())) {
                t.setStatus(newStatus);

                if (newStatus == TaskStatus.COMPLETED) {
                    t.setCompletedDate(System.currentTimeMillis());
                }
                break;
            }
        }

        // Écrire dans filesDir/tasks.xml
        XMLWriter.writeTasksToXML(requireContext(), tasks, "tasks.xml");

        // Recharger la tâche et rafraîchir UI
        currentTask = findTaskById(currentTask.getId());
        if (currentTask != null) {
            fillUI(currentTask);
            setupButtons();
        }
    }

    private void deleteTask() {
        List<Task> tasks = taskController.getAllTasks();
        Task toRemove = null;

        for (Task t : tasks) {
            if (t.getId().equals(currentTask.getId())) {
                toRemove = t;
                break;
            }
        }

        if (toRemove != null) tasks.remove(toRemove);

        XMLWriter.writeTasksToXML(requireContext(), tasks, "tasks.xml");

        // Retour au fragment précédent
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private boolean isOverdue(Task t) {
        if (t.getDueDate() <= 0) return false;
        if (t.getStatus() == TaskStatus.COMPLETED) return false;
        return System.currentTimeMillis() > t.getDueDate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void applyStatusStyle(TaskStatus status) {
        if (status == null) return;

        int color;
        switch (status) {
            case COMPLETED:
                color = requireContext().getColor(R.color.statusCompleted);
                break;
            case IN_PROGRESS:
                color = requireContext().getColor(R.color.statusInProgress);
                break;
            case PENDING:
            default:
                color = requireContext().getColor(R.color.statusPending);
                break;
        }

        // cardStatus est un CardView -> setCardBackgroundColor possible si cast
        if (cardStatus instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) cardStatus).setCardBackgroundColor(color);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void applyPriorityStyle(TaskPriority priority) {
        if (priority == null) return;

        int color;
        switch (priority) {
            case URGENT:
                color = requireContext().getColor(R.color.priorityUrgent);
                break;
            case HIGH:
                color = requireContext().getColor(R.color.priorityHigh);
                break;
            case MEDIUM:
                color = requireContext().getColor(R.color.priorityMedium);
                break;
            case LOW:
            default:
                color = requireContext().getColor(R.color.priorityLow);
                break;
        }

        if (cardPriority instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) cardPriority).setCardBackgroundColor(color);
        }
    }

    private String formatDate(long millis) {
        if (millis <= 0) return "-";
        return sdf.format(new Date(millis));
    }

    private String nonNull(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}

package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.adapters.TaskListAdapter;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.utils.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité pour afficher la liste des tâches
 * Supporte le filtrage par statut et priorité
 * Supporte le tri par date, échéance et priorité
 */
public class TaskListActivity extends AppCompatActivity implements TaskListAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewTasks;
    private TaskListAdapter adapter;
    private TextView tvNoTasks;
    private Spinner spinnerStatus, spinnerPriority, spinnerSort;

    private TaskController taskController;
    private AuthController authController;

    private List<Task> allTasks;
    private List<Task> filteredTasks;

    private boolean showAll;
    private String userId;

    private TaskStatus selectedStatus = null;
    private TaskPriority selectedPriority = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer la langue
        LanguageManager.applyLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Initialiser les contrôleurs
        taskController = new TaskController(this);
        authController = new AuthController(this);

        // Obtenir les paramètres
        showAll = getIntent().getBooleanExtra("show_all", false);
        userId = getIntent().getStringExtra("user_id");

        if (userId == null) {
            userId = authController.getCurrentUserId();
        }

        // Configurer la toolbar
        setupToolbar();

        // Initialiser les vues
        initViews();

        // Configurer le RecyclerView
        setupRecyclerView();

        // Configurer les spinners
        setupSpinners();

        // Charger les tâches
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les tâches quand on revient sur cette activité
        loadTasks();
    }

    /**
     * Configure la barre d'outils
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(showAll ? R.string.all_tasks : R.string.my_tasks);
        }
    }

    /**
     * Initialise les vues
     */
    private void initViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerSort = findViewById(R.id.spinnerSort);
    }

    /**
     * Configure le RecyclerView
     */
    private void setupRecyclerView() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskListAdapter(this, new ArrayList<>(), this);
        recyclerViewTasks.setAdapter(adapter);
    }

    /**
     * Configure les spinners de filtrage et tri
     */
    private void setupSpinners() {
        // Spinner de statut
        String[] statusOptions = {
                getString(R.string.all_tasks),
                getString(R.string.status_pending),
                getString(R.string.status_in_progress),
                getString(R.string.status_completed),
                getString(R.string.status_cancelled)
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedStatus = null;
                        break;
                    case 1:
                        selectedStatus = TaskStatus.PENDING;
                        break;
                    case 2:
                        selectedStatus = TaskStatus.IN_PROGRESS;
                        break;
                    case 3:
                        selectedStatus = TaskStatus.COMPLETED;
                        break;
                    case 4:
                        selectedStatus = TaskStatus.CANCELLED;
                        break;
                }
                filterTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner de priorité
        String[] priorityOptions = {
                getString(R.string.all_tasks),
                getString(R.string.priority_low),
                getString(R.string.priority_medium),
                getString(R.string.priority_high),
                getString(R.string.priority_urgent)
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, priorityOptions
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedPriority = null;
                        break;
                    case 1:
                        selectedPriority = TaskPriority.LOW;
                        break;
                    case 2:
                        selectedPriority = TaskPriority.MEDIUM;
                        break;
                    case 3:
                        selectedPriority = TaskPriority.HIGH;
                        break;
                    case 4:
                        selectedPriority = TaskPriority.URGENT;
                        break;
                }
                filterTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner de tri
        String[] sortOptions = {
                getString(R.string.sort) + " - " + getString(R.string.created_date),
                getString(R.string.sort) + " - " + getString(R.string.due_date),
                getString(R.string.sort) + " - " + getString(R.string.priority)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortTasks(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Charge les tâches depuis le contrôleur
     */
    private void loadTasks() {
        if (showAll) {
            allTasks = taskController.getAllTasks();
        } else {
            allTasks = taskController.getTasksByUser(userId);
        }

        filteredTasks = new ArrayList<>(allTasks);
        filterTasks();
    }

    /**
     * Filtre les tâches selon les critères sélectionnés
     */
    private void filterTasks() {
        filteredTasks = new ArrayList<>(allTasks);

        // Filtrer par statut
        if (selectedStatus != null) {
            List<Task> statusFiltered = new ArrayList<>();
            for (Task task : filteredTasks) {
                if (task.getStatus() == selectedStatus) {
                    statusFiltered.add(task);
                }
            }
            filteredTasks = statusFiltered;
        }

        // Filtrer par priorité
        if (selectedPriority != null) {
            List<Task> priorityFiltered = new ArrayList<>();
            for (Task task : filteredTasks) {
                if (task.getPriority() == selectedPriority) {
                    priorityFiltered.add(task);
                }
            }
            filteredTasks = priorityFiltered;
        }

        updateUI();
    }

    /**
     * Trie les tâches selon le critère sélectionné
     */
    private void sortTasks(int sortType) {
        switch (sortType) {
            case 0: // Date de création
                filteredTasks.sort((t1, t2) -> Long.compare(t2.getCreatedDate(), t1.getCreatedDate()));
                break;
            case 1: // Date d'échéance
                filteredTasks.sort((t1, t2) -> Long.compare(t1.getDueDate(), t2.getDueDate()));
                break;
            case 2: // Priorité
                filteredTasks.sort((t1, t2) -> Integer.compare(
                        getPriorityValue(t2.getPriority()),
                        getPriorityValue(t1.getPriority())
                ));
                break;
        }

        updateUI();
    }

    /**
     * Obtient une valeur numérique pour la priorité (pour le tri)
     */
    private int getPriorityValue(TaskPriority priority) {
        switch (priority) {
            case LOW:
                return 1;
            case MEDIUM:
                return 2;
            case HIGH:
                return 3;
            case URGENT:
                return 4;
            default:
                return 0;
        }
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        if (filteredTasks.isEmpty()) {
            recyclerViewTasks.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);
            adapter.updateTasks(filteredTasks);
        }
    }

    /**
     * Gère le clic sur une tâche
     */
    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, TaskDetailsActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
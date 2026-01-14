package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.LanguageManager;


/**
 * Tableau de bord de l'administrateur
 * View dans l'architecture MVC
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvTotalTasks, tvPendingTasks, tvInProgressTasks, tvCompletedTasks;
    private TextView tvTotalUsers, tvTotalEmployees;
    private Button btnViewAllTasks, btnManageUsers, btnCreateTask;

    private AuthController authController;
    private TaskController taskController;
    private UserController userController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer la langue
        LanguageManager.applyLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialiser les contrôleurs
        authController = new AuthController(this);
        taskController = new TaskController(this);
        userController = new UserController(this);

        // Obtenir l'utilisateur actuel
        currentUser = authController.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            // Rediriger vers login si pas d'admin
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Configurer la toolbar
        setupToolbar();

        // Initialiser les vues
        initViews();

        // Charger les statistiques
        loadStatistics();

        // Configurer les boutons
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les statistiques quand on revient sur cette activité
        loadStatistics();
    }

    /**
     * Configure la barre d'outils
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.dashboard);
        }
    }

    /**
     * Initialise les vues
     */
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvInProgressTasks = findViewById(R.id.tvInProgressTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        btnViewAllTasks = findViewById(R.id.btnViewAllTasks);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnCreateTask = findViewById(R.id.btnCreateTask);

        // Afficher le message de bienvenue
        tvWelcome.setText(getString(R.string.welcome) + ", " + currentUser.getFullName());
    }

    /**
     * Charge les statistiques
     */
    private void loadStatistics() {
        // Statistiques des tâches
        int totalTasks = taskController.getTotalTaskCount();
        int pendingTasks = taskController.getTaskCountByStatus(TaskStatus.PENDING);
        int inProgressTasks = taskController.getTaskCountByStatus(TaskStatus.IN_PROGRESS);
        int completedTasks = taskController.getTaskCountByStatus(TaskStatus.COMPLETED);

        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvPendingTasks.setText(String.valueOf(pendingTasks));
        tvInProgressTasks.setText(String.valueOf(inProgressTasks));
        tvCompletedTasks.setText(String.valueOf(completedTasks));

        // Statistiques des utilisateurs
        int totalUsers = userController.getTotalUserCount();
        int totalEmployees = userController.getEmployeeCount();

        tvTotalUsers.setText(String.valueOf(totalUsers));
        tvTotalEmployees.setText(String.valueOf(totalEmployees));
    }

    /**
     * Configure les boutons
     */
    private void setupButtons() {
        btnViewAllTasks.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            intent.putExtra("show_all", true);
            startActivity(intent);
        });

        btnManageUsers.setOnClickListener(v -> {
            // TODO: Implémenter UserListActivity
            Toast.makeText(this, "Gestion des utilisateurs à venir", Toast.LENGTH_SHORT).show();
        });

        btnCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            loadStatistics();
            Toast.makeText(this, R.string.refresh, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Affiche une boîte de dialogue pour confirmer la déconnexion
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    authController.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
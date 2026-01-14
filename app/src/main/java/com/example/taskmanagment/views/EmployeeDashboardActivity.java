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
import com.example.taskmanagment.database.TaskDAO;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.LanguageManager;

/**
 * Tableau de bord de l'employé
 * Affiche les statistiques personnelles et permet d'accéder aux tâches
 */
public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvMyTotalTasks, tvMyPendingTasks, tvMyInProgressTasks, tvMyCompletedTasks;
    private TextView tvOverdueTasks;
    private Button btnViewMyTasks, btnViewAllTasks;

    private AuthController authController;
    private TaskController taskController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer la langue
        LanguageManager.applyLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        // Initialiser les contrôleurs
        authController = new AuthController(this);
        taskController = new TaskController(this);

        // Obtenir l'utilisateur actuel
        currentUser = authController.getCurrentUser();
        if (currentUser == null || !currentUser.isEmployee()) {
            // Rediriger vers login si pas d'employé
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
        tvMyTotalTasks = findViewById(R.id.tvMyTotalTasks);
        tvMyPendingTasks = findViewById(R.id.tvMyPendingTasks);
        tvMyInProgressTasks = findViewById(R.id.tvMyInProgressTasks);
        tvMyCompletedTasks = findViewById(R.id.tvMyCompletedTasks);
        tvOverdueTasks = findViewById(R.id.tvOverdueTasks);
        btnViewMyTasks = findViewById(R.id.btnViewMyTasks);
        btnViewAllTasks = findViewById(R.id.btnViewAllTasks);

        // Afficher le message de bienvenue
        tvWelcome.setText(getString(R.string.welcome) + ", " + currentUser.getFullName());
    }

    /**
     * Charge les statistiques de l'employé
     */
    private void loadStatistics() {
        // Obtenir les statistiques des tâches de l'employé
        TaskDAO.TaskStatistics stats = taskController.getUserTaskStatistics(currentUser.getId());

        tvMyTotalTasks.setText(String.valueOf(stats.total));
        tvMyPendingTasks.setText(String.valueOf(stats.pending));
        tvMyInProgressTasks.setText(String.valueOf(stats.inProgress));
        tvMyCompletedTasks.setText(String.valueOf(stats.completed));
        tvOverdueTasks.setText(String.valueOf(stats.overdue));
    }

    /**
     * Configure les boutons
     */
    private void setupButtons() {
        // Bouton pour voir mes tâches
        btnViewMyTasks.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            intent.putExtra("user_id", currentUser.getId());
            intent.putExtra("show_all", false);
            startActivity(intent);
        });

        // Bouton pour voir toutes les tâches
        btnViewAllTasks.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            intent.putExtra("show_all", true);
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
        } else if (id == R.id.action_profile) {
            showProfile();
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

    /**
     * Affiche le profil de l'utilisateur
     */
    private void showProfile() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile)
                .setMessage(
                        getString(R.string.full_name) + ": " + currentUser.getFullName() + "\n" +
                                getString(R.string.username) + ": " + currentUser.getUsername() + "\n" +
                                getString(R.string.email) + ": " + currentUser.getEmail() + "\n" +
                                getString(R.string.user_type) + ": " + currentUser.getUserType().getDisplayName()
                )
                .setPositiveButton("OK", null)
                .show();
    }
}
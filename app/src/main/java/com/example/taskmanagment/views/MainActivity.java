package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.LanguageManager;
import com.example.taskmanagment.views.fragments.AddUserFragment;
import com.example.taskmanagment.views.fragments.AdminDashboardFragment;
import com.example.taskmanagment.views.fragments.CalendarFragment;
import com.example.taskmanagment.views.fragments.CreateTaskFragment;
import com.example.taskmanagment.views.fragments.DashboardFragment;
import com.example.taskmanagment.views.fragments.ProfileFragment;
import com.example.taskmanagment.views.fragments.SettingsFragment;
import com.example.taskmanagment.views.fragments.TaskListFragment;
import com.example.taskmanagment.views.fragments.TasksFragment;
import com.example.taskmanagment.views.fragments.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabActions;

    private AuthController authController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authController = new AuthController(this);
        currentUser = authController.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupToolbar();
        setupDrawer();
        setupBottomNavigation();

        fabActions.setOnClickListener(v -> showActionsBottomSheet());
        updateDrawerHeader();

        if (savedInstanceState == null) {
            loadInitialFragment();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabActions = findViewById(R.id.fab_actions);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.dashboard);
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Masquer certains items selon le type d'utilisateur
        Menu menu = navigationView.getMenu();

        // Exemple: create_task visible uniquement admin
        MenuItem createTaskItem = menu.findItem(R.id.nav_create_task);
        if (createTaskItem != null) {
            createTaskItem.setVisible(currentUser.isAdmin());
        }

        // all_tasks visible surtout admin (optionnel)
        MenuItem allTasksItem = menu.findItem(R.id.nav_all_tasks);
        if (allTasksItem != null) {
            allTasksItem.setVisible(currentUser.isAdmin());
        }
    }

    private void setupBottomNavigation() {
        // IMPORTANT: nav_placeholder est désactivé donc il n’envoie pas d’événement.
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(getHomeDashboard());
                safeCheckDrawerItem(R.id.nav_dashboard);
                return true;
            }

            if (id == R.id.nav_calendar) {
                loadFragment(getCalendar());
                safeCheckDrawerItem(R.id.nav_calendar);
                return true;
            }

            return false;
        });
    }

    private Fragment getHomeDashboard() {
        return currentUser.isAdmin() ? new AdminDashboardFragment() : new DashboardFragment();
    }

    private Fragment getCalendar() {
        return new CalendarFragment();
    }

    private void loadInitialFragment() {
        loadFragment(getHomeDashboard());
        safeCheckDrawerItem(R.id.nav_dashboard);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void updateDrawerHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
        TextView tvUserEmail = headerView.findViewById(R.id.tv_user_email);
        TextView tvUserType = headerView.findViewById(R.id.tv_user_type);

        if (tvUserName != null) tvUserName.setText(currentUser.getFullName());
        if (tvUserEmail != null) tvUserEmail.setText(currentUser.getEmail());
        if (tvUserType != null) tvUserType.setText(currentUser.getUserType().getDisplayName());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void safeCheckDrawerItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        if (item != null) navigationView.setCheckedItem(itemId);
    }

    // ---------- DRAWER CLICKS ----------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = null;

        if (itemId == R.id.nav_dashboard) {
            fragment = getHomeDashboard();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

        } else if (itemId == R.id.nav_create_task) {
            fragment = new CreateTaskFragment();

        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
            // bottom nav ne contient pas profile
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();

        } else if (itemId == R.id.nav_all_tasks) {
            fragment = new TasksFragment();

        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
        }
        else if (itemId == R.id.nav_all_users) {
            fragment = new UsersFragment();

        }

        if (fragment != null) loadFragment(fragment);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * IMPORTANT :
     * On crée le TaskListFragment en passant les infos via Bundle
     * (comme ça tu n’es pas dépendant d’une méthode newInstance qui peut ne pas exister)
     */
    private Fragment createTaskListFragment(boolean showAll, String userId) {
        TaskListFragment f = new TaskListFragment();
        Bundle b = new Bundle();
        b.putBoolean("SHOW_ALL", showAll);
        b.putString("USER_ID", userId);
        f.setArguments(b);
        return f;
    }

    // ---------- SETTINGS ----------
    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setItems(new String[]{
                        getString(R.string.change_language),
                        getString(R.string.change_password)
                }, (dialog, which) -> {
                    if (which == 0) {
                        showLanguageDialog();
                    } else {
                        Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.french), getString(R.string.arabic)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setItems(languages, (dialog, which) -> {
                    String langCode = (which == 0) ? LanguageManager.LANGUAGE_FRENCH : LanguageManager.LANGUAGE_ARABIC;
                    LanguageManager.setLocale(this, langCode);
                    recreate();
                })
                .show();
    }

    // ---------- LOGOUT ----------
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    authController.logout();
                    redirectToLogin();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    // ---------- BOTTOM SHEET ACTIONS ----------
    private void showActionsBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_actions, null);
        dialog.setContentView(view);

        View btnAddTask = view.findViewById(R.id.btn_add_task);
        View btnAddUser = view.findViewById(R.id.btn_add_user);

        btnAddTask.setOnClickListener(v -> {
            dialog.dismiss();
            loadFragment(new CreateTaskFragment());
            safeCheckDrawerItem(R.id.nav_create_task);
        });

        if (!currentUser.isAdmin()) {
            if (btnAddUser != null) btnAddUser.setVisibility(View.GONE);
        } else {
            if (btnAddUser != null) {
                btnAddUser.setOnClickListener(v -> {
                    dialog.dismiss();
                    loadFragment(new AddUserFragment());
                });
            }
        }

        dialog.show();
    }
}
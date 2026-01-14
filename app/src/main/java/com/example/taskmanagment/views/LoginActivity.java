package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.LanguageManager;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activité de connexion
 * View dans l'architecture MVC
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private Spinner spinnerLanguage;

    private AuthController authController;
    private UserController userController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authController = new AuthController(this);
        userController = new UserController(this);

        // Vérifier si déjà connecté
        if (authController.isLoggedIn()) {
            User currentUser = authController.getCurrentUser();
            if (currentUser != null) {
                navigateToDashboard(currentUser);
                finish();
                return;
            }
        }

        initViews();
        setupLanguageSpinner();
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    /**
     * Initialise les vues
     */
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
    }

    /**
     * Configure le sélecteur de langue
     */
    private void setupLanguageSpinner() {
        String[] languages = {
                getString(R.string.french),
                getString(R.string.arabic)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Sélectionner la langue actuelle
        String currentLanguage = LanguageManager.getLanguage(this);
        if (currentLanguage.equals(LanguageManager.LANGUAGE_ARABIC)) {
            spinnerLanguage.setSelection(1);
        } else {
            spinnerLanguage.setSelection(0);
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newLanguage = position == 0 ?
                        LanguageManager.LANGUAGE_FRENCH :
                        LanguageManager.LANGUAGE_ARABIC;

                String currentLang = LanguageManager.getLanguage(LoginActivity.this);
                if (!currentLang.equals(newLanguage)) {
                    LanguageManager.setLocale(LoginActivity.this, newLanguage);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Configure les écouteurs d'événements
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    /**
     * Gère la connexion
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation des champs
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.login_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Authentification
        User user = authController.authenticate(username, password);

        if (user != null) {
            Toast.makeText(this, getString(R.string.welcome) + " " + user.getFullName(),
                    Toast.LENGTH_SHORT).show();
            navigateToDashboard(user);
            finish();
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
            etPassword.setText("");
        }
    }

    /**
     * Navigue vers le tableau de bord approprié selon le type d'utilisateur
     */
    private void navigateToDashboard(User user) {
        Intent intent;

        if (user.isAdmin()) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, EmployeeDashboardActivity.class);
        }

        intent.putExtra("user", user);
        startActivity(intent);
    }
}
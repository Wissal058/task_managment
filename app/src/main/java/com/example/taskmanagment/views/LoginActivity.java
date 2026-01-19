package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.LanguageManager;
import com.example.taskmanagment.views.MainActivity;

/**
 * Activité de connexion
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private Spinner spinnerLanguage;

    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer la langue AVANT super.onCreate()
        LanguageManager.applyLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser le contrôleur
        authController = new AuthController(this);

        // Vérifier si déjà connecté
        if (authController.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialiser les vues
        initViews();

        // Configurer le spinner de langue
        setupLanguageSpinner();

        // Configurer le bouton de connexion
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    /**
     * Initialiser les vues
     */
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
    }

    /**
     * Configurer le spinner de langue
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

        // Changer la langue
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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Gérer la connexion
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUsername.setError(getString(R.string.field_required));
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.field_required));
            etPassword.requestFocus();
            return;
        }

        // Authentification
        User user = authController.authenticate(username, password);

        if (user != null) {
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        } else {
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Naviguer vers MainActivity (qui gère les fragments)
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
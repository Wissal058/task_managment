package com.example.taskmanagment.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.database.XMLDatabaseManager;
import com.example.taskmanagment.utils.LanguageManager;

/**
 * Écran de démarrage avec animation de chargement
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 secondes

    private ImageView imgLogo;
    private ImageView imgLoadingDot1, imgLoadingDot2, imgLoadingDot3;
    private TextView tvAppName, tvAppTagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer la langue
        LanguageManager.applyLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialiser les vues
        initViews();

        // Démarrer les animations
        startAnimations();

        // Initialiser la base de données en arrière-plan
        initializeDatabase();

        // Naviguer après le délai
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }

    /**
     * Initialiser les vues
     */
    private void initViews() {
        imgLogo = findViewById(R.id.imgLogo);
        imgLoadingDot1 = findViewById(R.id.imgLoadingDot1);
        imgLoadingDot2 = findViewById(R.id.imgLoadingDot2);
        imgLoadingDot3 = findViewById(R.id.imgLoadingDot3);
        tvAppName = findViewById(R.id.tvAppName);
        tvAppTagline = findViewById(R.id.tvAppTagline);
    }

    /**
     * Démarrer toutes les animations
     */
    private void startAnimations() {
        // Animation du logo (fade in + scale)
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation);
        imgLogo.startAnimation(logoAnimation);

        // Animation du texte (fade in)
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvAppName.startAnimation(fadeIn);
        tvAppTagline.startAnimation(fadeIn);

        // Animation des points de chargement (rotation avec délai)
        Animation rotateAnimation1 = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
        Animation rotateAnimation2 = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
        Animation rotateAnimation3 = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);

        // Décalage des animations pour effet de vague
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                imgLoadingDot1.startAnimation(rotateAnimation1), 0);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                imgLoadingDot2.startAnimation(rotateAnimation2), 200);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                imgLoadingDot3.startAnimation(rotateAnimation3), 400);
    }

    /**
     * Initialiser la base de données
     */
    private void initializeDatabase() {
        new Thread(() -> {
            // Initialiser XMLDatabaseManager (charge et valide les XML)
            XMLDatabaseManager.getInstance(this);
        }).start();
    }

    /**
     * Naviguer vers l'écran suivant
     */
    private void navigateToNextScreen() {
        AuthController authController = new AuthController(this);

        Intent intent;
        if (authController.isLoggedIn()) {
            // Si déjà connecté, aller directement à MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // Sinon, aller à LoginActivity
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();

        // Animation de transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
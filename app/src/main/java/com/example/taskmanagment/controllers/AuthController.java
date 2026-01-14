package com.example.taskmanagment.controllers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.taskmanagment.database.UserDAO;
import com.example.taskmanagment.models.User;


/**
 * Contrôleur d'authentification
 * Gère la connexion, déconnexion et la session utilisateur
 */
public class AuthController {
    private static final String PREF_NAME = "auth_preferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private Context context;
    private UserDAO userDAO;
    private SharedPreferences prefs;

    public AuthController(Context context) {
        this.context = context;
        this.userDAO = new UserDAO(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Authentifie un utilisateur
     */
    public User authenticate(String username, String password) {
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            // Sauvegarder la session
            saveSession(user);
        }

        return user;
    }

    /**
     * Sauvegarde la session de l'utilisateur
     */
    private void saveSession(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, user.getId());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtient l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        String userId = prefs.getString(KEY_USER_ID, null);
        if (userId != null) {
            return userDAO.getUserById(userId);
        }

        return null;
    }

    /**
     * Obtient l'ID de l'utilisateur connecté
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Change le mot de passe de l'utilisateur actuel
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        String userId = getCurrentUserId();
        if (userId != null) {
            return userDAO.changePassword(userId, oldPassword, newPassword);
        }
        return false;
    }

    /**
     * Vérifie si l'utilisateur actuel est un administrateur
     */
    public boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    /**
     * Vérifie si l'utilisateur actuel est un employé
     */
    public boolean isCurrentUserEmployee() {
        User user = getCurrentUser();
        return user != null && user.isEmployee();
    }
}

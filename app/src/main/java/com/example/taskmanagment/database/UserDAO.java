package com.example.taskmanagment.database;


import android.content.Context;

import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les utilisateurs
 * Design Pattern: DAO (Data Access Object)
 */
public class UserDAO {
    private XMLDatabaseManager dbManager;

    public UserDAO(Context context) {
        this.dbManager = XMLDatabaseManager.getInstance(context);
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return dbManager.getAllUsers();
    }

    /**
     * Récupère un utilisateur par son ID
     */
    public User getUserById(String userId) {
        return dbManager.getUserById(userId);
    }

    /**
     * Récupère un utilisateur par nom d'utilisateur
     */
    public User getUserByUsername(String username) {
        List<User> users = dbManager.getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Authentifie un utilisateur
     */
    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Récupère tous les utilisateurs d'un type spécifique
     */
    public List<User> getUsersByType(UserType userType) {
        List<User> allUsers = dbManager.getAllUsers();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getUserType() == userType) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }

    /**
     * Récupère tous les employés
     */
    public List<User> getAllEmployees() {
        return getUsersByType(UserType.EMPLOYEE);
    }

    /**
     * Récupère tous les administrateurs
     */
    public List<User> getAllAdmins() {
        return getUsersByType(UserType.ADMIN);
    }

    /**
     * Insère un nouvel utilisateur
     */
    public boolean insertUser(User user) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (getUserByUsername(user.getUsername()) != null) {
            return false;
        }
        return dbManager.addUser(user);
    }

    /**
     * Met à jour un utilisateur existant
     */
    public boolean updateUser(User user) {
        return dbManager.updateUser(user);
    }

    /**
     * Supprime un utilisateur
     */
    public boolean deleteUser(String userId) {
        return dbManager.deleteUser(userId);
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user != null && user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            return updateUser(user);
        }
        return false;
    }

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }

    /**
     * Compte le nombre total d'utilisateurs
     */
    public int getTotalUserCount() {
        return dbManager.getAllUsers().size();
    }

    /**
     * Compte le nombre d'employés
     */
    public int getEmployeeCount() {
        return getAllEmployees().size();
    }

    /**
     * Compte le nombre d'administrateurs
     */
    public int getAdminCount() {
        return getAllAdmins().size();
    }
}
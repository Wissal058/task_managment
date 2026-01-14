package com.example.taskmanagment.controllers;


import android.content.Context;


import com.example.taskmanagment.database.UserDAO;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;
import com.example.taskmanagment.utils.UserFactory;

import java.util.List;

/**
 * Contrôleur de gestion des utilisateurs
 * Gère toutes les opérations liées aux utilisateurs
 */
public class UserController {
    private UserDAO userDAO;
    private Context context;

    public UserController(Context context) {
        this.context = context;
        this.userDAO = new UserDAO(context);
    }

    /**
     * Obtient tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Obtient un utilisateur par son ID
     */
    public User getUserById(String userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Obtient un utilisateur par son nom d'utilisateur
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * Obtient tous les employés
     */
    public List<User> getAllEmployees() {
        return userDAO.getAllEmployees();
    }

    /**
     * Obtient tous les administrateurs
     */
    public List<User> getAllAdmins() {
        return userDAO.getAllAdmins();
    }

    /**
     * Crée un nouvel utilisateur en utilisant le Factory Pattern
     */
    public boolean createUser(UserType userType, String username, String password,
                              String email, String fullName) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (userDAO.usernameExists(username)) {
            return false;
        }

        // Utiliser le Factory pour créer l'utilisateur
        User user = UserFactory.createUser(userType, username, password, email, fullName);

        return userDAO.insertUser(user);
    }

    /**
     * Crée un nouvel administrateur
     */
    public boolean createAdmin(String username, String password, String email, String fullName) {
        return createUser(UserType.ADMIN, username, password, email, fullName);
    }

    /**
     * Crée un nouvel employé
     */
    public boolean createEmployee(String username, String password, String email, String fullName) {
        return createUser(UserType.EMPLOYEE, username, password, email, fullName);
    }

    /**
     * Met à jour un utilisateur
     */
    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    /**
     * Supprime un utilisateur
     */
    public boolean deleteUser(String userId) {
        return userDAO.deleteUser(userId);
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        return userDAO.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }

    /**
     * Obtient le nombre total d'utilisateurs
     */
    public int getTotalUserCount() {
        return userDAO.getTotalUserCount();
    }

    /**
     * Obtient le nombre d'employés
     */
    public int getEmployeeCount() {
        return userDAO.getEmployeeCount();
    }

    /**
     * Obtient le nombre d'administrateurs
     */
    public int getAdminCount() {
        return userDAO.getAdminCount();
    }

    /**
     * Met à jour le profil d'un utilisateur
     */
    public boolean updateUserProfile(String userId, String email, String fullName) {
        User user = getUserById(userId);
        if (user != null) {
            user.setEmail(email);
            user.setFullName(fullName);
            return updateUser(user);
        }
        return false;
    }

    /**
     * Valide les informations d'un utilisateur
     */
    public boolean validateUserInfo(String username, String password, String email) {
        // Vérifier que les champs ne sont pas vides
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            return false;
        }

        // Vérifier la longueur du nom d'utilisateur
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }

        // Vérifier la longueur du mot de passe
        if (password.length() < 6) {
            return false;
        }

        // Vérifier le format de l'email (basique)
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }

        return true;
    }
}

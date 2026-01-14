package com.example.taskmanagment.utils;


import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;

import java.util.UUID;

/**
 * Factory pour créer des utilisateurs
 * Design Pattern: Factory Method
 */
public class UserFactory {

    /**
     * Crée un nouvel utilisateur administrateur
     */
    public static User createAdmin(String username, String password, String email, String fullName) {
        User user = new User();
        user.setId(generateUserId());
        user.setUsername(username);
        user.setPassword(password);
        user.setUserType(UserType.ADMIN);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setCreatedDate(System.currentTimeMillis());

        return user;
    }

    /**
     * Crée un nouvel utilisateur employé
     */
    public static User createEmployee(String username, String password, String email, String fullName) {
        User user = new User();
        user.setId(generateUserId());
        user.setUsername(username);
        user.setPassword(password);
        user.setUserType(UserType.EMPLOYEE);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setCreatedDate(System.currentTimeMillis());

        return user;
    }

    /**
     * Crée un utilisateur selon son type
     */
    public static User createUser(UserType userType, String username, String password,
                                  String email, String fullName) {
        switch (userType) {
            case ADMIN:
                return createAdmin(username, password, email, fullName);
            case EMPLOYEE:
                return createEmployee(username, password, email, fullName);
            default:
                return createEmployee(username, password, email, fullName);
        }
    }

    /**
     * Génère un ID unique pour l'utilisateur
     */
    private static String generateUserId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

package com.example.taskmanagment.models;
import com.example.taskmanagment.models.UserType;

import java.io.Serializable;

/**
 * Modèle représentant un utilisateur du système
 * Implémente Serializable pour pouvoir être passé entre activités
 */
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private UserType userType;
    private String email;
    private String fullName;
    private long createdDate;

    // Constructeur vide
    public User() {
        this.createdDate = System.currentTimeMillis();
    }

    // Constructeur complet
    public User(String id, String username, String password, UserType userType,
                String email, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.email = email;
        this.fullName = fullName;
        this.createdDate = System.currentTimeMillis();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    /**
     * Vérifie si l'utilisateur est un employé
     */
    public boolean isEmployee() {
        return userType == UserType.EMPLOYEE;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", userType=" + userType +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
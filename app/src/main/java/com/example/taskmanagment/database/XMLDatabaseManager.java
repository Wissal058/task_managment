package com.example.taskmanagment.database;

import android.content.Context;
import android.util.Log;

import com.example.taskmanagment.R;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.utils.XMLParser;
import com.example.taskmanagment.utils.XMLValidator;
import com.example.taskmanagment.utils.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de base de données XML
 * Design Pattern: Singleton
 * Gère toutes les opérations de lecture/écriture des fichiers XML avec validation XSD
 */
public class XMLDatabaseManager {
    private static final String TAG = "XMLDatabaseManager";
    private static XMLDatabaseManager instance;
    private Context context;

    private static final String USERS_FILE = "users.xml";
    private static final String TASKS_FILE = "tasks.xml";

    private List<User> usersCache;
    private List<Task> tasksCache;

    /**
     * Constructeur privé pour Singleton
     */
    private XMLDatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        initializeCache();
    }

    /**
     * Obtient l'instance unique (Singleton)
     */
    public static synchronized XMLDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new XMLDatabaseManager(context);
        }
        return instance;
    }

    /**
     * Initialise le cache en chargeant les données depuis les fichiers XML
     * avec validation XSD
     */
    private void initializeCache() {
        Log.d(TAG, "========== Initializing XML Database with XSD Validation ==========");

        // Charger les utilisateurs avec validation XSD
        try {
            File usersFile = new File(context.getFilesDir(), USERS_FILE);

            if (!usersFile.exists()) {
                Log.d(TAG, "First run: copying users.xml from resources");
                copyResourceToFile(R.raw.users, USERS_FILE);
            }

            // VALIDATION XSD AVANT LE PARSING
            Log.d(TAG, "Step 1: Validating users.xml against XSD schema");
            InputStream validationStream = new FileInputStream(usersFile);
            boolean isValid = XMLValidator.validateUsersXML(context, validationStream);
            validationStream.close();

            if (!isValid) {
                Log.e(TAG, "❌ Users XML validation FAILED! Using empty list.");
                usersCache = new ArrayList<>();
                return;
            }

            Log.i(TAG, "✅ Users XML validation PASSED");

            // PARSING DU XML VALIDÉ
            Log.d(TAG, "Step 2: Parsing users.xml");
            InputStream usersStream = new FileInputStream(usersFile);
            usersCache = XMLParser.parseUsersXML(usersStream);
            usersStream.close();

            Log.i(TAG, "✅ Successfully loaded " + usersCache.size() + " users");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading users: " + e.getMessage());
            e.printStackTrace();
            usersCache = new ArrayList<>();
        }

        // Charger les tâches avec validation XSD
        try {
            File tasksFile = new File(context.getFilesDir(), TASKS_FILE);

            if (!tasksFile.exists()) {
                Log.d(TAG, "First run: copying tasks.xml from resources");
                copyResourceToFile(R.raw.tasks, TASKS_FILE);
            }

            // VALIDATION XSD AVANT LE PARSING
            Log.d(TAG, "Step 1: Validating tasks.xml against XSD schema");
            InputStream validationStream = new FileInputStream(tasksFile);
            boolean isValid = XMLValidator.validateTasksXML(context, validationStream);
            validationStream.close();

            if (!isValid) {
                Log.e(TAG, "❌ Tasks XML validation FAILED! Using empty list.");
                tasksCache = new ArrayList<>();
                return;
            }

            Log.i(TAG, "✅ Tasks XML validation PASSED");

            // PARSING DU XML VALIDÉ
            Log.d(TAG, "Step 2: Parsing tasks.xml");
            InputStream tasksStream = new FileInputStream(tasksFile);
            tasksCache = XMLParser.parseTasksXML(tasksStream);
            tasksStream.close();

            Log.i(TAG, "✅ Successfully loaded " + tasksCache.size() + " tasks");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            tasksCache = new ArrayList<>();
        }

        Log.d(TAG, "========== XML Database Initialization Complete ==========");
    }

    /**
     * Copie un fichier de ressource vers le stockage interne
     */
    private void copyResourceToFile(int resourceId, String fileName) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            File outputFile = new File(context.getFilesDir(), fileName);

            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "✅ Copied resource to file: " + fileName);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error copying resource: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde les utilisateurs dans le fichier XML
     * Valide le XML après l'écriture
     */
    private boolean saveUsers() {
        Log.d(TAG, "Saving users to XML");
        boolean writeSuccess = XMLWriter.writeUsersToXML(context, usersCache, USERS_FILE);

        if (writeSuccess) {
            // Valider le XML après écriture
            try {
                File usersFile = new File(context.getFilesDir(), USERS_FILE);
                InputStream validationStream = new FileInputStream(usersFile);
                boolean isValid = XMLValidator.validateUsersXML(context, validationStream);
                validationStream.close();

                if (isValid) {
                    Log.i(TAG, "✅ Users XML saved and validated successfully");
                    return true;
                } else {
                    Log.e(TAG, "❌ Users XML saved but validation FAILED!");
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error validating saved users XML: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Sauvegarde les tâches dans le fichier XML
     * Valide le XML après l'écriture
     */
    private boolean saveTasks() {
        Log.d(TAG, "Saving tasks to XML");
        boolean writeSuccess = XMLWriter.writeTasksToXML(context, tasksCache, TASKS_FILE);

        if (writeSuccess) {
            // Valider le XML après écriture
            try {
                File tasksFile = new File(context.getFilesDir(), TASKS_FILE);
                InputStream validationStream = new FileInputStream(tasksFile);
                boolean isValid = XMLValidator.validateTasksXML(context, validationStream);
                validationStream.close();

                if (isValid) {
                    Log.i(TAG, "✅ Tasks XML saved and validated successfully");
                    return true;
                } else {
                    Log.e(TAG, "❌ Tasks XML saved but validation FAILED!");
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error validating saved tasks XML: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    // ====== MÉTHODES PUBLIQUES CRUD ======

    public List<User> getAllUsers() {
        return new ArrayList<>(usersCache);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasksCache);
    }

    public User getUserById(String userId) {
        for (User user : usersCache) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public Task getTaskById(String taskId) {
        for (Task task : tasksCache) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        usersCache.add(user);
        return saveUsers();
    }

    public boolean addTask(Task task) {
        tasksCache.add(task);
        return saveTasks();
    }

    public boolean updateUser(User user) {
        for (int i = 0; i < usersCache.size(); i++) {
            if (usersCache.get(i).getId().equals(user.getId())) {
                usersCache.set(i, user);
                return saveUsers();
            }
        }
        return false;
    }

    public boolean updateTask(Task task) {
        for (int i = 0; i < tasksCache.size(); i++) {
            if (tasksCache.get(i).getId().equals(task.getId())) {
                tasksCache.set(i, task);
                return saveTasks();
            }
        }
        return false;
    }

    public boolean deleteUser(String userId) {
        for (int i = 0; i < usersCache.size(); i++) {
            if (usersCache.get(i).getId().equals(userId)) {
                usersCache.remove(i);
                return saveUsers();
            }
        }
        return false;
    }

    public boolean deleteTask(String taskId) {
        for (int i = 0; i < tasksCache.size(); i++) {
            if (tasksCache.get(i).getId().equals(taskId)) {
                tasksCache.remove(i);
                return saveTasks();
            }
        }
        return false;
    }

    public void reload() {
        Log.d(TAG, "Reloading XML database");
        initializeCache();
    }
}


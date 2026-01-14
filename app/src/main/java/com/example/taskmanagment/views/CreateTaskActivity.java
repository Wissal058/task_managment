package com.example.taskmanagment.views;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.utils.LanguageManager;
import com.example.taskmanagment.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activité pour créer ou modifier une tâche
 */
public class CreateTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskTitle, etTaskDescription;
    private Spinner spinnerAssignTo, spinnerPriority;
    private Button btnSelectDueDate, btnSave, btnCancel;

    private TaskController taskController;
    private UserController userController;
    private AuthController authController;

    private List<User> employees;
    private String selectedUserId;
    private TaskPriority selectedPriority = TaskPriority.MEDIUM;
    private long selectedDueDate = 0;

    private boolean editMode = false;
    private Task taskToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Initialiser les contrôleurs
        taskController = new TaskController(this);
        userController = new UserController(this);
        authController = new AuthController(this);

        // Vérifier le mode édition
        editMode = getIntent().getBooleanExtra("edit_mode", false);
        if (editMode) {
            taskToEdit = (Task) getIntent().getSerializableExtra("task");
        }

        // Configurer la toolbar
        setupToolbar();

        // Initialiser les vues
        initViews();

        // Charger les employés
        loadEmployees();

        // Configurer les spinners
        setupSpinners();

        // Configurer les boutons
        setupButtons();

        // Si en mode édition, remplir les champs
        if (editMode && taskToEdit != null) {
            fillFieldsForEdit();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editMode ? R.string.edit_task : R.string.create_task);
        }
    }

    private void initViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerAssignTo = findViewById(R.id.spinnerAssignTo);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnSelectDueDate = findViewById(R.id.btnSelectDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadEmployees() {
        employees = userController.getAllEmployees();
    }

    private void setupSpinners() {
        // Spinner pour assigner à un employé
        String[] employeeNames = new String[employees.size()];
        for (int i = 0; i < employees.size(); i++) {
            User employee = employees.get(i);
            employeeNames[i] = employee.getFullName() + " (" + employee.getUsername() + ")";
        }

        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, employeeNames
        );
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(employeeAdapter);

        spinnerAssignTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = employees.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner de priorité
        String[] priorityOptions = {
                getString(R.string.priority_low),
                getString(R.string.priority_medium),
                getString(R.string.priority_high),
                getString(R.string.priority_urgent)
        };

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, priorityOptions
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Medium par défaut

        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedPriority = TaskPriority.LOW;
                        break;
                    case 1:
                        selectedPriority = TaskPriority.MEDIUM;
                        break;
                    case 2:
                        selectedPriority = TaskPriority.HIGH;
                        break;
                    case 3:
                        selectedPriority = TaskPriority.URGENT;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupButtons() {
        btnSelectDueDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth, 23, 59, 59);
                    selectedDueDate = selectedCalendar.getTimeInMillis();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    btnSelectDueDate.setText(sdf.format(selectedCalendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Ne pas permettre de sélectionner une date passée
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void fillFieldsForEdit() {
        etTaskTitle.setText(taskToEdit.getTitle());
        etTaskDescription.setText(taskToEdit.getDescription());

        // Trouver l'index de l'employé assigné
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equals(taskToEdit.getAssignedTo())) {
                spinnerAssignTo.setSelection(i);
                break;
            }
        }

        // Définir la priorité
        switch (taskToEdit.getPriority()) {
            case LOW:
                spinnerPriority.setSelection(0);
                break;
            case MEDIUM:
                spinnerPriority.setSelection(1);
                break;
            case HIGH:
                spinnerPriority.setSelection(2);
                break;
            case URGENT:
                spinnerPriority.setSelection(3);
                break;
        }

        // Définir la date d'échéance
        selectedDueDate = taskToEdit.getDueDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnSelectDueDate.setText(sdf.format(selectedDueDate));

        // Changer le texte du bouton
        btnSave.setText(R.string.update);
    }

    private void saveTask() {
        // Validation
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError(getString(R.string.field_required));
            etTaskTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etTaskDescription.setError(getString(R.string.field_required));
            etTaskDescription.requestFocus();
            return;
        }

        if (selectedUserId == null || selectedUserId.isEmpty()) {
            Toast.makeText(this, R.string.select_user, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDueDate == 0) {
            Toast.makeText(this, R.string.select_date, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;

        if (editMode && taskToEdit != null) {
            // Mode édition
            taskToEdit.setTitle(title);
            taskToEdit.setDescription(description);
            taskToEdit.setAssignedTo(selectedUserId);
            taskToEdit.setPriority(selectedPriority);
            taskToEdit.setDueDate(selectedDueDate);

            success = taskController.updateTask(taskToEdit);
        } else {
            // Mode création
            String currentUserId = authController.getCurrentUserId();
            success = taskController.createTask(
                    title,
                    description,
                    selectedUserId,
                    currentUserId,
                    selectedPriority,
                    selectedDueDate
            );
        }

        if (success) {
            Toast.makeText(this,
                    editMode ? R.string.task_updated_success : R.string.task_created_success,
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.task_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
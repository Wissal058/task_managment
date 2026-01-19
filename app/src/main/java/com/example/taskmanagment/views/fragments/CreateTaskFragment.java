package com.example.taskmanagment.views.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;
import com.example.taskmanagment.utils.XMLParser;
import com.example.taskmanagment.utils.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTaskFragment extends Fragment {

    private EditText etTitle, etDescription, etDueDate;
    private Spinner spinnerEmployee, spinnerPriority;
    private Button btnCancel, btnSave;

    private long selectedDueDateMillis = 0;

    private AuthController authController;
    private User currentUser;

    private List<User> employees = new ArrayList<>();

    public CreateTaskFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_create_task, container, false);

        authController = new AuthController(requireContext());
        currentUser = authController.getCurrentUser(); // celui qui crée la tâche

        etTitle = v.findViewById(R.id.etTitle);
        etDescription = v.findViewById(R.id.etDescription);
        etDueDate = v.findViewById(R.id.etDueDate);

        spinnerEmployee = v.findViewById(R.id.spinnerEmployee);
        spinnerPriority = v.findViewById(R.id.spinnerPriority);

        btnCancel = v.findViewById(R.id.btnCancel);
        btnSave = v.findViewById(R.id.btnSave);

        setupPrioritySpinner();
        setupEmployeeSpinner();
        setupDueDatePicker();

        btnCancel.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        btnSave.setOnClickListener(view -> saveTaskToXml());

        return v;
    }

    // -------------------- SPINNERS --------------------

    private void setupPrioritySpinner() {
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "URGENT"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(adapter);
    }

    private void setupEmployeeSpinner() {
        employees = loadEmployees(requireContext());

        List<String> employeeNames = new ArrayList<>();
        for (User u : employees) {
            employeeNames.add(u.getFullName() + " (id=" + u.getId() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, employeeNames);
        spinnerEmployee.setAdapter(adapter);
    }

    // -------------------- DATE PICKER --------------------

    private void setupDueDatePicker() {
        etDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(Calendar.YEAR, year);
                        picked.set(Calendar.MONTH, month);
                        picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        picked.set(Calendar.HOUR_OF_DAY, 0);
                        picked.set(Calendar.MINUTE, 0);
                        picked.set(Calendar.SECOND, 0);
                        picked.set(Calendar.MILLISECOND, 0);

                        selectedDueDateMillis = picked.getTimeInMillis();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDueDate.setText(sdf.format(picked.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));

            dp.show();
        });
    }

    // -------------------- SAVE TASK --------------------

    private void saveTaskToXml() {
        Context ctx = requireContext();

        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(ctx, "Veuillez remplir le titre et la description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (employees.isEmpty()) {
            Toast.makeText(ctx, "Aucun employé trouvé pour l’affectation", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDueDateMillis == 0) {
            Toast.makeText(ctx, "Veuillez sélectionner une date d’échéance", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Charger tasks existantes
        List<Task> tasks = loadTasks(ctx);

        // 2) Créer un nouvel ID (max + 1)
        int newId = getNextTaskId(tasks);

        // 3) Récupérer employé sélectionné
        int pos = spinnerEmployee.getSelectedItemPosition();
        User assignedEmployee = employees.get(pos);

        // 4) Récupérer priorité
        String priorityStr = String.valueOf(spinnerPriority.getSelectedItem());
        TaskPriority priority = TaskPriority.fromString(priorityStr);

        // 5) Construire la tâche
        Task t = new Task();
        t.setId(String.valueOf(newId));
        t.setTitle(title);
        t.setDescription(desc);
        t.setAssignedTo(assignedEmployee.getId());
        t.setCreatedBy(currentUser != null ? currentUser.getId() : "1");
        t.setStatus(TaskStatus.PENDING);
        t.setPriority(priority);
        t.setCreatedDate(System.currentTimeMillis());
        t.setDueDate(selectedDueDateMillis);

        tasks.add(t);

        // 6) Écrire dans filesDir/tasks.xml
        boolean ok = XMLWriter.writeTasksToXML(ctx, tasks, "tasks.xml");

        if (ok) {
            Toast.makeText(ctx, "Tâche ajoutée avec succès ✅", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(ctx, "Erreur lors de l'enregistrement ❌", Toast.LENGTH_SHORT).show();
        }
    }

    // -------------------- LOADERS --------------------

    private List<Task> loadTasks(Context ctx) {
        try {
            File internalFile = new File(ctx.getFilesDir(), "tasks.xml");
            InputStream is;

            if (internalFile.exists()) {
                is = new FileInputStream(internalFile);
            } else {
                // ⚠️ Remplace R.raw.tasks par le vrai nom de ton fichier dans res/raw
                is = ctx.getResources().openRawResource(R.raw.tasks);
            }

            List<Task> tasks = XMLParser.parseTasksXML(is);
            is.close();
            return tasks != null ? tasks : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<User> loadEmployees(Context ctx) {
        try {
            File internalFile = new File(ctx.getFilesDir(), "users.xml");
            InputStream is;

            if (internalFile.exists()) {
                is = new FileInputStream(internalFile);
            } else {
                // ⚠️ Remplace R.raw.users par le vrai nom de ton fichier dans res/raw
                is = ctx.getResources().openRawResource(R.raw.users);
            }

            List<User> users = XMLParser.parseUsersXML(is);
            is.close();

            List<User> employeesOnly = new ArrayList<>();
            for (User u : users) {
                if (u.getUserType() == UserType.EMPLOYEE) {
                    employeesOnly.add(u);
                }
            }
            return employeesOnly;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private int getNextTaskId(List<Task> tasks) {
        int max = 0;
        for (Task t : tasks) {
            try {
                int id = Integer.parseInt(t.getId());
                if (id > max) max = id;
            } catch (Exception ignored) {}
        }
        return max + 1;
    }
}

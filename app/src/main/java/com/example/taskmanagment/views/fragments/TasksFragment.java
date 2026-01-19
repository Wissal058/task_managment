package com.example.taskmanagment.views.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.adapters.TaskAdapter;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private com.google.android.material.textfield.TextInputEditText etSearch, etFromDate, etToDate;
    private Spinner spUser, spPriority;
    private View btnApply, btnReset;
    private RecyclerView rvTasks;
    private TextView tvEmpty;

    private TaskController taskController;
    private UserController userController;

    private TaskAdapter adapter;

    private final List<Task> allTasks = new ArrayList<>();
    private final List<User> employees = new ArrayList<>();

    private String selectedUserId = "ALL";
    private String selectedPriority = "ALL";
    private Long fromMillis = null;
    private Long toMillis = null;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        taskController = new TaskController(requireContext());
        userController = new UserController(requireContext());

        bind(view);
        setupRecycler();
        setupFilters();

        loadAll();
        applyFilters();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
        applyFilters();
    }

    private void bind(View v) {
        etSearch = v.findViewById(R.id.etSearch);
        etFromDate = v.findViewById(R.id.etFromDate);
        etToDate = v.findViewById(R.id.etToDate);

        spUser = v.findViewById(R.id.spUser);
        spPriority = v.findViewById(R.id.spPriority);

        btnApply = v.findViewById(R.id.btnApply);
        btnReset = v.findViewById(R.id.btnReset);

        rvTasks = v.findViewById(R.id.rvTasks);
        tvEmpty = v.findViewById(R.id.tvEmpty);
    }

    private void setupRecycler() {
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TaskAdapter(
                requireContext(),
                task -> openTaskDetail(task.getId()),
                task -> openEditTask(task.getId()),
                task -> confirmDelete(task)
        );

        rvTasks.setAdapter(adapter);
    }

    private void setupFilters() {
        // users
        List<String> userLabels = new ArrayList<>();
        userLabels.add(getString(R.string.all_users));

        employees.clear();
        employees.addAll(userController.getAllEmployees());
        for (User u : employees) userLabels.add(u.getFullName() + " (id=" + u.getId() + ")");

        spUser.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, userLabels));

        spUser.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = (position == 0) ? "ALL" : employees.get(position - 1).getId();
                applyFilters();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // priority
        List<String> priorities = new ArrayList<>();
        priorities.add(getString(R.string.all_priorities));
        priorities.add("LOW");
        priorities.add("MEDIUM");
        priorities.add("HIGH");
        priorities.add("URGENT");

        spPriority.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, priorities));

        spPriority.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = (position == 0) ? "ALL" : String.valueOf(spPriority.getSelectedItem());
                applyFilters();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // dates
        etFromDate.setOnClickListener(v -> pickDate(true));
        etToDate.setOnClickListener(v -> pickDate(false));

        // search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnApply.setOnClickListener(v -> applyFilters());

        btnReset.setOnClickListener(v -> {
            selectedUserId = "ALL";
            selectedPriority = "ALL";
            fromMillis = null;
            toMillis = null;

            spUser.setSelection(0);
            spPriority.setSelection(0);
            etFromDate.setText("");
            etToDate.setText("");
            etSearch.setText("");

            applyFilters();
        });
    }

    private void loadAll() {
        allTasks.clear();
        allTasks.addAll(taskController.getAllTasks());
    }

    private void applyFilters() {
        String q = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase() : "";

        List<Task> out = new ArrayList<>();
        for (Task t : allTasks) {

            // search
            if (!q.isEmpty()) {
                String title = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
                String desc = t.getDescription() != null ? t.getDescription().toLowerCase() : "";
                if (!title.contains(q) && !desc.contains(q)) continue;
            }

            // user
            if (!"ALL".equals(selectedUserId)) {
                if (t.getAssignedTo() == null || !selectedUserId.equals(t.getAssignedTo())) continue;
            }

            // priority
            if (!"ALL".equals(selectedPriority)) {
                TaskPriority pr = t.getPriority();
                if (pr == null || !selectedPriority.equals(pr.name())) continue;
            }

            // date range (dueDate)
            long due = t.getDueDate();
            if (fromMillis != null && due < fromMillis) continue;
            if (toMillis != null && due > toMillis) continue;

            out.add(t);
        }

        adapter.submit(out);

        boolean empty = out.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void pickDate(boolean isFrom) {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dp = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    c.set(Calendar.HOUR_OF_DAY, isFrom ? 0 : 23);
                    c.set(Calendar.MINUTE, isFrom ? 0 : 59);
                    c.set(Calendar.SECOND, isFrom ? 0 : 59);
                    c.set(Calendar.MILLISECOND, isFrom ? 0 : 999);

                    long ms = c.getTimeInMillis();
                    if (isFrom) {
                        fromMillis = ms;
                        etFromDate.setText(sdf.format(c.getTime()));
                    } else {
                        toMillis = ms;
                        etToDate.setText(sdf.format(c.getTime()));
                    }

                    applyFilters();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        dp.show();
    }

    private void openTaskDetail(String taskId) {
        TaskDetailFragment fragment = TaskDetailFragment.newInstance(taskId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("task_detail")
                .commit();
    }

    private void openEditTask(String taskId) {
        // TODO: remplacer par EditTaskFragment
        android.widget.Toast.makeText(requireContext(), "Edit: " + taskId, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_task)
                .setMessage(R.string.confirm_delete_task)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    taskController.deleteTask(task.getId());
                    loadAll();
                    applyFilters();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
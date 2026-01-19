package com.example.taskmanagment.views.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.adapters.TaskAdapter;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerViewTasks;
    private TextView tvSelectedDate, tvNoTasks;

    private TaskAdapter taskAdapter;
    private TaskController taskController;

    private long selectedDateMillis;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initViews(view);
        initControllers();
        setupRecyclerView();
        setupCalendar();

        // Charger les tâches du jour actuel
        selectedDateMillis = System.currentTimeMillis();
        loadTasksForDate(selectedDateMillis);

        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        recyclerViewTasks = view.findViewById(R.id.recyclerViewCalendarTasks);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvNoTasks = view.findViewById(R.id.tvNoTasks);
    }

    private void initControllers() {
        taskController = new TaskController(requireContext());
        dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
    }

    private void setupRecyclerView() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        taskAdapter = new TaskAdapter(
                requireContext(),

                // ✅ click -> ouvrir détails
                task -> {
                    TaskDetailFragment fragment = TaskDetailFragment.newInstance(task.getId());
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack("task_detail")
                            .commit();
                },

                // ✅ edit (pour l’instant juste ouvrir détails ou créer EditTaskFragment après)
                task -> {
                    // TODO: remplacer par EditTaskFragment / EditTaskActivity
                    // Exemple temporaire:
                    TaskDetailFragment fragment = TaskDetailFragment.newInstance(task.getId());
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack("task_detail")
                            .commit();
                },

                // ✅ delete (pour l’instant on supprime depuis XML via TaskController)
                task -> {
                    // supprimer la tâche
                    taskController.deleteTask(task.getId());

                    // recharger la liste du jour
                    loadTasksForDate(selectedDateMillis);
                }
        );

        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            selectedDateMillis = calendar.getTimeInMillis();
            loadTasksForDate(selectedDateMillis);
        });
    }

    private void loadTasksForDate(long dateMillis) {
        // Mettre à jour le titre
        tvSelectedDate.setText(dateFormat.format(dateMillis));

        List<Task> allTasks = taskController.getAllTasks();

        // Bornes de la journée
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTimeInMillis(dateMillis);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        long startMillis = startOfDay.getTimeInMillis();

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTimeInMillis(dateMillis);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);
        long endMillis = endOfDay.getTimeInMillis();

        // ✅ Filtrage compatible Android < N (pas de stream)
        List<Task> tasksForDate = new ArrayList<>();
        for (Task task : allTasks) {
            long dueDate = task.getDueDate();
            if (dueDate >= startMillis && dueDate <= endMillis) {
                tasksForDate.add(task);
            }
        }

        if (tasksForDate.isEmpty()) {
            recyclerViewTasks.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);

            // ✅ méthode existante dans notre adapter
            taskAdapter.submit(tasksForDate);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasksForDate(selectedDateMillis);
    }
}
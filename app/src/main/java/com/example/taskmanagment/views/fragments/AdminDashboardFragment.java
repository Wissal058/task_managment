package com.example.taskmanagment.views.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalTasks, tvTotalUsers, tvCompletedTasks, tvInProgressTasks;
    private TextView tvTotalTasksPercent, tvCompletedPercent;

    private View progressTotalTasks, progressCompleted;
    private View progressTotalContainer, progressCompletedContainer;

    private LineChart lineChartWeekly;
    private PieChart pieChartPriority;
    private BarChart barChartEmployees;

    private TaskController taskController;
    private UserController userController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        taskController = new TaskController(requireContext());
        userController = new UserController(requireContext());

        initViews(view);
        loadDataFromXML();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh après ajout task/user
        loadDataFromXML();
    }

    private void initViews(View view) {
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvInProgressTasks = view.findViewById(R.id.tvInProgressTasks);

        tvTotalTasksPercent = view.findViewById(R.id.tvTotalTasksPercent);
        tvCompletedPercent = view.findViewById(R.id.tvCompletedPercent);

        progressTotalTasks = view.findViewById(R.id.progressTotalTasks);
        progressCompleted = view.findViewById(R.id.progressCompleted);

        // ✅ IDs à ajouter dans XML (voir plus bas)
        progressTotalContainer = view.findViewById(R.id.progressTotalContainer);
        progressCompletedContainer = view.findViewById(R.id.progressCompletedContainer);

        lineChartWeekly = view.findViewById(R.id.lineChartWeekly);
        pieChartPriority = view.findViewById(R.id.pieChartPriority);
        barChartEmployees = view.findViewById(R.id.barChartEmployees);
    }

    private void loadDataFromXML() {
        loadStatistics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setupWeeklyChart();
            setupPriorityPieChart();
            setupEmployeesBarChart();
        }
    }

    private void loadStatistics() {
        List<Task> allTasks = taskController.getAllTasks();

        int totalTasks = allTasks.size();
        int completed = 0, inProgress = 0;

        for (Task task : allTasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) completed++;
            if (task.getStatus() == TaskStatus.IN_PROGRESS) inProgress++;
        }

        int totalUsers = userController.getAllEmployees().size();

        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvTotalUsers.setText(String.valueOf(totalUsers));
        tvCompletedTasks.setText(String.valueOf(completed));
        tvInProgressTasks.setText(String.valueOf(inProgress));

        int totalPercent = totalTasks == 0 ? 0 : Math.min(100, totalTasks * 10); // exemple visuel
        int completedPercent = totalTasks > 0 ? (completed * 100) / totalTasks : 0;

        tvTotalTasksPercent.setText(totalPercent + "%");
        tvCompletedPercent.setText(completedPercent + "%");

        updateProgressBar(progressTotalContainer, progressTotalTasks, totalPercent);
        updateProgressBar(progressCompletedContainer, progressCompleted, completedPercent);
    }

    private void updateProgressBar(View container, View bar, int percent) {
        if (container == null || bar == null) return;

        container.post(() -> {
            int fullWidth = container.getWidth();
            ViewGroup.LayoutParams params = bar.getLayoutParams();
            params.width = (int) (fullWidth * (percent / 100f));
            bar.setLayoutParams(params);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupWeeklyChart() {
        List<Task> allTasks = taskController.getAllTasks();

        Map<Integer, Integer> currentWeek = new HashMap<>();
        Map<Integer, Integer> lastWeek = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000);
        long twoWeeksAgo = now - (14L * 24 * 60 * 60 * 1000);

        for (Task task : allTasks) {
            long createdTime = task.getCreatedDate();
            calendar.setTimeInMillis(createdTime);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1..7

            if (createdTime >= oneWeekAgo) {
                currentWeek.put(dayOfWeek, currentWeek.getOrDefault(dayOfWeek, 0) + 1);
            } else if (createdTime >= twoWeeksAgo) {
                lastWeek.put(dayOfWeek, lastWeek.getOrDefault(dayOfWeek, 0) + 1);
            }
        }

        List<Entry> currentEntries = new ArrayList<>();
        List<Entry> lastEntries = new ArrayList<>();

        // Dim..Sam (IndexAxis formatter)
        String[] days = {"D", "L", "M", "M", "J", "V", "S"};

        // Calendar.DAY_OF_WEEK: 1=Sunday..7=Saturday
        for (int i = 1; i <= 7; i++) {
            currentEntries.add(new Entry(i - 1, currentWeek.getOrDefault(i, 0)));
            lastEntries.add(new Entry(i - 1, lastWeek.getOrDefault(i, 0)));
        }

        LineDataSet currentDataSet = new LineDataSet(currentEntries, "Cette semaine");
        currentDataSet.setColor(Color.BLACK);
        currentDataSet.setLineWidth(3f);
        currentDataSet.setCircleColor(Color.BLACK);
        currentDataSet.setCircleRadius(5f);
        currentDataSet.setDrawFilled(true);
        currentDataSet.setFillColor(Color.BLACK);
        currentDataSet.setFillAlpha(25);
        currentDataSet.setDrawValues(false);
        currentDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet lastDataSet = new LineDataSet(lastEntries, "Semaine dernière");
        lastDataSet.setColor(Color.parseColor("#FFB3B3"));
        lastDataSet.setLineWidth(2f);
        lastDataSet.setCircleColor(Color.parseColor("#FFB3B3"));
        lastDataSet.setCircleRadius(4f);
        lastDataSet.setDrawFilled(true);
        lastDataSet.setFillColor(Color.parseColor("#FFB3B3"));
        lastDataSet.setFillAlpha(18);
        lastDataSet.setDrawValues(false);
        lastDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChartWeekly.setData(new LineData(currentDataSet, lastDataSet));
        lineChartWeekly.getDescription().setEnabled(false);
        lineChartWeekly.setDrawGridBackground(false);
        lineChartWeekly.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChartWeekly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        lineChartWeekly.getAxisLeft().setAxisMinimum(0f);
        lineChartWeekly.getAxisLeft().setDrawGridLines(true);
        lineChartWeekly.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        Legend legend = lineChartWeekly.getLegend();
        legend.setEnabled(false);

        lineChartWeekly.animateX(800);
        lineChartWeekly.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupPriorityPieChart() {
        List<Task> allTasks = taskController.getAllTasks();
        Map<TaskPriority, Integer> priorityCount = new HashMap<>();

        for (Task task : allTasks) {
            TaskPriority priority = task.getPriority();
            if (priority == null) continue;
            priorityCount.put(priority, priorityCount.getOrDefault(priority, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<TaskPriority, Integer> e : priorityCount.entrySet()) {
            if (e.getValue() > 0) {
                entries.add(new PieEntry(e.getValue(), e.getKey().getDisplayName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4169E1"),
                Color.parseColor("#87CEEB"),
                Color.parseColor("#00CED1"),
                Color.parseColor("#20B2AA")
        );
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartPriority));

        pieChartPriority.setData(data);
        pieChartPriority.setUsePercentValues(true);
        pieChartPriority.getDescription().setEnabled(false);
        pieChartPriority.setDrawHoleEnabled(true);
        pieChartPriority.setHoleColor(Color.WHITE);
        pieChartPriority.setHoleRadius(55f);
        pieChartPriority.setTransparentCircleRadius(60f);
        pieChartPriority.setCenterText("Priorités");
        pieChartPriority.setCenterTextSize(16f);
        pieChartPriority.setEntryLabelColor(Color.BLACK);
        pieChartPriority.setEntryLabelTextSize(11f);

        pieChartPriority.getLegend().setEnabled(false);

        pieChartPriority.animateY(800);
        pieChartPriority.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupEmployeesBarChart() {
        List<Task> allTasks = taskController.getAllTasks();
        Map<String, Integer> employeeTaskCount = new HashMap<>();
        Map<String, String> employeeNames = new HashMap<>();

        for (Task task : allTasks) {
            String userId = task.getAssignedTo();
            if (userId == null) continue;

            employeeTaskCount.put(userId, employeeTaskCount.getOrDefault(userId, 0) + 1);

            if (!employeeNames.containsKey(userId)) {
                User u = userController.getUserById(userId);
                String name = (u != null && u.getFullName() != null && !u.getFullName().isEmpty())
                        ? u.getFullName().split(" ")[0]
                        : "U" + userId;
                employeeNames.put(userId, name);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> e : employeeTaskCount.entrySet()) {
            entries.add(new BarEntry(index, e.getValue()));
            labels.add(employeeNames.get(e.getKey()));
            colors.add(index % 2 == 0 ? Color.BLACK : Color.parseColor("#FFB3B3"));
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartEmployees.setData(data);
        barChartEmployees.getDescription().setEnabled(false);
        barChartEmployees.setDrawGridBackground(false);
        barChartEmployees.getAxisRight().setEnabled(false);

        barChartEmployees.getAxisLeft().setAxisMinimum(0f);
        barChartEmployees.getAxisLeft().setDrawGridLines(true);
        barChartEmployees.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        XAxis xAxis = barChartEmployees.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-35);

        barChartEmployees.getLegend().setEnabled(false);

        barChartEmployees.animateY(800);
        barChartEmployees.invalidate();
    }
}

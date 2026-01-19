package com.example.taskmanagment.views.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.database.TaskDAO;
import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Employé avec design moderne (style Spendly)
 */
public class DashboardFragment extends Fragment {

    private TextView tvUserName, tvIncome, tvExpenses;
    private TextView tvIncomePercent, tvExpensesPercent;
    private LineChart lineChartCashflow;
    private PieChart pieChartExpenses;

    private AuthController authController;
    private TaskController taskController;
    private User currentUser;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        authController = new AuthController(requireContext());
        taskController = new TaskController(requireContext());
        currentUser = authController.getCurrentUser();

        initViews(view);
        loadData();

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpenses = view.findViewById(R.id.tvExpenses);
        tvIncomePercent = view.findViewById(R.id.tvIncomePercent);
        tvExpensesPercent = view.findViewById(R.id.tvExpensesPercent);
        lineChartCashflow = view.findViewById(R.id.lineChartCashflow);
        pieChartExpenses = view.findViewById(R.id.pieChartExpenses);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadData() {
        // Set user name
        tvUserName.setText(currentUser.getFullName());

        // Load statistics
        TaskDAO.TaskStatistics stats = taskController.getUserTaskStatistics(currentUser.getId());

        // Income = Completed tasks, Expenses = Pending/In Progress
        tvIncome.setText(String.valueOf(stats.completed));
        tvExpenses.setText(String.valueOf(stats.pending + stats.inProgress));

        // Calculate percentages
        int totalTasks = stats.total;
        int incomePercent = totalTasks > 0 ? (stats.completed * 100) / totalTasks : 0;
        int expensesPercent = totalTasks > 0 ? ((stats.pending + stats.inProgress) * 100) / totalTasks : 0;

        tvIncomePercent.setText("↑ " + incomePercent + "%");
        tvExpensesPercent.setText("↓ " + expensesPercent + "%");

        // Setup charts
        setupCashflowChart();
        setupExpensesPieChart();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupCashflowChart() {
        List<Task> userTasks = taskController.getTasksByUser(currentUser.getId());

        // Group tasks by month
        Map<Integer, Integer> completedByMonth = new HashMap<>();
        Map<Integer, Integer> pendingByMonth = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        for (Task task : userTasks) {
            calendar.setTimeInMillis(task.getCreatedDate());
            int month = calendar.get(Calendar.MONTH);

            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedByMonth.put(month, completedByMonth.getOrDefault(month, 0) + 1);
            } else {
                pendingByMonth.put(month, pendingByMonth.getOrDefault(month, 0) + 1);
            }
        }

        // Prepare data for last 6 months
        List<Entry> incomeEntries = new ArrayList<>();
        List<Entry> expenseEntries = new ArrayList<>();
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        for (int i = 0; i < 6; i++) {
            int month = (currentMonth - 5 + i + 12) % 12;
            incomeEntries.add(new Entry(i, completedByMonth.getOrDefault(month, 0) * 100));
            expenseEntries.add(new Entry(i, pendingByMonth.getOrDefault(month, 0) * 100));
        }

        // Income line (green)
        LineDataSet incomeDataSet = new LineDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.parseColor("#00C853"));
        incomeDataSet.setLineWidth(2.5f);
        incomeDataSet.setCircleColor(Color.parseColor("#00C853"));
        incomeDataSet.setCircleRadius(4f);
        incomeDataSet.setDrawCircleHole(false);
        incomeDataSet.setDrawValues(false);
        incomeDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Expense line (blue)
        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, "Expense");
        expenseDataSet.setColor(Color.parseColor("#2196F3"));
        expenseDataSet.setLineWidth(2.5f);
        expenseDataSet.setCircleColor(Color.parseColor("#2196F3"));
        expenseDataSet.setCircleRadius(4f);
        expenseDataSet.setDrawCircleHole(false);
        expenseDataSet.setDrawValues(false);
        expenseDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(incomeDataSet, expenseDataSet);
        lineChartCashflow.setData(lineData);
        lineChartCashflow.getDescription().setEnabled(false);
        lineChartCashflow.setDrawGridBackground(false);
        lineChartCashflow.getAxisRight().setEnabled(false);
        lineChartCashflow.getAxisLeft().setDrawGridLines(true);
        lineChartCashflow.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
        lineChartCashflow.getAxisLeft().setAxisMinimum(0f);

        XAxis xAxis = lineChartCashflow.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        Legend legend = lineChartCashflow.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);

        lineChartCashflow.animateX(1000);
        lineChartCashflow.invalidate();
    }

    private void setupExpensesPieChart() {
        List<Task> userTasks = taskController.getTasksByUser(currentUser.getId());

        // Count tasks by status
        int pending = 0, inProgress = 0, completed = 0, cancelled = 0;
        for (Task task : userTasks) {
            switch (task.getStatus()) {
                case PENDING:
                    pending++;
                    break;
                case IN_PROGRESS:
                    inProgress++;
                    break;
                case COMPLETED:
                    completed++;
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        if (pending > 0) entries.add(new PieEntry(pending, "En Attente"));
        if (inProgress > 0) entries.add(new PieEntry(inProgress, "En Cours"));
        if (completed > 0) entries.add(new PieEntry(completed, "Terminées"));
        if (cancelled > 0) entries.add(new PieEntry(cancelled, "Annulées"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4169E1"), // Blue
                Color.parseColor("#00CED1"), // Turquoise
                Color.parseColor("#87CEEB"), // Light Blue
                Color.parseColor("#20B2AA")  // Light Sea Green
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartExpenses));

        pieChartExpenses.setData(data);
        pieChartExpenses.setUsePercentValues(true);
        pieChartExpenses.getDescription().setEnabled(false);
        pieChartExpenses.setDrawHoleEnabled(true);
        pieChartExpenses.setHoleColor(Color.WHITE);
        pieChartExpenses.setHoleRadius(58f);
        pieChartExpenses.setTransparentCircleRadius(63f);

        // Center text
        int total = pending + inProgress + completed + cancelled;
        pieChartExpenses.setCenterText("Total\n" + total);
        pieChartExpenses.setCenterTextSize(14f);
        pieChartExpenses.setEntryLabelColor(Color.BLACK);
        pieChartExpenses.setEntryLabelTextSize(10f);

        Legend legend = pieChartExpenses.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);

        pieChartExpenses.animateY(1000);
        pieChartExpenses.invalidate();
    }
}
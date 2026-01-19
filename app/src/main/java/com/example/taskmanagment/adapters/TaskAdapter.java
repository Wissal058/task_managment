package com.example.taskmanagment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    public interface OnTaskClick {
        void onClick(Task task); // ouvrir détails
    }

    public interface OnTaskEdit {
        void onEdit(Task task);
    }

    public interface OnTaskDelete {
        void onDelete(Task task);
    }

    private final Context context;
    private final OnTaskClick onTaskClick;
    private final OnTaskEdit onTaskEdit;
    private final OnTaskDelete onTaskDelete;

    private final List<Task> tasks = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // ✅ Un seul constructeur, mais avec les 3 callbacks
    public TaskAdapter(Context context,
                       OnTaskClick onTaskClick,
                       OnTaskEdit onTaskEdit,
                       OnTaskDelete onTaskDelete) {
        this.context = context;
        this.onTaskClick = onTaskClick;
        this.onTaskEdit = onTaskEdit;
        this.onTaskDelete = onTaskDelete;
    }

    public void updateTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public void submit(List<Task> newTasks) {
        updateTasks(newTasks);
    }

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {
        Task t = tasks.get(position);

        holder.tvTitle.setText(t.getTitle() != null ? t.getTitle() : "-");
        holder.tvStatus.setText(t.getStatus() != null ? t.getStatus().getDisplayName() : "-");
        holder.tvPriority.setText(t.getPriority() != null ? t.getPriority().getDisplayName() : "-");

        long due = t.getDueDate();
        holder.tvDueDate.setText(due > 0 ? ("Due: " + sdf.format(new Date(due))) : "Due: -");

        // clic sur item -> détails
        holder.itemView.setOnClickListener(v -> {
            if (onTaskClick != null) onTaskClick.onClick(t);
        });

        // menu 3 points -> edit/delete
        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMenu);
            popup.getMenuInflater().inflate(R.menu.menu_task_item, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_edit) {
                    if (onTaskEdit != null) onTaskEdit.onEdit(t);
                    return true;
                }

                if (id == R.id.action_delete) {
                    if (onTaskDelete != null) onTaskDelete.onDelete(t);
                    return true;
                }

                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvPriority, tvDueDate;
        ImageView btnMenu;

        TaskVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }

}

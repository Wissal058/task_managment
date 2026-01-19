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
import com.example.taskmanagment.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> {

    public interface OnUserClick {
        void onClick(User user);
    }

    public interface OnUserEdit {
        void onEdit(User user);
    }

    public interface OnUserDelete {
        void onDelete(User user);
    }

    private final Context context;
    private final OnUserClick onUserClick;
    private final OnUserEdit onUserEdit;
    private final OnUserDelete onUserDelete;

    private final List<User> users = new ArrayList<>();

    public UserAdapter(Context context,
                       OnUserClick onUserClick,
                       OnUserEdit onUserEdit,
                       OnUserDelete onUserDelete) {
        this.context = context;
        this.onUserClick = onUserClick;
        this.onUserEdit = onUserEdit;
        this.onUserDelete = onUserDelete;
    }

    public void submit(List<User> newList) {
        users.clear();
        if (newList != null) users.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH holder, int position) {
        User u = users.get(position);

        holder.tvFullName.setText(u.getFullName() != null ? u.getFullName() : "-");
        holder.tvEmail.setText(u.getEmail() != null ? u.getEmail() : "-");
        holder.tvType.setText(u.getUserType() != null ? u.getUserType().name() : "-");

        holder.itemView.setOnClickListener(v -> {
            if (onUserClick != null) onUserClick.onClick(u);
        });

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMenu);
            popup.getMenuInflater().inflate(R.menu.menu_user_item, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_edit_user) {
                    if (onUserEdit != null) onUserEdit.onEdit(u);
                    return true;
                }
                if (id == R.id.action_delete_user) {
                    if (onUserDelete != null) onUserDelete.onDelete(u);
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvType;
        ImageView btnMenu;

        UserVH(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvType = itemView.findViewById(R.id.tvType);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
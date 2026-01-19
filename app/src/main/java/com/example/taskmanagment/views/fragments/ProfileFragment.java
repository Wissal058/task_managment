package com.example.taskmanagment.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.controllers.TaskController;
import com.example.taskmanagment.database.TaskDAO;
import com.example.taskmanagment.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment pour afficher le profil de l'utilisateur
 */
public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvUserType, tvMemberSince;
    private TextView tvTotalTasks, tvCompletedTasks, tvPendingTasks, tvCompletionRate;
    private CardView cardChangePassword;

    private AuthController authController;
    private TaskController taskController;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialiser les contrôleurs
        authController = new AuthController(requireContext());
        taskController = new TaskController(requireContext());
        currentUser = authController.getCurrentUser();

        // Initialiser les vues
        initViews(view);

        // Charger les données
        loadProfileData();

        // Configurer les boutons
        setupButtons();

        return view;
    }

    /**
     * Initialiser les vues
     */
    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserType = view.findViewById(R.id.tvUserType);
        tvMemberSince = view.findViewById(R.id.tvMemberSince);

        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvPendingTasks = view.findViewById(R.id.tvPendingTasks);
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate);

        cardChangePassword = view.findViewById(R.id.cardChangePassword);
    }

    /**
     * Charger les données du profil
     */
    private void loadProfileData() {
        // Informations utilisateur
        tvUserName.setText(currentUser.getFullName());
        tvUserEmail.setText(currentUser.getEmail());
        tvUserType.setText(currentUser.getUserType().getDisplayName());

        // Date d'inscription
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String memberSince = sdf.format(new Date(currentUser.getCreatedDate()));
        tvMemberSince.setText(getString(R.string.member_since) + " " + memberSince);

        // Statistiques des tâches
        TaskDAO.TaskStatistics stats = taskController.getUserTaskStatistics(currentUser.getId());

        tvTotalTasks.setText(String.valueOf(stats.total));
        tvCompletedTasks.setText(String.valueOf(stats.completed));
        tvPendingTasks.setText(String.valueOf(stats.pending));

        // Taux de complétion
        int completionRate = stats.total > 0 ? (stats.completed * 100) / stats.total : 0;
        tvCompletionRate.setText(completionRate + "%");
    }

    /**
     * Configurer les boutons
     */
    private void setupButtons() {
        cardChangePassword.setOnClickListener(v -> {
            // TODO: Implémenter le changement de mot de passe
            android.widget.Toast.makeText(requireContext(), R.string.feature_coming_soon, android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}
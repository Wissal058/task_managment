package com.example.taskmanagment.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.controllers.AuthController;
import com.example.taskmanagment.utils.LanguageManager;
import com.example.taskmanagment.views.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    private TextView tvCurrentLanguage, tvAbout;
    private MaterialButton btnChangeLanguage, btnLogout;

    private AuthController authController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        authController = new AuthController(requireContext());

        tvCurrentLanguage = v.findViewById(R.id.tvCurrentLanguage);
        tvAbout = v.findViewById(R.id.tvAbout);
        btnChangeLanguage = v.findViewById(R.id.btnChangeLanguage);
        btnLogout = v.findViewById(R.id.btnLogout);

        updateLanguageLabel();

        tvAbout.setText("TaskManagement - Version 1.0");

        btnChangeLanguage.setOnClickListener(view -> showLanguageDialog());
        btnLogout.setOnClickListener(view -> showLogoutDialog());

        return v;
    }

    private void updateLanguageLabel() {
        String lang = LanguageManager.getLanguage(requireContext());
        String label = lang.equals(LanguageManager.LANGUAGE_ARABIC) ? "العربية" : "Français";
        tvCurrentLanguage.setText(getString(R.string.change_language) + " : " + label);
    }

    private void showLanguageDialog() {
        String[] languages = {"Français", "العربية"};

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    String langCode = (which == 0) ? LanguageManager.LANGUAGE_FRENCH : LanguageManager.LANGUAGE_ARABIC;
                    LanguageManager.setLocale(requireContext(), langCode);

                    // Recréer l'activité pour appliquer la langue partout
                    requireActivity().recreate();
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton(getString(R.string.yes), (d, w) -> {
                    authController.logout();
                    Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
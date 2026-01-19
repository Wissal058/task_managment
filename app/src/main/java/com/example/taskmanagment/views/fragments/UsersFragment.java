package com.example.taskmanagment.views.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagment.R;
import com.example.taskmanagment.adapters.UserAdapter;
import com.example.taskmanagment.controllers.UserController;
import com.example.taskmanagment.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private com.google.android.material.textfield.TextInputEditText etSearchUser;
    private RecyclerView rvUsers;
    private TextView tvEmptyUsers;

    private UserController userController;
    private UserAdapter adapter;

    private final List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_users, container, false);

        userController = new UserController(requireContext());

        etSearchUser = v.findViewById(R.id.etSearchUser);
        rvUsers = v.findViewById(R.id.rvUsers);
        tvEmptyUsers = v.findViewById(R.id.tvEmptyUsers);

        setupRecycler();
        setupSearch();

        loadUsers();
        applyFilter("");

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
        applyFilter(getQuery());
    }

    private void setupRecycler() {
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserAdapter(
                requireContext(),
                user -> { /* optionnel: ouvrir un détail user */ },
                this::showEditUserDialog,
                this::confirmDeleteUser
        );

        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        allUsers.clear();
        allUsers.addAll(userController.getAllUsers());
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();

        List<User> out = new ArrayList<>();
        for (User u : allUsers) {
            String name = u.getFullName() != null ? u.getFullName().toLowerCase() : "";
            String username = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
            String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";

            if (name.contains(q) || username.contains(q) || email.contains(q)) {
                out.add(u);
            }
        }

        adapter.submit(out);

        boolean empty = out.isEmpty();
        tvEmptyUsers.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String getQuery() {
        return etSearchUser.getText() != null ? etSearchUser.getText().toString() : "";
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer utilisateur")
                .setMessage("Voulez-vous supprimer : " + (user.getFullName() != null ? user.getFullName() : "Utilisateur") + " ?")
                .setPositiveButton("Supprimer", (d, w) -> deleteUser(user))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteUser(User user) {
        boolean ok = userController.deleteUser(user.getId());
        if (ok) {
            Toast.makeText(requireContext(), "Utilisateur supprimé ✅", Toast.LENGTH_SHORT).show();
            loadUsers();
            applyFilter(getQuery());
        } else {
            Toast.makeText(requireContext(), "Suppression échouée ❌", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditUser(User user) {
        // ✅ Ici tu peux ouvrir un EditUserFragment
        // Pour l’instant je te mets un exemple simple (à remplacer):
        Toast.makeText(requireContext(), "Edit user: " + user.getUsername(), Toast.LENGTH_SHORT).show();

        // Exemple si tu crées EditUserFragment:
        // EditUserFragment f = EditUserFragment.newInstance(user.getId());
        // requireActivity().getSupportFragmentManager()
        //         .beginTransaction()
        //         .replace(R.id.fragment_container, f)
        //         .addToBackStack("edit_user")
        //         .commit();
    }
    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_user, null);

        com.google.android.material.textfield.TextInputEditText etFullName =
                dialogView.findViewById(R.id.etFullName);
        com.google.android.material.textfield.TextInputEditText etEmail =
                dialogView.findViewById(R.id.etEmail);

        etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Modifier utilisateur")
                        .setView(dialogView)
                        .setPositiveButton("Enregistrer", null) // on override après
                        .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                        .create();

        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String newFullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
                String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

                if (newFullName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.setFullName(newFullName);
                user.setEmail(newEmail);

                boolean ok = userController.updateUser(user);
                if (ok) {
                    Toast.makeText(requireContext(), "Utilisateur modifié ✅", Toast.LENGTH_SHORT).show();
                    loadUsers(); // recharge ta liste
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Modification échouée ❌", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
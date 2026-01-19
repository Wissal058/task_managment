package com.example.taskmanagment.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskmanagment.R;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;
import com.example.taskmanagment.utils.XMLParser;
import com.example.taskmanagment.utils.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddUserFragment extends Fragment {

    private EditText etUsername, etPassword, etEmail, etFullName;
    private Spinner spUserType; // optionnel si tu veux choisir ADMIN/EMPLOYEE
    private Button btnSave;

    public AddUserFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_user, container, false);

        etUsername = v.findViewById(R.id.et_username);
        etPassword = v.findViewById(R.id.et_password);
        etEmail = v.findViewById(R.id.et_email);
        etFullName = v.findViewById(R.id.et_fullname);
        btnSave = v.findViewById(R.id.btn_save_user);

        btnSave.setOnClickListener(view -> saveUser());

        return v;
    }

    private void saveUser() {
        Context ctx = requireContext();

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(ctx, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Charger users
        List<User> users = loadUsers(ctx);

        // 2) Vérifier si username existe déjà
        for (User u : users) {
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                Toast.makeText(ctx, R.string.username_exists, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 3) Créer user + ID auto
        int nextId = getNextUserId(users);

        User newUser = new User();
        newUser.setId(String.valueOf(nextId));
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setCreatedDate(System.currentTimeMillis());

        // Type (si spinner موجود)
        // Exemple: "ADMIN" / "EMPLOYEE"
        UserType type = UserType.EMPLOYEE;
        if (spUserType != null) {
            String selected = String.valueOf(spUserType.getSelectedItem());
            if ("ADMIN".equalsIgnoreCase(selected)) type = UserType.ADMIN;
            else if ("EMPLOYEE".equalsIgnoreCase(selected)) type = UserType.EMPLOYEE;
        }
        newUser.setUserType(type);

        users.add(newUser);

        // 4) Écrire dans Internal Storage -> users.xml
        boolean ok = XMLWriter.writeUsersToXML(ctx, users, "users.xml");

        if (ok) {
            Toast.makeText(ctx, R.string.user_added_success, Toast.LENGTH_SHORT).show();
            // revenir au fragment précédent
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(ctx, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lire users depuis filesDir/users.xml s'il existe, sinon depuis raw/users.xml
     */
    private List<User> loadUsers(Context ctx) {
        try {
            File internalFile = new File(ctx.getFilesDir(), "users.xml");
            InputStream is;

            if (internalFile.exists()) {
                is = new FileInputStream(internalFile);
            } else {
                // ⚠️ Remplace R.raw.users par le nom réel de ton fichier dans res/raw
                is = ctx.getResources().openRawResource(R.raw.users);
            }

            List<User> users = XMLParser.parseUsersXML(is);
            is.close();
            return users != null ? users : new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Calculer un nouvel ID (max + 1)
     */
    private int getNextUserId(List<User> users) {
        int max = 0;
        for (User u : users) {
            try {
                int id = Integer.parseInt(u.getId());
                if (id > max) max = id;
            } catch (Exception ignored) {}
        }
        return max + 1;
    }
}

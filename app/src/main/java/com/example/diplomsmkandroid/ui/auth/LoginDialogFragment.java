package com.example.diplomsmkandroid.ui.auth;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.util.SecurityUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_password);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        MaterialButton btnGoRegister = view.findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (email.isEmpty() || password.isEmpty()) {
                tvError.setText("Заполните все поля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnLogin.setEnabled(false);
            AppDatabase db = AppDatabase.getInstance(requireContext());
            new Thread(() -> {
                UserEntity user = db.userDao().getByEmail(email);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        if (user == null || !SecurityUtil.verifyPassword(password, user.passwordHash)) {
                            tvError.setText("Неверный email или пароль");
                            tvError.setVisibility(View.VISIBLE);
                            return;
                        }
                        // Auto-upgrade legacy hash to bcrypt
                        if (!user.passwordHash.startsWith("$2")) {
                            new Thread(() -> {
                                user.passwordHash = SecurityUtil.hashPassword(password);
                                db.userDao().update(user);
                            }).start();
                        }
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.setCurrentUser(user);
                            Snackbar.make(activity.findViewById(R.id.container),
                                    R.string.login_success, Snackbar.LENGTH_SHORT).show();
                        }
                        dismiss();
                    });
                }
            }).start();
        });

        btnGoRegister.setOnClickListener(v -> {
            dismiss();
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.showRegisterDialog();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

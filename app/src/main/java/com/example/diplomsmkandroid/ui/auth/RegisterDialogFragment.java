package com.example.diplomsmkandroid.ui.auth;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.FundEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.util.SecurityUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RegisterDialogFragment extends DialogFragment {

    private final String[] roleLabels = {"Донор", "Волонтёр", "Представитель фонда"};
    private final String[] roleValues = {"donor", "volunteer", "fund"};

    private List<FundEntity> fundList = new ArrayList<>();
    private List<String> fundNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etName = view.findViewById(R.id.et_name);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_password);
        TextInputEditText etCity = view.findViewById(R.id.et_city);
        Spinner spinnerRole = view.findViewById(R.id.spinner_role);
        Spinner spinnerFund = view.findViewById(R.id.spinner_fund);
        TextView tvFundLabel = view.findViewById(R.id.tv_fund_label);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnRegister = view.findViewById(R.id.btn_register);

        spinnerRole.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, roleLabels));

        // Load funds list for fund role
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            fundList = db.fundDao().getAllSync();
            fundNames.clear();
            for (FundEntity f : fundList) fundNames.add(f.name);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        spinnerFund.setAdapter(new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, fundNames)));
            }
        }).start();

        // Show/hide fund spinner based on role
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                boolean isFund = "fund".equals(roleValues[pos]);
                tvFundLabel.setVisibility(isFund ? View.VISIBLE : View.GONE);
                spinnerFund.setVisibility(isFund ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
            String role = roleValues[spinnerRole.getSelectedItemPosition()];

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvError.setText("Заполните обязательные поля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (password.length() < 8) {
                tvError.setText("Пароль должен содержать минимум 8 символов");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Fund role must select a fund
            Integer fundId = null;
            if ("fund".equals(role)) {
                if (fundList.isEmpty() || spinnerFund.getSelectedItemPosition() < 0) {
                    tvError.setText("Выберите фонд");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                fundId = fundList.get(spinnerFund.getSelectedItemPosition()).id;
            }

            btnRegister.setEnabled(false);
            final Integer fId = fundId;

            new Thread(() -> {
                UserEntity existing = db.userDao().getByEmail(email);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (existing != null) {
                            btnRegister.setEnabled(true);
                            tvError.setText("Пользователь с таким email уже существует");
                            tvError.setVisibility(View.VISIBLE);
                            return;
                        }

                        new Thread(() -> {
                            UserEntity user = new UserEntity();
                            user.email = email;
                            user.passwordHash = SecurityUtil.hashPassword(password);
                            user.fullName = name;
                            user.role = role;
                            user.city = city;
                            user.fundId = fId;
                            user.createdAt = System.currentTimeMillis();
                            long id = db.userDao().insert(user);
                            user.id = (int) id;

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    btnRegister.setEnabled(true);
                                    MainActivity activity = (MainActivity) getActivity();
                                    if (activity != null) {
                                        activity.setCurrentUser(user);
                                        Snackbar.make(activity.findViewById(R.id.container),
                                                R.string.register_success, Snackbar.LENGTH_SHORT).show();
                                    }
                                    dismiss();
                                });
                            }
                        }).start();
                    });
                }
            }).start();
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

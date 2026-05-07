package com.example.diplomsmkandroid.ui.dialogs;

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
import com.example.diplomsmkandroid.data.entity.DonationEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class DonateDialogFragment extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_USER_ID = "user_id";

    private Runnable onDonateListener;

    public static DonateDialogFragment newInstance(int projectId, int userId) {
        DonateDialogFragment f = new DonateDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        args.putInt(ARG_USER_ID, userId);
        f.setArguments(args);
        return f;
    }

    public void setOnDonateListener(Runnable listener) {
        this.onDonateListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int projectId = getArguments().getInt(ARG_PROJECT_ID);
        int userId = getArguments().getInt(ARG_USER_ID);

        TextView tvProjectTitle = view.findViewById(R.id.tv_project_title);
        TextInputEditText etAmount = view.findViewById(R.id.et_amount);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnDonate = view.findViewById(R.id.btn_donate);
        MaterialButton btn500 = view.findViewById(R.id.btn_500);
        MaterialButton btn1000 = view.findViewById(R.id.btn_1000);
        MaterialButton btn5000 = view.findViewById(R.id.btn_5000);

        btn500.setOnClickListener(v -> etAmount.setText("500"));
        btn1000.setOnClickListener(v -> etAmount.setText("1000"));
        btn5000.setOnClickListener(v -> etAmount.setText("5000"));

        // Load project title
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            ProjectEntity p = db.projectDao().getById(projectId);
            if (p != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> tvProjectTitle.setText(p.title));
            }
        }).start();

        btnDonate.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                tvError.setText("Введите сумму");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                tvError.setText("Некорректная сумма");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            if (amount <= 0) {
                tvError.setText("Сумма должна быть больше нуля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnDonate.setEnabled(false);
            new Thread(() -> {
                DonationEntity donation = new DonationEntity();
                donation.userId = userId;
                donation.projectId = projectId;
                donation.amount = amount;
                donation.createdAt = System.currentTimeMillis();
                db.donationDao().insert(donation);

                // Update project collected
                ProjectEntity p = db.projectDao().getById(projectId);
                if (p != null) {
                    p.collected += amount;
                    db.projectDao().update(p);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnDonate.setEnabled(true);
                        Snackbar.make(getActivity().findViewById(R.id.container),
                                R.string.donate_success, Snackbar.LENGTH_SHORT).show();
                        if (onDonateListener != null) onDonateListener.run();
                        dismiss();
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

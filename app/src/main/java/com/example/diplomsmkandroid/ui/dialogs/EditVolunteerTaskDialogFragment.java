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
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class EditVolunteerTaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK_ID = "task_id";

    public static EditVolunteerTaskDialogFragment newInstance(int taskId) {
        EditVolunteerTaskDialogFragment f = new EditVolunteerTaskDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etDescription = view.findViewById(R.id.et_description);
        TextInputEditText etCategory = view.findViewById(R.id.et_category);
        TextInputEditText etCity = view.findViewById(R.id.et_city);
        TextInputEditText etSchedule = view.findViewById(R.id.et_schedule);
        TextInputEditText etSlots = view.findViewById(R.id.et_slots);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);

        int taskId = getArguments().getInt(ARG_TASK_ID);
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Load task data
        new Thread(() -> {
            VolunteerTaskEntity task = db.volunteerTaskDao().getById(taskId);
            if (task == null || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                etTitle.setText(task.title);
                etDescription.setText(task.description);
                etCategory.setText(task.category);
                etCity.setText(task.city);
                etSchedule.setText(task.schedule);
                etSlots.setText(String.valueOf(task.slotsTotal));
            });
        }).start();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
            String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
            String schedule = etSchedule.getText() != null ? etSchedule.getText().toString().trim() : "";
            String slotsStr = etSlots.getText() != null ? etSlots.getText().toString().trim() : "";

            if (title.isEmpty() || desc.isEmpty()) {
                tvError.setText("Заполните обязательные поля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            int slots;
            try {
                slots = slotsStr.isEmpty() ? 10 : Integer.parseInt(slotsStr);
            } catch (NumberFormatException e) {
                tvError.setText("Некорректное число мест");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnSave.setEnabled(false);
            new Thread(() -> {
                VolunteerTaskEntity task = db.volunteerTaskDao().getById(taskId);
                if (task != null) {
                    task.title = title;
                    task.description = desc;
                    task.category = category;
                    task.city = city;
                    task.schedule = schedule;
                    task.slotsTotal = slots;
                    db.volunteerTaskDao().update(task);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Snackbar.make(getActivity().findViewById(R.id.container),
                                "Задача обновлена", Snackbar.LENGTH_SHORT).show();
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

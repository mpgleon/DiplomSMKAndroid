package com.example.diplomsmkandroid.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

public class CreateVolunteerTaskDialogFragment extends DialogFragment {

    private static final String ARG_FUND_ID = "fund_id";

    private final String[] categories = {"Экология", "Дети и молодёжь", "Животные", "Пожилые люди", "Культура", "Медицина", "Образование"};

    public static CreateVolunteerTaskDialogFragment newInstance(int fundId) {
        CreateVolunteerTaskDialogFragment f = new CreateVolunteerTaskDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FUND_ID, fundId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int fundId = getArguments().getInt(ARG_FUND_ID);

        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etDescription = view.findViewById(R.id.et_description);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        TextInputEditText etCity = view.findViewById(R.id.et_city);
        TextInputEditText etSchedule = view.findViewById(R.id.et_schedule);
        TextInputEditText etSlots = view.findViewById(R.id.et_slots);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnCreate = view.findViewById(R.id.btn_create);

        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories));

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
            String schedule = etSchedule.getText() != null ? etSchedule.getText().toString().trim() : "";
            String slotsStr = etSlots.getText() != null ? etSlots.getText().toString().trim() : "";

            if (title.isEmpty() || desc.isEmpty() || city.isEmpty()) {
                tvError.setText("Заполните обязательные поля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            int slots;
            try {
                slots = slotsStr.isEmpty() ? 10 : Integer.parseInt(slotsStr);
                if (slots < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                tvError.setText("Количество мест должно быть >= 1");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnCreate.setEnabled(false);
            String category = categories[spinnerCategory.getSelectedItemPosition()];

            AppDatabase db = AppDatabase.getInstance(requireContext());
            final int finalSlots = slots;
            new Thread(() -> {
                VolunteerTaskEntity t = new VolunteerTaskEntity();
                t.title = title;
                t.description = desc;
                t.category = category;
                t.city = city;
                t.schedule = schedule.isEmpty() ? "По договорённости" : schedule;
                t.slotsTotal = finalSlots;
                t.slotsTaken = 0;
                t.ratingPlus = 0;
                t.fundId = fundId;
                db.volunteerTaskDao().insert(t);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnCreate.setEnabled(true);
                        Snackbar.make(getActivity().findViewById(R.id.container),
                                "Задача создана!", Snackbar.LENGTH_SHORT).show();
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

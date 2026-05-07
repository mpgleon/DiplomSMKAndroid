package com.example.diplomsmkandroid.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CreateProjectDialogFragment extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_FUND_ID = "fund_id";

    private final String[] categories = {"Дети", "Медицина", "Экология", "Животные", "Образование", "Пожилые", "Культура"};
    private ImageView ivCover;
    private String selectedImagePath = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static CreateProjectDialogFragment newInstance(int userId, Integer fundId) {
        CreateProjectDialogFragment f = new CreateProjectDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putInt(ARG_FUND_ID, fundId != null ? fundId : -1);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            selectedImagePath = copyUriToInternal(uri);
                            if (selectedImagePath != null && ivCover != null) {
                                ivCover.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                                ivCover.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_create_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivCover = view.findViewById(R.id.iv_cover);
        MaterialButton btnPickImage = view.findViewById(R.id.btn_pick_image);
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etDescription = view.findViewById(R.id.et_description);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        TextInputEditText etRegion = view.findViewById(R.id.et_region);
        TextInputEditText etGoal = view.findViewById(R.id.et_goal);
        TextInputEditText etDays = view.findViewById(R.id.et_days);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnCreate = view.findViewById(R.id.btn_create);

        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories));

        int userId = getArguments().getInt(ARG_USER_ID);
        int fundIdArg = getArguments().getInt(ARG_FUND_ID, -1);
        Integer fundId = fundIdArg > 0 ? fundIdArg : null;

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String region = etRegion.getText() != null ? etRegion.getText().toString().trim() : "";
            String goalStr = etGoal.getText() != null ? etGoal.getText().toString().trim() : "";
            String daysStr = etDays.getText() != null ? etDays.getText().toString().trim() : "";

            if (title.isEmpty() || desc.isEmpty() || goalStr.isEmpty()) {
                tvError.setText("Заполните обязательные поля");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            double goal;
            int days;
            try {
                goal = Double.parseDouble(goalStr);
                days = daysStr.isEmpty() ? 30 : Integer.parseInt(daysStr);
            } catch (NumberFormatException e) {
                tvError.setText("Некорректные числовые значения");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (goal < 1000) {
                tvError.setText("Минимальная сумма сбора — 1 000 ₽");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            if (days < 3) {
                tvError.setText("Минимальный срок — 3 дня");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            if (days > 365) {
                tvError.setText("Максимальный срок — 365 дней");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnCreate.setEnabled(false);
            String category = categories[spinnerCategory.getSelectedItemPosition()];

            AppDatabase db = AppDatabase.getInstance(requireContext());
            new Thread(() -> {
                int dup = db.projectDao().countByTitle(title);
                if (dup > 0) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnCreate.setEnabled(true);
                            tvError.setText("Проект с таким названием уже существует");
                            tvError.setVisibility(View.VISIBLE);
                        });
                    }
                    return;
                }

                ProjectEntity p = new ProjectEntity();
                p.title = title;
                p.description = desc;
                p.category = category;
                p.region = region;
                p.goal = goal;
                p.collected = 0;
                p.daysLeft = days;
                p.urgent = false;
                p.fundId = fundId;
                p.creatorId = userId;
                p.coverPath = selectedImagePath;
                p.status = "active";
                p.createdAt = System.currentTimeMillis();
                db.projectDao().insert(p);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnCreate.setEnabled(true);
                        Snackbar.make(getActivity().findViewById(R.id.container),
                                "Проект создан!", Snackbar.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            }).start();
        });
    }

    private String copyUriToInternal(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return null;
            File dir = new File(requireContext().getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "project_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
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

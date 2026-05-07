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

public class EditProjectDialogFragment extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private final String[] categories = {"Дети", "Медицина", "Экология", "Животные", "Образование", "Пожилые", "Культура"};

    private ImageView ivCover;
    private String selectedImagePath = null;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static EditProjectDialogFragment newInstance(int projectId) {
        EditProjectDialogFragment f = new EditProjectDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
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
                            if (selectedImagePath != null) {
                                ivCover.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                            }
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivCover = view.findViewById(R.id.iv_cover);
        MaterialButton btnPickImage = view.findViewById(R.id.btn_pick_image);
        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etDescription = view.findViewById(R.id.et_description);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        TextInputEditText etRegion = view.findViewById(R.id.et_region);
        TextView tvError = view.findViewById(R.id.tv_error);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);
        MaterialButton btnClose = view.findViewById(R.id.btn_close_project);

        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories));

        int projectId = getArguments().getInt(ARG_PROJECT_ID);

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Load project data
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            ProjectEntity project = db.projectDao().getById(projectId);
            if (project == null || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                etTitle.setText(project.title);
                etDescription.setText(project.description);
                etRegion.setText(project.region);

                // Select category
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(project.category)) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }

                // Show existing cover
                if (project.coverPath != null) {
                    File f = new File(project.coverPath);
                    if (f.exists()) {
                        ivCover.setImageURI(Uri.fromFile(f));
                    }
                    selectedImagePath = project.coverPath;
                }

                // Close button — only if no donations
                double collected = project.collected;
                if (collected > 0) {
                    btnClose.setEnabled(false);
                    btnClose.setText("Нельзя закрыть");
                }
            });

            // Wire save
            btnSave.setOnClickListener(v -> {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
                String region = etRegion.getText() != null ? etRegion.getText().toString().trim() : "";

                if (title.isEmpty() || desc.isEmpty()) {
                    tvError.setText("Заполните обязательные поля");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }

                btnSave.setEnabled(false);
                new Thread(() -> {
                    ProjectEntity p = db.projectDao().getById(projectId);
                    if (p != null) {
                        p.title = title;
                        p.description = desc;
                        p.region = region;
                        p.category = categories[spinnerCategory.getSelectedItemPosition()];
                        if (selectedImagePath != null) {
                            p.coverPath = selectedImagePath;
                        }
                        db.projectDao().update(p);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            Snackbar.make(getActivity().findViewById(R.id.container),
                                    "Проект обновлён", Snackbar.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                }).start();
            });

            // Wire close project
            btnClose.setOnClickListener(v -> {
                new Thread(() -> {
                    ProjectEntity p = db.projectDao().getById(projectId);
                    if (p != null && p.collected <= 0) {
                        db.projectDao().delete(p);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Snackbar.make(getActivity().findViewById(R.id.container),
                                    "Проект закрыт", Snackbar.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                }).start();
            });
        }).start();
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

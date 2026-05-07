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
import com.example.diplomsmkandroid.data.entity.ReviewEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class ReviewDialogFragment extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_USER_ID = "user_id";

    private int selectedRating = 0;
    private TextView[] stars;

    public static ReviewDialogFragment newInstance(int projectId, int userId) {
        ReviewDialogFragment f = new ReviewDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        args.putInt(ARG_USER_ID, userId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int projectId = getArguments().getInt(ARG_PROJECT_ID);
        int userId = getArguments().getInt(ARG_USER_ID);

        TextInputEditText etText = view.findViewById(R.id.et_review_text);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit);

        stars = new TextView[]{
                view.findViewById(R.id.tv_star_1),
                view.findViewById(R.id.tv_star_2),
                view.findViewById(R.id.tv_star_3),
                view.findViewById(R.id.tv_star_4),
                view.findViewById(R.id.tv_star_5),
        };

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }

        btnSubmit.setOnClickListener(v -> {
            if (selectedRating == 0) {
                Snackbar.make(view, "Выберите оценку", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String text = etText.getText() != null ? etText.getText().toString().trim() : "";
            btnSubmit.setEnabled(false);

            AppDatabase db = AppDatabase.getInstance(requireContext());
            new Thread(() -> {
                ReviewEntity review = new ReviewEntity();
                review.userId = userId;
                review.projectId = projectId;
                review.rating = selectedRating;
                review.text = text;
                review.createdAt = System.currentTimeMillis();
                db.reviewDao().insert(review);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        Snackbar.make(getActivity().findViewById(R.id.container),
                                R.string.review_success, Snackbar.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            }).start();
        });
    }

    private void setRating(int rating) {
        selectedRating = rating;
        int activeColor = requireContext().getColor(R.color.accent);
        int inactiveColor = requireContext().getColor(R.color.border);
        for (int i = 0; i < 5; i++) {
            stars[i].setTextColor(i < rating ? activeColor : inactiveColor);
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

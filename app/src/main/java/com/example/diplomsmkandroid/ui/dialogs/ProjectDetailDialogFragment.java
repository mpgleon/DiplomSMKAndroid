package com.example.diplomsmkandroid.ui.dialogs;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.data.entity.ReviewEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.ReviewAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectDetailDialogFragment extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";

    public static ProjectDetailDialogFragment newInstance(int projectId) {
        ProjectDetailDialogFragment f = new ProjectDetailDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_project_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int projectId = getArguments().getInt(ARG_PROJECT_ID);
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));

        TextView tvCategory = view.findViewById(R.id.tv_category);
        TextView tvUrgent = view.findViewById(R.id.tv_urgent);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        TextView tvCollected = view.findViewById(R.id.tv_collected);
        TextView tvDaysLeft = view.findViewById(R.id.tv_days_left);
        LinearLayout layoutRating = view.findViewById(R.id.layout_rating);
        TextView tvRating = view.findViewById(R.id.tv_rating);
        TextView tvReviewsCount = view.findViewById(R.id.tv_reviews_count);
        TextView tvReviewsTitle = view.findViewById(R.id.tv_reviews_title);
        ImageView ivCover = view.findViewById(R.id.iv_cover);
        RecyclerView rvReviews = view.findViewById(R.id.rv_reviews);
        MaterialButton btnDonate = view.findViewById(R.id.btn_donate);
        MaterialButton btnReview = view.findViewById(R.id.btn_review);

        ReviewAdapter reviewAdapter = new ReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            ProjectEntity p = db.projectDao().getById(projectId);
            double avgRating = db.reviewDao().getAvgRating(projectId);
            int reviewsCount = db.reviewDao().getCountForProject(projectId);
            List<ReviewEntity> reviews = db.reviewDao().getByProjectSync(projectId);

            Map<Integer, String> userNames = new HashMap<>();
            for (ReviewEntity r : reviews) {
                if (!userNames.containsKey(r.userId)) {
                    UserEntity u = db.userDao().getById(r.userId);
                    userNames.put(r.userId, u != null ? u.fullName : "Пользователь");
                }
            }

            if (p != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvCategory.setText(p.category);
                    tvTitle.setText(p.title);
                    tvDescription.setText(p.description);
                    progressBar.setMax(100);
                    progressBar.setProgress(p.getPercent());
                    tvCollected.setText("₽" + fmt.format((long) p.collected) + " / ₽" + fmt.format((long) p.goal));
                    tvDaysLeft.setText(p.daysLeft + " дн. осталось");
                    tvUrgent.setVisibility(p.urgent ? View.VISIBLE : View.GONE);

                    // Cover image
                    if (p.coverPath != null && !p.coverPath.isEmpty()) {
                        ivCover.setVisibility(View.VISIBLE);
                        try {
                            if (p.coverPath.startsWith("content://") || p.coverPath.startsWith("http")) {
                                ivCover.setImageURI(Uri.parse(p.coverPath));
                            } else {
                                ivCover.setImageURI(Uri.fromFile(new File(p.coverPath)));
                            }
                        } catch (Exception e) {
                            ivCover.setVisibility(View.GONE);
                        }
                    }

                    if (reviewsCount > 0) {
                        layoutRating.setVisibility(View.VISIBLE);
                        tvRating.setText(String.format(Locale.US, "★ %.1f", avgRating));
                        tvReviewsCount.setText("(" + reviewsCount + " отзывов)");
                        tvReviewsTitle.setVisibility(View.VISIBLE);
                        reviewAdapter.setItems(reviews, userNames);
                    }
                });
            }
        }).start();

        btnDonate.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null) return;
            UserEntity user = activity.getCurrentUser();
            if (user == null) {
                Snackbar.make(view, R.string.need_login, Snackbar.LENGTH_SHORT).show();
                activity.showLoginDialog();
                return;
            }
            dismiss();
            DonateDialogFragment dialog = DonateDialogFragment.newInstance(projectId, user.id);
            dialog.show(getParentFragmentManager(), "donate");
        });

        btnReview.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null) return;
            UserEntity user = activity.getCurrentUser();
            if (user == null) {
                Snackbar.make(view, R.string.need_login, Snackbar.LENGTH_SHORT).show();
                activity.showLoginDialog();
                return;
            }
            dismiss();
            ReviewDialogFragment dialog = ReviewDialogFragment.newInstance(projectId, user.id);
            dialog.show(getParentFragmentManager(), "review");
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

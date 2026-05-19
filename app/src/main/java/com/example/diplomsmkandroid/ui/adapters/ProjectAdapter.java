package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    public interface OnProjectAction {
        void onDonate(int projectId);
        void onDetails(int projectId);
    }

    private List<ProjectEntity> items = new ArrayList<>();
    private OnProjectAction listener;
    private final NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));
    private Map<Integer, double[]> ratings = new HashMap<>();

    public ProjectAdapter(OnProjectAction listener) {
        this.listener = listener;
    }

    public void setItems(List<ProjectEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setRatings(Map<Integer, double[]> ratings) {
        this.ratings = ratings != null ? ratings : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ProjectEntity p = items.get(position);
        h.tvCategory.setText(p.category);
        h.tvTitle.setText(p.title);
        h.tvDescription.setText(p.description);
        h.progressBar.setMax(100);
        h.progressBar.setProgress(p.getPercent());
        h.tvCollected.setText("₽" + fmt.format((long) p.collected) + " / ₽" + fmt.format((long) p.goal));
        h.tvDaysLeft.setText(p.daysLeft + " дн.");

        if (p.urgent) {
            h.tvUrgent.setVisibility(View.VISIBLE);
        } else {
            h.tvUrgent.setVisibility(View.GONE);
        }

        // Cover image
        if (p.coverPath != null && !p.coverPath.isEmpty()) {
            h.ivCover.setVisibility(View.VISIBLE);
            Object source = p.coverPath.startsWith("http") || p.coverPath.startsWith("content://")
                    ? p.coverPath : new File(p.coverPath);
            Glide.with(h.itemView.getContext())
                    .load(source)
                    .centerCrop()
                    .into(h.ivCover);
        } else {
            h.ivCover.setVisibility(View.GONE);
            Glide.with(h.itemView.getContext()).clear(h.ivCover);
        }

        // Rating
        double[] ratingData = ratings.get(p.id);
        if (ratingData != null && ratingData[1] > 0) {
            h.layoutRating.setVisibility(View.VISIBLE);
            h.tvRating.setText(String.format(Locale.US, "★ %.1f", ratingData[0]));
            h.tvReviewsCount.setText("(" + (int) ratingData[1] + " отзывов)");
        } else {
            h.layoutRating.setVisibility(View.GONE);
        }

        h.btnDonate.setOnClickListener(v -> {
            if (listener != null) listener.onDonate(p.id);
        });
        h.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(p.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvCategory, tvUrgent, tvTitle, tvDescription, tvFund, tvCollected, tvDaysLeft, tvRating, tvReviewsCount;
        LinearLayout layoutRating;
        ProgressBar progressBar;
        MaterialButton btnDonate, btnDetails;

        ViewHolder(View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_cover);
            tvCategory = v.findViewById(R.id.tv_category);
            tvUrgent = v.findViewById(R.id.tv_urgent);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvFund = v.findViewById(R.id.tv_fund);
            tvCollected = v.findViewById(R.id.tv_collected);
            tvDaysLeft = v.findViewById(R.id.tv_days_left);
            layoutRating = v.findViewById(R.id.layout_rating);
            tvRating = v.findViewById(R.id.tv_rating);
            tvReviewsCount = v.findViewById(R.id.tv_reviews_count);
            progressBar = v.findViewById(R.id.progress_bar);
            btnDonate = v.findViewById(R.id.btn_donate);
            btnDetails = v.findViewById(R.id.btn_details);
        }
    }
}

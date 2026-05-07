package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.ReviewEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewEntity> items = new ArrayList<>();
    private Map<Integer, String> userNames;

    public void setItems(List<ReviewEntity> list, Map<Integer, String> userNames) {
        this.items = list != null ? list : new ArrayList<>();
        this.userNames = userNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ReviewEntity r = items.get(position);

        String name = userNames != null && userNames.containsKey(r.userId)
                ? userNames.get(r.userId) : "Пользователь";
        h.tvUserName.setText(name);

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < r.rating ? "★" : "☆");
        }
        h.tvRating.setText(stars.toString());

        if (r.text != null && !r.text.trim().isEmpty()) {
            h.tvReviewText.setVisibility(View.VISIBLE);
            h.tvReviewText.setText(r.text);
        } else {
            h.tvReviewText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvRating, tvReviewText;

        ViewHolder(View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tv_user_name);
            tvRating = v.findViewById(R.id.tv_rating);
            tvReviewText = v.findViewById(R.id.tv_review_text);
        }
    }
}

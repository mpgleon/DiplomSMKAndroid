package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.TestimonialEntity;

import java.util.ArrayList;
import java.util.List;

public class TestimonialAdapter extends RecyclerView.Adapter<TestimonialAdapter.ViewHolder> {

    private List<TestimonialEntity> items = new ArrayList<>();

    public void setItems(List<TestimonialEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_testimonial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        TestimonialEntity t = items.get(position);
        h.tvText.setText(t.text);
        h.tvAuthor.setText(t.author);
        h.tvRoleCity.setText(t.role + " · " + t.city);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvAuthor, tvRoleCity;

        ViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tv_text);
            tvAuthor = v.findViewById(R.id.tv_author);
            tvRoleCity = v.findViewById(R.id.tv_role_city);
        }
    }
}

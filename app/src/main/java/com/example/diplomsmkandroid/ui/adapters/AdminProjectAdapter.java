package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectAdapter extends RecyclerView.Adapter<AdminProjectAdapter.ViewHolder> {

    public interface OnProjectAction {
        void onActivate(ProjectEntity project);
        void onArchive(ProjectEntity project);
    }

    private List<ProjectEntity> items = new ArrayList<>();
    private final OnProjectAction listener;

    public AdminProjectAdapter(OnProjectAction listener) {
        this.listener = listener;
    }

    public void setItems(List<ProjectEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_project, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ProjectEntity p = items.get(position);
        h.tvTitle.setText(p.title);
        h.tvCategory.setText(p.category + " · " + (p.region != null ? p.region : ""));

        String statusText;
        switch (p.status) {
            case "completed": statusText = "Завершён"; break;
            case "archived": statusText = "В архиве"; break;
            default: statusText = "Активен"; break;
        }
        h.tvStatus.setText(statusText);

        boolean isActive = "active".equals(p.status);
        h.btnActivate.setEnabled(!isActive);
        h.btnArchive.setEnabled(isActive);

        h.btnActivate.setOnClickListener(v -> {
            if (listener != null) listener.onActivate(p);
        });
        h.btnArchive.setOnClickListener(v -> {
            if (listener != null) listener.onArchive(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus;
        MaterialButton btnActivate, btnArchive;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvCategory = v.findViewById(R.id.tv_category);
            tvStatus = v.findViewById(R.id.tv_status);
            btnActivate = v.findViewById(R.id.btn_activate);
            btnArchive = v.findViewById(R.id.btn_archive);
        }
    }
}

package com.example.diplomsmkandroid.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.VolunteerApplicationEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;

import java.util.ArrayList;
import java.util.List;

public class MyApplicationAdapter extends RecyclerView.Adapter<MyApplicationAdapter.ViewHolder> {

    private List<VolunteerApplicationEntity> items = new ArrayList<>();
    private AppDatabase db;

    public void setItems(List<VolunteerApplicationEntity> list, AppDatabase db) {
        this.items = list != null ? list : new ArrayList<>();
        this.db = db;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_application, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VolunteerApplicationEntity app = items.get(position);

        // Load task title async
        if (db != null) {
            new Thread(() -> {
                VolunteerTaskEntity task = db.volunteerTaskDao().getById(app.taskId);
                h.itemView.post(() -> {
                    if (task != null) {
                        h.tvTaskTitle.setText(task.title);
                        h.tvTaskOrg.setText(task.category + " · " + task.city);
                    } else {
                        h.tvTaskTitle.setText("Задача #" + app.taskId);
                        h.tvTaskOrg.setText("");
                    }
                });
            }).start();
        }

        // Status display
        switch (app.status) {
            case "accepted":
                h.tvStatus.setText("Принята");
                h.tvStatus.setTextColor(Color.parseColor("#16a34a"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#dcfce7"));
                break;
            case "rejected":
                h.tvStatus.setText("Отклонена");
                h.tvStatus.setTextColor(Color.parseColor("#dc2626"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#fee2e2"));
                break;
            default:
                h.tvStatus.setText("В ожидании");
                h.tvStatus.setTextColor(Color.parseColor("#ca8a04"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#fef9c3"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskOrg, tvStatus;

        ViewHolder(View v) {
            super(v);
            tvTaskTitle = v.findViewById(R.id.tv_task_title);
            tvTaskOrg = v.findViewById(R.id.tv_task_org);
            tvStatus = v.findViewById(R.id.tv_status);
        }
    }
}

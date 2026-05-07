package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerApplicationEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VolunteerApplicationAdapter extends RecyclerView.Adapter<VolunteerApplicationAdapter.ViewHolder> {

    public interface OnApplicationAction {
        void onAccept(int applicationId);
        void onReject(int applicationId);
    }

    private List<VolunteerApplicationEntity> items = new ArrayList<>();
    private AppDatabase db;
    private OnApplicationAction listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru"));

    public VolunteerApplicationAdapter(OnApplicationAction listener) {
        this.listener = listener;
    }

    public void setItems(List<VolunteerApplicationEntity> list, AppDatabase database) {
        this.items = list != null ? list : new ArrayList<>();
        this.db = database;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_volunteer_application, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VolunteerApplicationEntity app = items.get(position);
        h.tvDate.setText(dateFormat.format(new Date(app.createdAt)));

        String statusText;
        switch (app.status) {
            case "accepted": statusText = "Принята"; break;
            case "rejected": statusText = "Отклонена"; break;
            default: statusText = "Ожидает"; break;
        }
        h.tvStatus.setText(statusText);

        boolean isPending = "pending".equals(app.status);
        h.btnAccept.setVisibility(isPending ? View.VISIBLE : View.GONE);
        h.btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);

        h.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(app.id);
        });
        h.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(app.id);
        });

        // Async load user and task names
        if (db != null) {
            new Thread(() -> {
                UserEntity user = db.userDao().getById(app.userId);
                VolunteerTaskEntity task = db.volunteerTaskDao().getById(app.taskId);
                String userName = user != null ? user.fullName : "Пользователь #" + app.userId;
                String taskName = task != null ? task.title : "Задача #" + app.taskId;
                h.tvUserName.post(() -> {
                    h.tvUserName.setText(userName);
                    h.tvTaskName.setText(taskName);
                });
            }).start();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTaskName, tvDate, tvStatus;
        MaterialButton btnAccept, btnReject;

        ViewHolder(View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tv_user_name);
            tvTaskName = v.findViewById(R.id.tv_task_name);
            tvDate = v.findViewById(R.id.tv_date);
            tvStatus = v.findViewById(R.id.tv_status);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnReject = v.findViewById(R.id.btn_reject);
        }
    }
}

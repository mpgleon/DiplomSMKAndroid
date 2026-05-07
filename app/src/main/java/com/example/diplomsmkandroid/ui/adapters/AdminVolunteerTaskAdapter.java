package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminVolunteerTaskAdapter extends RecyclerView.Adapter<AdminVolunteerTaskAdapter.ViewHolder> {

    public interface OnTaskAction {
        void onApprove(VolunteerTaskEntity task);
        void onReject(VolunteerTaskEntity task);
    }

    private List<VolunteerTaskEntity> items = new ArrayList<>();
    private final OnTaskAction listener;

    public AdminVolunteerTaskAdapter(OnTaskAction listener) {
        this.listener = listener;
    }

    public void setItems(List<VolunteerTaskEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_volunteer_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VolunteerTaskEntity t = items.get(position);
        h.tvTitle.setText(t.title);
        h.tvCategory.setText(t.category + " · " + (t.city != null ? t.city : ""));

        String statusText;
        switch (t.status != null ? t.status : "pending") {
            case "active": statusText = "Одобрено"; break;
            case "rejected": statusText = "Отклонено"; break;
            default: statusText = "На модерации"; break;
        }
        h.tvStatus.setText(statusText);

        boolean isPending = "pending".equals(t.status) || t.status == null;
        h.btnApprove.setEnabled(!"active".equals(t.status));
        h.btnReject.setEnabled(!"rejected".equals(t.status));

        h.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(t);
        });
        h.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus;
        MaterialButton btnApprove, btnReject;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvCategory = v.findViewById(R.id.tv_category);
            tvStatus = v.findViewById(R.id.tv_status);
            btnApprove = v.findViewById(R.id.btn_approve);
            btnReject = v.findViewById(R.id.btn_reject);
        }
    }
}

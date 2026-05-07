package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class VolunteerTaskAdapter extends RecyclerView.Adapter<VolunteerTaskAdapter.ViewHolder> {

    public interface OnTaskAction {
        void onApply(int taskId);
    }

    private List<VolunteerTaskEntity> items = new ArrayList<>();
    private OnTaskAction listener;

    public VolunteerTaskAdapter(OnTaskAction listener) {
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
                .inflate(R.layout.item_volunteer_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VolunteerTaskEntity t = items.get(position);
        h.tvCategory.setText(t.category);
        h.tvTitle.setText(t.title);
        h.tvDescription.setText(t.description);
        h.tvCity.setText(t.city);
        h.tvSchedule.setText(t.schedule);
        h.progressSlots.setMax(t.slotsTotal);
        h.progressSlots.setProgress(t.slotsTaken);
        h.tvSlots.setText(t.slotsTaken + " / " + t.slotsTotal + " мест занято");

        h.btnApply.setOnClickListener(v -> {
            if (listener != null) listener.onApply(t.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTitle, tvDescription, tvCity, tvSchedule, tvSlots;
        ProgressBar progressSlots;
        MaterialButton btnApply;

        ViewHolder(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCity = v.findViewById(R.id.tv_city);
            tvSchedule = v.findViewById(R.id.tv_schedule);
            tvSlots = v.findViewById(R.id.tv_slots);
            progressSlots = v.findViewById(R.id.progress_slots);
            btnApply = v.findViewById(R.id.btn_apply);
        }
    }
}

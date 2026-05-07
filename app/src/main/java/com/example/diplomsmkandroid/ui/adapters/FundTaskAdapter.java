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

public class FundTaskAdapter extends RecyclerView.Adapter<FundTaskAdapter.ViewHolder> {

    public interface OnTaskAction {
        void onEdit(VolunteerTaskEntity task);
        void onDelete(VolunteerTaskEntity task);
    }

    private List<VolunteerTaskEntity> items = new ArrayList<>();
    private final OnTaskAction listener;

    public FundTaskAdapter(OnTaskAction listener) {
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
                .inflate(R.layout.item_fund_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VolunteerTaskEntity t = items.get(position);
        h.tvTitle.setText(t.title);
        h.tvSlots.setText(t.slotsTaken + "/" + t.slotsTotal);
        h.tvInfo.setText(t.category + " · " + t.city + " · " + t.schedule);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(t);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSlots, tvInfo;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvSlots = v.findViewById(R.id.tv_slots);
            tvInfo = v.findViewById(R.id.tv_info);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}

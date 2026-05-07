package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.DonationEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    private List<DonationEntity> items = new ArrayList<>();
    private AppDatabase db;
    private final NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru"));

    public void setItems(List<DonationEntity> list, AppDatabase database) {
        this.items = list != null ? list : new ArrayList<>();
        this.db = database;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DonationEntity d = items.get(position);
        h.tvAmount.setText("₽" + fmt.format((long) d.amount));
        h.tvDate.setText(dateFormat.format(new Date(d.createdAt)));

        if (db != null) {
            new Thread(() -> {
                ProjectEntity p = db.projectDao().getById(d.projectId);
                String title = p != null ? p.title : "Проект #" + d.projectId;
                h.tvProjectTitle.post(() -> h.tvProjectTitle.setText(title));
            }).start();
        } else {
            h.tvProjectTitle.setText("Проект #" + d.projectId);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectTitle, tvDate, tvAmount;

        ViewHolder(View v) {
            super(v);
            tvProjectTitle = v.findViewById(R.id.tv_project_title);
            tvDate = v.findViewById(R.id.tv_date);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}

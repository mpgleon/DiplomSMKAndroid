package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.FundEntity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminFundAdapter extends RecyclerView.Adapter<AdminFundAdapter.ViewHolder> {

    public interface OnFundAction {
        void onToggleVerify(FundEntity fund);
    }

    private List<FundEntity> items = new ArrayList<>();
    private final OnFundAction listener;

    public AdminFundAdapter(OnFundAction listener) {
        this.listener = listener;
    }

    public void setItems(List<FundEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_fund, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FundEntity f = items.get(position);
        h.tvName.setText(f.name);
        h.tvCity.setText(f.city + " · с " + f.foundedYear + " г.");
        h.tvVerified.setText(f.verified ? "✓ Верифицирован" : "Не верифицирован");
        h.tvVerified.setVisibility(View.VISIBLE);
        h.btnToggle.setText(f.verified ? "Снять верификацию" : "Верифицировать");

        h.btnToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleVerify(f);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCity, tvVerified;
        MaterialButton btnToggle;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvCity = v.findViewById(R.id.tv_city);
            tvVerified = v.findViewById(R.id.tv_verified);
            btnToggle = v.findViewById(R.id.btn_toggle_verify);
        }
    }
}

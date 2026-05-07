package com.example.diplomsmkandroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnUserAction {
        void onChangeRole(UserEntity user, String newRole);
    }

    private final String[] roleLabels = {"Донор", "Волонтёр", "Фонд", "Админ"};
    private final String[] roleValues = {"donor", "volunteer", "fund", "admin"};

    private List<UserEntity> items = new ArrayList<>();
    private final OnUserAction listener;

    public AdminUserAdapter(OnUserAction listener) {
        this.listener = listener;
    }

    public void setItems(List<UserEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UserEntity u = items.get(position);
        h.tvName.setText(u.fullName);
        h.tvEmail.setText(u.email);

        String roleText;
        switch (u.role) {
            case "volunteer": roleText = "Волонтёр"; break;
            case "fund": roleText = "Фонд"; break;
            case "admin": roleText = "Админ"; break;
            default: roleText = "Донор"; break;
        }
        h.tvRole.setText(roleText);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(h.itemView.getContext(),
                android.R.layout.simple_spinner_dropdown_item, roleLabels);
        h.spinnerRole.setAdapter(adapter);

        // Pre-select current role
        for (int i = 0; i < roleValues.length; i++) {
            if (roleValues[i].equals(u.role)) {
                h.spinnerRole.setSelection(i);
                break;
            }
        }

        h.btnSaveRole.setOnClickListener(v -> {
            String newRole = roleValues[h.spinnerRole.getSelectedItemPosition()];
            if (!newRole.equals(u.role) && listener != null) {
                listener.onChangeRole(u, newRole);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        Spinner spinnerRole;
        MaterialButton btnSaveRole;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvEmail = v.findViewById(R.id.tv_email);
            tvRole = v.findViewById(R.id.tv_role);
            spinnerRole = v.findViewById(R.id.spinner_role);
            btnSaveRole = v.findViewById(R.id.btn_save_role);
        }
    }
}

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FundAdapter extends RecyclerView.Adapter<FundAdapter.ViewHolder> {

    public interface OnFundAction {
        void onDetails(int fundId);
    }

    private List<FundEntity> items = new ArrayList<>();
    private OnFundAction listener;
    private final NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));

    public FundAdapter(OnFundAction listener) {
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
                .inflate(R.layout.item_fund_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FundEntity f = items.get(position);
        h.tvName.setText(f.name);
        h.tvCityYear.setText(f.city + " · с " + f.foundedYear + " г.");
        h.tvDescription.setText(f.description);
        h.tvPeopleHelped.setText(fmt.format(f.peopleHelped));
        h.tvHelpedLabel.setText(f.peopleHelpedLabel);
        h.tvVolunteers.setText(String.valueOf(f.volunteersCount));

        if (f.verified) {
            h.tvVerified.setVisibility(View.VISIBLE);
        } else {
            h.tvVerified.setVisibility(View.GONE);
        }

        h.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(f.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvVerified, tvCityYear, tvDescription, tvPeopleHelped, tvHelpedLabel, tvVolunteers;
        MaterialButton btnDetails;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvVerified = v.findViewById(R.id.tv_verified);
            tvCityYear = v.findViewById(R.id.tv_city_year);
            tvDescription = v.findViewById(R.id.tv_description);
            tvPeopleHelped = v.findViewById(R.id.tv_people_helped);
            tvHelpedLabel = v.findViewById(R.id.tv_helped_label);
            tvVolunteers = v.findViewById(R.id.tv_volunteers);
            btnDetails = v.findViewById(R.id.btn_details);
        }
    }
}

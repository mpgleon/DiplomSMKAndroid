package com.example.diplomsmkandroid.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.DonationEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.DonationAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutNotLogged, layoutLogged;
    private TextView tvName, tvEmail, tvRole;
    private DonationAdapter donationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutNotLogged = view.findViewById(R.id.layout_not_logged);
        layoutLogged = view.findViewById(R.id.layout_logged);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvRole = view.findViewById(R.id.tv_role);

        RecyclerView rvDonations = view.findViewById(R.id.rv_donations);
        donationAdapter = new DonationAdapter();
        rvDonations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDonations.setAdapter(donationAdapter);

        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.showLoginDialog();
        });

        MaterialButton btnRegister = view.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.showRegisterDialog();
        });

        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.logout();
        });

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        UserEntity user = activity.getCurrentUser();
        if (user == null) {
            layoutNotLogged.setVisibility(View.VISIBLE);
            layoutLogged.setVisibility(View.GONE);
            return;
        }

        layoutNotLogged.setVisibility(View.GONE);
        layoutLogged.setVisibility(View.VISIBLE);

        tvName.setText(user.fullName);
        tvEmail.setText(user.email);
        String roleText;
        switch (user.role) {
            case "volunteer": roleText = "Волонтёр"; break;
            case "fund": roleText = "Фонд"; break;
            default: roleText = "Донор"; break;
        }
        tvRole.setText(roleText);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<DonationEntity> donations = db.donationDao().getRecentByUserSync(user.id);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> donationAdapter.setItems(donations, db));
            }
        }).start();
    }
}

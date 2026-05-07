package com.example.diplomsmkandroid.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.google.android.material.button.MaterialButton;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnRegister = view.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                if (activity.getCurrentUser() != null) {
                    activity.showProfileDialog();
                } else {
                    activity.showRegisterDialog();
                }
            }
        });
    }
}

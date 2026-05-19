package com.example.diplomsmkandroid.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.FundEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.ui.adapters.ProjectAdapter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FundDetailDialogFragment extends DialogFragment {

    private static final String ARG_FUND_ID = "fund_id";

    public static FundDetailDialogFragment newInstance(int fundId) {
        FundDetailDialogFragment f = new FundDetailDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FUND_ID, fundId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fund_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int fundId = getArguments().getInt(ARG_FUND_ID);
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));

        ImageView ivCover = view.findViewById(R.id.iv_cover);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvVerified = view.findViewById(R.id.tv_verified);
        TextView tvCityYear = view.findViewById(R.id.tv_city_year);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        TextView tvPeopleHelped = view.findViewById(R.id.tv_people_helped);
        TextView tvHelpedLabel = view.findViewById(R.id.tv_helped_label);
        TextView tvVolunteers = view.findViewById(R.id.tv_volunteers);

        RecyclerView rvProjects = view.findViewById(R.id.rv_projects);
        ProjectAdapter projectAdapter = new ProjectAdapter(new ProjectAdapter.OnProjectAction() {
            @Override
            public void onDonate(int projectId) {
                // Simplified: no donate from fund detail
            }
            @Override
            public void onDetails(int projectId) {
                dismiss();
                ProjectDetailDialogFragment dialog = ProjectDetailDialogFragment.newInstance(projectId);
                dialog.show(getParentFragmentManager(), "project_detail");
            }
        });
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProjects.setAdapter(projectAdapter);
        rvProjects.setNestedScrollingEnabled(false);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            FundEntity fund = db.fundDao().getById(fundId);
            List<ProjectEntity> projects = db.projectDao().getByFundSync(fundId);

            if (fund != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvName.setText(fund.name);
                    tvCityYear.setText(fund.city + " · с " + fund.foundedYear + " г.");
                    tvDescription.setText(fund.description);
                    tvPeopleHelped.setText(fmt.format(fund.peopleHelped));
                    tvHelpedLabel.setText(fund.peopleHelpedLabel);
                    tvVolunteers.setText(String.valueOf(fund.volunteersCount));
                    tvVerified.setVisibility(fund.verified ? View.VISIBLE : View.GONE);

                    // Cover image
                    if (fund.coverPath != null && !fund.coverPath.isEmpty()) {
                        ivCover.setVisibility(View.VISIBLE);
                        Object source = fund.coverPath.startsWith("http") || fund.coverPath.startsWith("content://")
                                ? fund.coverPath : new java.io.File(fund.coverPath);
                        Glide.with(requireContext())
                                .load(source)
                                .centerCrop()
                                .into(ivCover);
                    }

                    projectAdapter.setItems(projects);
                });
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

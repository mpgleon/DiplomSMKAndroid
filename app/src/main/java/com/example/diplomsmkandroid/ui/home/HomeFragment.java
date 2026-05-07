package com.example.diplomsmkandroid.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.ProjectAdapter;
import com.example.diplomsmkandroid.ui.adapters.TestimonialAdapter;
import com.example.diplomsmkandroid.ui.dialogs.DonateDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.ProjectDetailDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment implements ProjectAdapter.OnProjectAction {

    private ProjectAdapter projectAdapter;
    private TestimonialAdapter testimonialAdapter;
    private TextView statDonations, statUsers, statProjects;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statDonations = view.findViewById(R.id.stat_donations);
        statUsers = view.findViewById(R.id.stat_users);
        statProjects = view.findViewById(R.id.stat_projects);

        // Projects RecyclerView
        RecyclerView rvProjects = view.findViewById(R.id.rv_projects);
        projectAdapter = new ProjectAdapter(this);
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProjects.setAdapter(projectAdapter);
        rvProjects.setNestedScrollingEnabled(false);

        // Testimonials RecyclerView (horizontal)
        RecyclerView rvTestimonials = view.findViewById(R.id.rv_testimonials);
        testimonialAdapter = new TestimonialAdapter();
        rvTestimonials.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTestimonials.setAdapter(testimonialAdapter);
        rvTestimonials.setNestedScrollingEnabled(false);

        // Buttons
        MaterialButton btnHelpNow = view.findViewById(R.id.btn_help_now);
        btnHelpNow.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.getNavController().navigate(R.id.nav_projects);
            }
        });

        MaterialButton btnCreateProject = view.findViewById(R.id.btn_create_project);
        btnCreateProject.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null) return;
            UserEntity user = activity.getCurrentUser();
            if (user == null) {
                activity.showLoginDialog();
                return;
            }
            // Open create project dialog (fund rep → their fund, others → own initiative)
            Integer fundId = "fund".equals(user.role) ? user.fundId : null;
            com.example.diplomsmkandroid.ui.dialogs.CreateProjectDialogFragment dialog =
                    com.example.diplomsmkandroid.ui.dialogs.CreateProjectDialogFragment.newInstance(user.id, fundId);
            dialog.show(getParentFragmentManager(), "create_project");
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            var projects = db.projectDao().getActiveSync();
            var testimonials = db.testimonialDao().getAllSync();
            double totalDonations = db.donationDao().getTotalDonations();
            int userCount = db.userDao().getCount();
            int projectCount = db.projectDao().getActiveCount();

            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    projectAdapter.setItems(projects.size() > 6 ? projects.subList(0, 6) : projects);
                    testimonialAdapter.setItems(testimonials);
                    statDonations.setText("₽" + fmt.format((long) totalDonations));
                    statUsers.setText(String.valueOf(userCount));
                    statProjects.setText(String.valueOf(projectCount));
                });
            }
        }).start();
    }

    @Override
    public void onDonate(int projectId) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        UserEntity user = activity.getCurrentUser();
        if (user == null) {
            Snackbar.make(requireView(), R.string.need_login, Snackbar.LENGTH_SHORT).show();
            activity.showLoginDialog();
            return;
        }
        DonateDialogFragment dialog = DonateDialogFragment.newInstance(projectId, user.id);
        dialog.setOnDonateListener(() -> loadData());
        dialog.show(getParentFragmentManager(), "donate");
    }

    @Override
    public void onDetails(int projectId) {
        ProjectDetailDialogFragment dialog = ProjectDetailDialogFragment.newInstance(projectId);
        dialog.show(getParentFragmentManager(), "project_detail");
    }
}

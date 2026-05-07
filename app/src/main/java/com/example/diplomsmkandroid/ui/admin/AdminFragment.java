package com.example.diplomsmkandroid.ui.admin;

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
import com.example.diplomsmkandroid.data.entity.FundEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.AdminFundAdapter;
import com.example.diplomsmkandroid.ui.adapters.AdminProjectAdapter;
import com.example.diplomsmkandroid.ui.adapters.AdminUserAdapter;
import com.example.diplomsmkandroid.ui.adapters.AdminVolunteerTaskAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class AdminFragment extends Fragment {

    private AdminUserAdapter userAdapter;
    private AdminProjectAdapter projectAdapter;
    private AdminFundAdapter fundAdapter;
    private AdminVolunteerTaskAdapter volunteerTaskAdapter;
    private TextView statUsers, statProjects, statDonations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvDenied = view.findViewById(R.id.tv_access_denied);
        LinearLayout layoutContent = view.findViewById(R.id.layout_admin_content);

        MainActivity activity = (MainActivity) getActivity();
        UserEntity currentUser = activity != null ? activity.getCurrentUser() : null;

        if (currentUser == null || !"admin".equals(currentUser.role)) {
            tvDenied.setVisibility(View.VISIBLE);
            layoutContent.setVisibility(View.GONE);
            return;
        }

        tvDenied.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);

        statUsers = view.findViewById(R.id.stat_users);
        statProjects = view.findViewById(R.id.stat_projects);
        statDonations = view.findViewById(R.id.stat_donations);

        // Users
        RecyclerView rvUsers = view.findViewById(R.id.rv_users);
        userAdapter = new AdminUserAdapter((user, newRole) -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            new Thread(() -> {
                user.role = newRole;
                db.userDao().update(user);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(), "Роль изменена", Snackbar.LENGTH_SHORT).show();
                        loadData();
                    });
                }
            }).start();
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(userAdapter);

        // Projects
        RecyclerView rvProjects = view.findViewById(R.id.rv_projects);
        projectAdapter = new AdminProjectAdapter(new AdminProjectAdapter.OnProjectAction() {
            @Override
            public void onActivate(ProjectEntity project) {
                updateProjectStatus(project, "active");
            }
            @Override
            public void onArchive(ProjectEntity project) {
                updateProjectStatus(project, "archived");
            }
        });
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProjects.setAdapter(projectAdapter);

        // Volunteer tasks
        RecyclerView rvVolunteerTasks = view.findViewById(R.id.rv_volunteer_tasks);
        volunteerTaskAdapter = new AdminVolunteerTaskAdapter(new AdminVolunteerTaskAdapter.OnTaskAction() {
            @Override
            public void onApprove(VolunteerTaskEntity task) {
                updateVolunteerTaskStatus(task, "active");
            }
            @Override
            public void onReject(VolunteerTaskEntity task) {
                updateVolunteerTaskStatus(task, "rejected");
            }
        });
        rvVolunteerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVolunteerTasks.setAdapter(volunteerTaskAdapter);

        // Funds
        RecyclerView rvFunds = view.findViewById(R.id.rv_funds);
        fundAdapter = new AdminFundAdapter(fund -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            new Thread(() -> {
                fund.verified = !fund.verified;
                db.fundDao().update(fund);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(),
                                fund.verified ? "Фонд верифицирован" : "Верификация снята",
                                Snackbar.LENGTH_SHORT).show();
                        loadData();
                    });
                }
            }).start();
        });
        rvFunds.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFunds.setAdapter(fundAdapter);

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userAdapter != null) loadData();
    }

    private void loadData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            var users = db.userDao().getAllSync();
            var projects = db.projectDao().getAllSync();
            var funds = db.fundDao().getAllSync();
            var volunteerTasks = db.volunteerTaskDao().getAllSync();
            int userCount = db.userDao().getCount();
            int projectCount = db.projectDao().getCount();
            double totalDonations = db.donationDao().getTotalDonations();

            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("ru"));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    userAdapter.setItems(users);
                    projectAdapter.setItems(projects);
                    volunteerTaskAdapter.setItems(volunteerTasks);
                    fundAdapter.setItems(funds);
                    statUsers.setText(String.valueOf(userCount));
                    statProjects.setText(String.valueOf(projectCount));
                    statDonations.setText("₽" + fmt.format((long) totalDonations));
                });
            }
        }).start();
    }

    private void updateProjectStatus(ProjectEntity project, String status) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            project.status = status;
            db.projectDao().update(project);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Snackbar.make(requireView(), "Статус проекта изменён", Snackbar.LENGTH_SHORT).show();
                    loadData();
                });
            }
        }).start();
    }

    private void updateVolunteerTaskStatus(VolunteerTaskEntity task, String status) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            db.volunteerTaskDao().updateStatus(task.id, status);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Snackbar.make(requireView(), "Статус задачи изменён", Snackbar.LENGTH_SHORT).show();
                    loadData();
                });
            }
        }).start();
    }
}

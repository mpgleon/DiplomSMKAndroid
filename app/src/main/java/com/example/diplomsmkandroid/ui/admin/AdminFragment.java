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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;

public class AdminFragment extends Fragment {

    private static final int PAGE_SIZE = 20;

    private AdminUserAdapter userAdapter;
    private AdminProjectAdapter projectAdapter;
    private AdminFundAdapter fundAdapter;
    private AdminVolunteerTaskAdapter volunteerTaskAdapter;
    private TextView statUsers, statProjects, statDonations;
    private ActivityResultLauncher<String> importDbLauncher;
    private ActivityResultLauncher<String> exportDbLauncher;

    private int usersPage = 0, usersTotalPages = 1;
    private int projectsPage = 0, projectsTotalPages = 1;
    private int tasksPage = 0, tasksTotalPages = 1;
    private int fundsPage = 0, fundsTotalPages = 1;

    private TextView tvUsersPage, tvProjectsPage, tvTasksPage, tvFundsPage;
    private MaterialButton btnUsersPrev, btnUsersNext;
    private MaterialButton btnProjectsPrev, btnProjectsNext;
    private MaterialButton btnTasksPrev, btnTasksNext;
    private MaterialButton btnFundsPrev, btnFundsNext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importDbLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importDatabase(uri);
                    }
                }
        );
        exportDbLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                uri -> {
                    if (uri != null) {
                        exportDatabase(uri);
                    }
                }
        );
    }

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

        MaterialButton btnExportDb = view.findViewById(R.id.btn_export_db);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        btnExportDb.setOnClickListener(v -> exportDbLauncher.launch("dobrovmeste_" + timeStamp + ".db"));

        MaterialButton btnImportDb = view.findViewById(R.id.btn_import_db);
        btnImportDb.setOnClickListener(v -> importDbLauncher.launch("*/*"));

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

        // Pagination controls
        tvUsersPage = view.findViewById(R.id.tv_users_page);
        tvProjectsPage = view.findViewById(R.id.tv_projects_page);
        tvTasksPage = view.findViewById(R.id.tv_tasks_page);
        tvFundsPage = view.findViewById(R.id.tv_funds_page);

        btnUsersPrev = view.findViewById(R.id.btn_users_prev);
        btnUsersNext = view.findViewById(R.id.btn_users_next);
        btnProjectsPrev = view.findViewById(R.id.btn_projects_prev);
        btnProjectsNext = view.findViewById(R.id.btn_projects_next);
        btnTasksPrev = view.findViewById(R.id.btn_tasks_prev);
        btnTasksNext = view.findViewById(R.id.btn_tasks_next);
        btnFundsPrev = view.findViewById(R.id.btn_funds_prev);
        btnFundsNext = view.findViewById(R.id.btn_funds_next);

        btnUsersPrev.setOnClickListener(v -> { if (usersPage > 0) { usersPage--; loadData(); } });
        btnUsersNext.setOnClickListener(v -> { if (usersPage < usersTotalPages - 1) { usersPage++; loadData(); } });
        btnProjectsPrev.setOnClickListener(v -> { if (projectsPage > 0) { projectsPage--; loadData(); } });
        btnProjectsNext.setOnClickListener(v -> { if (projectsPage < projectsTotalPages - 1) { projectsPage++; loadData(); } });
        btnTasksPrev.setOnClickListener(v -> { if (tasksPage > 0) { tasksPage--; loadData(); } });
        btnTasksNext.setOnClickListener(v -> { if (tasksPage < tasksTotalPages - 1) { tasksPage++; loadData(); } });
        btnFundsPrev.setOnClickListener(v -> { if (fundsPage > 0) { fundsPage--; loadData(); } });
        btnFundsNext.setOnClickListener(v -> { if (fundsPage < fundsTotalPages - 1) { fundsPage++; loadData(); } });

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
            int userCount = db.userDao().getCount();
            int projectCount = db.projectDao().getCount();
            int fundCount = db.fundDao().getCount();
            int taskCount = db.volunteerTaskDao().getCount();
            double totalDonations = db.donationDao().getTotalDonations();

            usersTotalPages = Math.max(1, (int) Math.ceil((double) userCount / PAGE_SIZE));
            projectsTotalPages = Math.max(1, (int) Math.ceil((double) projectCount / PAGE_SIZE));
            tasksTotalPages = Math.max(1, (int) Math.ceil((double) taskCount / PAGE_SIZE));
            fundsTotalPages = Math.max(1, (int) Math.ceil((double) fundCount / PAGE_SIZE));

            if (usersPage >= usersTotalPages) usersPage = usersTotalPages - 1;
            if (projectsPage >= projectsTotalPages) projectsPage = projectsTotalPages - 1;
            if (tasksPage >= tasksTotalPages) tasksPage = tasksTotalPages - 1;
            if (fundsPage >= fundsTotalPages) fundsPage = fundsTotalPages - 1;

            var users = db.userDao().getPageSync(PAGE_SIZE, usersPage * PAGE_SIZE);
            var projects = db.projectDao().getPageSync(PAGE_SIZE, projectsPage * PAGE_SIZE);
            var funds = db.fundDao().getPageSync(PAGE_SIZE, fundsPage * PAGE_SIZE);
            var volunteerTasks = db.volunteerTaskDao().getPageSync(PAGE_SIZE, tasksPage * PAGE_SIZE);

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

                    updatePaginationUi();
                });
            }
        }).start();
    }

    private void updatePaginationUi() {
        tvUsersPage.setText((usersPage + 1) + " / " + usersTotalPages);
        tvProjectsPage.setText((projectsPage + 1) + " / " + projectsTotalPages);
        tvTasksPage.setText((tasksPage + 1) + " / " + tasksTotalPages);
        tvFundsPage.setText((fundsPage + 1) + " / " + fundsTotalPages);

        btnUsersPrev.setEnabled(usersPage > 0);
        btnUsersNext.setEnabled(usersPage < usersTotalPages - 1);
        btnProjectsPrev.setEnabled(projectsPage > 0);
        btnProjectsNext.setEnabled(projectsPage < projectsTotalPages - 1);
        btnTasksPrev.setEnabled(tasksPage > 0);
        btnTasksNext.setEnabled(tasksPage < tasksTotalPages - 1);
        btnFundsPrev.setEnabled(fundsPage > 0);
        btnFundsNext.setEnabled(fundsPage < fundsTotalPages - 1);
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

    private void exportDatabase(Uri destUri) {
        new Thread(() -> {
            try {
                Context ctx = requireContext().getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(ctx);
                db.checkpoint();

                File dbFile = ctx.getDatabasePath("dobrovmeste.db");
                try (InputStream is = new java.io.FileInputStream(dbFile);
                     java.io.OutputStream os = ctx.getContentResolver().openOutputStream(destUri)) {
                    if (os == null) throw new IOException("Не удалось создать файл");
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(), "База данных экспортирована", Snackbar.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(), "Ошибка экспорта: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void importDatabase(Uri sourceUri) {
        new Thread(() -> {
            try {
                Context ctx = requireContext().getApplicationContext();

                // 1. Close current DB and clear singleton
                AppDatabase.closeAndReset();

                // 2. Delete WAL/SHM leftovers so imported file isn't corrupted
                File dbFile = ctx.getDatabasePath("dobrovmeste.db");
                File parent = dbFile.getParentFile();
                if (parent != null) {
                    new File(parent, "dobrovmeste.db-wal").delete();
                    new File(parent, "dobrovmeste.db-shm").delete();
                }

                // 3. Copy selected file into app database path
                try (InputStream is = ctx.getContentResolver().openInputStream(sourceUri);
                     FileOutputStream fos = new FileOutputStream(dbFile)) {
                    if (is == null) throw new IOException("Не удалось открыть файл");
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                }

                // 4. Reopen database
                AppDatabase.getInstance(ctx);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(), "База данных импортирована", Snackbar.LENGTH_SHORT).show();
                        loadData();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Snackbar.make(requireView(), "Ошибка импорта: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }
}

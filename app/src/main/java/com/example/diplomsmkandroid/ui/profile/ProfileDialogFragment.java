package com.example.diplomsmkandroid.ui.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.DonationEntity;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerApplicationEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.DonationAdapter;
import com.example.diplomsmkandroid.ui.adapters.FundTaskAdapter;
import com.example.diplomsmkandroid.ui.adapters.MyApplicationAdapter;
import com.example.diplomsmkandroid.ui.adapters.ProjectAdapter;
import com.example.diplomsmkandroid.ui.adapters.VolunteerApplicationAdapter;
import com.example.diplomsmkandroid.ui.dialogs.CreateProjectDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.CreateVolunteerTaskDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.EditProjectDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.EditVolunteerTaskDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.ProjectDetailDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProfileDialogFragment extends DialogFragment {

    private ShapeableImageView ivAvatar;
    private ActivityResultLauncher<Intent> avatarPickerLauncher;

    @Override
    public int getTheme() {
        return R.style.Theme_DiplomSMKAndroid_FullScreenDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String path = copyUriToInternal(uri, "avatar");
                            if (path != null && ivAvatar != null) {
                                ivAvatar.setImageURI(Uri.fromFile(new File(path)));
                                // Save to user
                                MainActivity act = (MainActivity) getActivity();
                                if (act != null && act.getCurrentUser() != null) {
                                    UserEntity u = act.getCurrentUser();
                                    u.avatarPath = path;
                                    AppDatabase db = AppDatabase.getInstance(requireContext());
                                    new Thread(() -> db.userDao().update(u)).start();
                                }
                            }
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) { dismiss(); return; }

        MaterialButton btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dismiss());

        LinearLayout layoutNotLogged = view.findViewById(R.id.layout_not_logged);
        LinearLayout layoutLogged = view.findViewById(R.id.layout_logged);

        UserEntity user = activity.getCurrentUser();
        if (user == null) {
            layoutNotLogged.setVisibility(View.VISIBLE);
            layoutLogged.setVisibility(View.GONE);

            view.findViewById(R.id.btn_login).setOnClickListener(v -> {
                dismiss();
                activity.showLoginDialog();
            });
            view.findViewById(R.id.btn_register).setOnClickListener(v -> {
                dismiss();
                activity.showRegisterDialog();
            });
            return;
        }

        layoutNotLogged.setVisibility(View.GONE);
        layoutLogged.setVisibility(View.VISIBLE);

        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvEmail = view.findViewById(R.id.tv_email);
        TextView tvRole = view.findViewById(R.id.tv_role);

        tvName.setText(user.fullName);
        tvEmail.setText(user.email);
        switch (user.role) {
            case "volunteer": tvRole.setText("Волонтёр"); break;
            case "fund": tvRole.setText("Представитель фонда"); break;
            case "admin": tvRole.setText("Администратор"); break;
            default: tvRole.setText("Донор"); break;
        }

        // Avatar
        ivAvatar = view.findViewById(R.id.iv_avatar);
        if (user.avatarPath != null) {
            File avatarFile = new File(user.avatarPath);
            if (avatarFile.exists()) {
                ivAvatar.setImageURI(Uri.fromFile(avatarFile));
            }
        }
        view.findViewById(R.id.btn_change_avatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            avatarPickerLauncher.launch(intent);
        });

        // Donations
        RecyclerView rvDonations = view.findViewById(R.id.rv_donations);
        DonationAdapter donationAdapter = new DonationAdapter();
        rvDonations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDonations.setAdapter(donationAdapter);

        // My projects (with edit support)
        RecyclerView rvMyProjects = view.findViewById(R.id.rv_my_projects);
        ProjectAdapter myProjectAdapter = new ProjectAdapter(new ProjectAdapter.OnProjectAction() {
            @Override public void onDonate(int projectId) {
                // Edit project instead of donate in profile context

                EditProjectDialogFragment d = EditProjectDialogFragment.newInstance(projectId);
                d.show(getParentFragmentManager(), "edit_project");

            }
            @Override public void onDetails(int projectId) {
                ProjectDetailDialogFragment d = ProjectDetailDialogFragment.newInstance(projectId);
                d.show(getParentFragmentManager(), "project_detail");
            }
        });
        rvMyProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyProjects.setAdapter(myProjectAdapter);

        // My volunteer applications (task 5)
        LinearLayout layoutMyApps = view.findViewById(R.id.layout_my_applications);
        RecyclerView rvMyApps = view.findViewById(R.id.rv_my_applications);
        MyApplicationAdapter myAppAdapter = new MyApplicationAdapter();
        rvMyApps.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyApps.setAdapter(myAppAdapter);
        layoutMyApps.setVisibility(View.VISIBLE);

        // Fund tasks list (task 9)
        LinearLayout layoutFundTasks = view.findViewById(R.id.layout_fund_tasks);
        RecyclerView rvFundTasks = view.findViewById(R.id.rv_fund_tasks);

        // Fund section (only for fund reps)
        LinearLayout layoutFundSection = view.findViewById(R.id.layout_fund_section);
        RecyclerView rvApplications = view.findViewById(R.id.rv_fund_applications);

        if ("fund".equals(user.role) && user.fundId != null) {
            layoutFundSection.setVisibility(View.VISIBLE);

            VolunteerApplicationAdapter appAdapter = new VolunteerApplicationAdapter(
                    new VolunteerApplicationAdapter.OnApplicationAction() {
                        @Override
                        public void onAccept(int applicationId) {
                            updateApplicationStatus(applicationId, "accepted", rvApplications, user.fundId);
                        }
                        @Override
                        public void onReject(int applicationId) {
                            updateApplicationStatus(applicationId, "rejected", rvApplications, user.fundId);
                        }
                    });
            rvApplications.setLayoutManager(new LinearLayoutManager(getContext()));
            rvApplications.setAdapter(appAdapter);

            // Create project button for fund
            MaterialButton btnCreateProject = view.findViewById(R.id.btn_create_project);
            btnCreateProject.setVisibility(View.VISIBLE);
            btnCreateProject.setOnClickListener(v -> {
                CreateProjectDialogFragment d = CreateProjectDialogFragment.newInstance(user.id, user.fundId);
                d.show(getParentFragmentManager(), "create_project");
            });

            // Create volunteer task button
            MaterialButton btnCreateTask = view.findViewById(R.id.btn_create_task);
            btnCreateTask.setVisibility(View.VISIBLE);
            btnCreateTask.setOnClickListener(v -> {
                CreateVolunteerTaskDialogFragment d = CreateVolunteerTaskDialogFragment.newInstance(user.fundId);
                d.show(getParentFragmentManager(), "create_task");
            });

            // Fund tasks (task 9)
            layoutFundTasks.setVisibility(View.VISIBLE);
            final FundTaskAdapter[] ftHolder = new FundTaskAdapter[1];
            ftHolder[0] = new FundTaskAdapter(new FundTaskAdapter.OnTaskAction() {
                @Override
                public void onEdit(VolunteerTaskEntity task) {
                    EditVolunteerTaskDialogFragment d = EditVolunteerTaskDialogFragment.newInstance(task.id);
                    d.show(getParentFragmentManager(), "edit_task");
                }
                @Override
                public void onDelete(VolunteerTaskEntity task) {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    new Thread(() -> {
                        int appCount = db.volunteerApplicationDao().countForTask(task.id);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (appCount > 0) {
                                    Snackbar.make(requireView(), "Нельзя удалить — есть заявки", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    new Thread(() -> {
                                        db.volunteerTaskDao().delete(task);
                                        List<VolunteerTaskEntity> tasks = db.volunteerTaskDao().getByFundSync(user.fundId);
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> ftHolder[0].setItems(tasks));
                                        }
                                    }).start();
                                    Snackbar.make(requireView(), "Задача удалена", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
            });
            rvFundTasks.setLayoutManager(new LinearLayoutManager(getContext()));
            rvFundTasks.setAdapter(ftHolder[0]);
            loadFundTasks(ftHolder[0], user.fundId);

            // Load applications
            loadFundApplications(appAdapter, user.fundId);
        } else {
            layoutFundSection.setVisibility(View.GONE);

            // Regular user can still create "own initiative" projects
            MaterialButton btnCreateProject = view.findViewById(R.id.btn_create_project);
            btnCreateProject.setVisibility(View.VISIBLE);
            btnCreateProject.setText("Своя инициатива");
            btnCreateProject.setOnClickListener(v -> {
                CreateProjectDialogFragment d = CreateProjectDialogFragment.newInstance(user.id, null);
                d.show(getParentFragmentManager(), "create_project");
            });
        }

        // Admin panel button (only for admins)
        MaterialButton btnAdmin = view.findViewById(R.id.btn_admin);
        if ("admin".equals(user.role)) {
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setOnClickListener(v -> {
                dismiss();
                activity.getNavController().navigate(R.id.nav_admin);
            });
        }

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> activity.logout());

        // Load data
        loadProfileData(donationAdapter, myProjectAdapter, myAppAdapter, user);
    }

    private void loadProfileData(DonationAdapter donationAdapter, ProjectAdapter myProjectAdapter,
                                MyApplicationAdapter myAppAdapter, UserEntity user) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<DonationEntity> donations = db.donationDao().getRecentByUserSync(user.id);
            List<ProjectEntity> myProjects = db.projectDao().getByCreatorSync(user.id);
            List<VolunteerApplicationEntity> myApps = db.volunteerApplicationDao().getByUserSync(user.id);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    donationAdapter.setItems(donations, db);
                    myProjectAdapter.setItems(myProjects);
                    myAppAdapter.setItems(myApps, db);
                });
            }
        }).start();
    }

    private void loadFundTasks(FundTaskAdapter adapter, int fundId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<VolunteerTaskEntity> tasks = db.volunteerTaskDao().getByFundSync(fundId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setItems(tasks));
            }
        }).start();
    }

    private void loadFundApplications(VolunteerApplicationAdapter adapter, int fundId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<VolunteerApplicationEntity> apps = db.volunteerApplicationDao().getByFundSync(fundId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setItems(apps, db));
            }
        }).start();
    }

    private void updateApplicationStatus(int applicationId, String status, RecyclerView rv, int fundId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            db.volunteerApplicationDao().updateStatus(applicationId, status);
            // If accepted, increment slots_taken on the volunteer task
            if ("accepted".equals(status)) {
                VolunteerApplicationEntity app = db.volunteerApplicationDao().getById(applicationId);
                if (app != null) {
                    db.volunteerTaskDao().incrementSlotsTaken(app.taskId);
                }
            }
            List<VolunteerApplicationEntity> apps = db.volunteerApplicationDao().getByFundSync(fundId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    VolunteerApplicationAdapter adapter = (VolunteerApplicationAdapter) rv.getAdapter();
                    if (adapter != null) adapter.setItems(apps, db);
                });
            }
        }).start();
    }

    private String copyUriToInternal(Uri uri, String prefix) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return null;
            File dir = new File(requireContext().getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, prefix + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}

package com.example.diplomsmkandroid.ui.volunteers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerApplicationEntity;
import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.VolunteerTaskAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VolunteersFragment extends Fragment implements VolunteerTaskAdapter.OnTaskAction {

    private static final int PAGE_SIZE = 12;

    private VolunteerTaskAdapter adapter;
    private List<VolunteerTaskEntity> allTasks = new ArrayList<>();
    private List<VolunteerTaskEntity> filteredTasks = new ArrayList<>();
    private String currentSearch = "";
    private int currentPage = 0;

    private MaterialButton btnPrev, btnNext;
    private TextView tvPageInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_tasks);
        adapter = new VolunteerTaskAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
        tvPageInfo = view.findViewById(R.id.tv_page_info);

        btnPrev.setOnClickListener(v -> { currentPage--; applyPage(); });
        btnNext.setOnClickListener(v -> { currentPage++; applyPage(); });

        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                currentPage = 0;
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
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
            allTasks = db.volunteerTaskDao().getActiveSync();
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::applyFilters);
            }
        }).start();
    }

    private void applyFilters() {
        filteredTasks = allTasks.stream()
                .filter(t -> currentSearch.isEmpty() ||
                        t.title.toLowerCase().contains(currentSearch) ||
                        t.description.toLowerCase().contains(currentSearch) ||
                        t.category.toLowerCase().contains(currentSearch) ||
                        t.city.toLowerCase().contains(currentSearch))
                .collect(Collectors.toList());
        applyPage();
    }

    private void applyPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredTasks.size() / PAGE_SIZE));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredTasks.size());
        List<VolunteerTaskEntity> page = filteredTasks.subList(from, to);
        adapter.setItems(page);

        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
        tvPageInfo.setText((currentPage + 1) + " / " + totalPages);
    }

    @Override
    public void onApply(int taskId) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        UserEntity user = activity.getCurrentUser();
        if (user == null) {
            Snackbar.make(requireView(), R.string.need_login, Snackbar.LENGTH_SHORT).show();
            activity.showLoginDialog();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            int count = db.volunteerApplicationDao().countByUserAndTask(user.id, taskId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (count > 0) {
                        Snackbar.make(requireView(), R.string.already_applied, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(() -> {
                        VolunteerApplicationEntity app = new VolunteerApplicationEntity();
                        app.userId = user.id;
                        app.taskId = taskId;
                        app.status = "pending";
                        app.createdAt = System.currentTimeMillis();
                        db.volunteerApplicationDao().insert(app);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Snackbar.make(requireView(), R.string.apply_success, Snackbar.LENGTH_SHORT).show());
                        }
                    }).start();
                });
            }
        }).start();
    }
}

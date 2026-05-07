package com.example.diplomsmkandroid.ui.projects;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.entity.ProjectEntity;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.MainActivity;
import com.example.diplomsmkandroid.ui.adapters.ProjectAdapter;
import com.example.diplomsmkandroid.ui.dialogs.DonateDialogFragment;
import com.example.diplomsmkandroid.ui.dialogs.ProjectDetailDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectsFragment extends Fragment implements ProjectAdapter.OnProjectAction {

    private static final int PAGE_SIZE = 12;

    private ProjectAdapter adapter;
    private TextView tvCount, tvPageInfo;
    private MaterialButton btnPrev, btnNext;
    private List<ProjectEntity> allProjects = new ArrayList<>();
    private List<ProjectEntity> filteredProjects = new ArrayList<>();
    private String currentSearch = "";
    private String currentCategory = "";
    private int currentSort = 0;
    private int currentPage = 0;

    private final String[] categories = {"Все категории", "Дети", "Медицина", "Экология", "Животные", "Образование", "Пожилые", "Культура"};
    private final String[] sortOptions = {"По дате", "По популярности", "По сумме", "По срочности"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCount = view.findViewById(R.id.tv_count);

        // Search
        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Category spinner
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories));
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                currentCategory = pos == 0 ? "" : categories[pos];
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sort spinner
        Spinner spinnerSort = view.findViewById(R.id.spinner_sort);
        spinnerSort.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions));
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                currentSort = pos;
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.rv_projects);
        adapter = new ProjectAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Pagination
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
        tvPageInfo = view.findViewById(R.id.tv_page_info);
        btnPrev.setOnClickListener(v -> { currentPage--; applyPage(); });
        btnNext.setOnClickListener(v -> { currentPage++; applyPage(); });

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
            allProjects = db.projectDao().getActiveSync();
            Map<Integer, double[]> ratingsMap = new HashMap<>();
            for (ProjectEntity p : allProjects) {
                double avg = db.reviewDao().getAvgRating(p.id);
                int count = db.reviewDao().getCountForProject(p.id);
                if (count > 0) {
                    ratingsMap.put(p.id, new double[]{avg, count});
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setRatings(ratingsMap);
                    applyFilters();
                });
            }
        }).start();
    }

    private void applyFilters() {
        filteredProjects = allProjects.stream()
                .filter(p -> currentCategory.isEmpty() || p.category.equals(currentCategory))
                .filter(p -> currentSearch.isEmpty() ||
                        p.title.toLowerCase().contains(currentSearch) ||
                        p.description.toLowerCase().contains(currentSearch))
                .collect(Collectors.toList());

        switch (currentSort) {
            case 1: filteredProjects.sort(Comparator.comparingDouble((ProjectEntity p) -> p.collected).reversed()); break;
            case 2: filteredProjects.sort(Comparator.comparingDouble((ProjectEntity p) -> p.goal).reversed()); break;
            case 3: filteredProjects.sort(Comparator.comparing((ProjectEntity p) -> !p.urgent).thenComparingInt(p -> p.daysLeft)); break;
            default: filteredProjects.sort(Comparator.comparingLong((ProjectEntity p) -> p.createdAt).reversed()); break;
        }

        tvCount.setText("Найдено проектов: " + filteredProjects.size());
        currentPage = 0;
        applyPage();
    }

    private void applyPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredProjects.size() / PAGE_SIZE));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredProjects.size());
        adapter.setItems(filteredProjects.subList(from, to));

        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
        tvPageInfo.setText((currentPage + 1) + " / " + totalPages);
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
        dialog.setOnDonateListener(this::loadData);
        dialog.show(getParentFragmentManager(), "donate");
    }

    @Override
    public void onDetails(int projectId) {
        ProjectDetailDialogFragment dialog = ProjectDetailDialogFragment.newInstance(projectId);
        dialog.show(getParentFragmentManager(), "project_detail");
    }
}

package com.example.diplomsmkandroid.ui.funds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.ui.adapters.FundAdapter;
import com.example.diplomsmkandroid.ui.dialogs.FundDetailDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class FundsFragment extends Fragment implements FundAdapter.OnFundAction {

    private FundAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_funds, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_funds);
        adapter = new FundAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        MaterialButton btnPartner = view.findViewById(R.id.btn_become_partner);
        btnPartner.setOnClickListener(v ->
                Snackbar.make(requireView(), R.string.partner_request, Snackbar.LENGTH_LONG).show());

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
            var funds = db.fundDao().getAllSync();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setItems(funds));
            }
        }).start();
    }

    @Override
    public void onDetails(int fundId) {
        FundDetailDialogFragment dialog = FundDetailDialogFragment.newInstance(fundId);
        dialog.show(getParentFragmentManager(), "fund_detail");
    }
}

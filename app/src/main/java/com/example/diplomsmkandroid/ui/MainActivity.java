package com.example.diplomsmkandroid.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.diplomsmkandroid.R;
import com.example.diplomsmkandroid.data.AppDatabase;
import com.example.diplomsmkandroid.data.DatabaseSeeder;
import com.example.diplomsmkandroid.data.entity.UserEntity;
import com.example.diplomsmkandroid.ui.profile.ProfileDialogFragment;
import com.example.diplomsmkandroid.util.SecurityUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private UserEntity currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_nav);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Ensure admin exists and seed database in background
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            DatabaseSeeder.ensureAdminExists(db);
            DatabaseSeeder.seedIfEmpty(db);
        }).start();

        // Update toolbar title based on destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getLabel() != null) {
                toolbar.setTitle(destination.getLabel());
            }
        });

        // Left navigation icon → profile / login (always visible, not covered)
        toolbar.setNavigationOnClickListener(v -> {
            if (currentUser != null) {
                showProfileDialog();
            } else {
                showLoginDialog();
            }
        });

    }

    public void showProfileDialog() {
        ProfileDialogFragment dialog = new ProfileDialogFragment();
        dialog.show(getSupportFragmentManager(), "profile");
    }

    public void setCurrentUser(UserEntity user) {
        this.currentUser = user;
        updateToolbarIcon();
    }

    private void updateToolbarIcon() {
        if (currentUser != null) {
            toolbar.setNavigationContentDescription(currentUser.fullName);
        } else {
            toolbar.setNavigationContentDescription(getString(R.string.login));
        }
    }

    public UserEntity getCurrentUser() {
        return currentUser;
    }

    public void showLoginDialog() {
        com.example.diplomsmkandroid.ui.auth.LoginDialogFragment dialog =
                new com.example.diplomsmkandroid.ui.auth.LoginDialogFragment();
        dialog.show(getSupportFragmentManager(), "login");
    }

    public void showRegisterDialog() {
        com.example.diplomsmkandroid.ui.auth.RegisterDialogFragment dialog =
                new com.example.diplomsmkandroid.ui.auth.RegisterDialogFragment();
        dialog.show(getSupportFragmentManager(), "register");
    }

    public void logout() {
        setCurrentUser(null);
        // Dismiss profile dialog if open
        ProfileDialogFragment pf = (ProfileDialogFragment)
                getSupportFragmentManager().findFragmentByTag("profile");
        if (pf != null) pf.dismiss();

        Snackbar.make(findViewById(R.id.container),
                R.string.logout_success, Snackbar.LENGTH_SHORT).show();
    }

    public NavController getNavController() {
        return navController;
    }
}

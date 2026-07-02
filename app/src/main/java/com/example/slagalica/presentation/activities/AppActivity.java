package com.example.slagalica.presentation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ActivityAppBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.social.NotificationItem;
import com.example.slagalica.domain.service.social.NotificationsService;
import com.example.slagalica.presentation.fragments.auth.LoginFragment;
import com.example.slagalica.presentation.fragments.auth.ResetPasswordFragment;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.fragments.common.HomeFragment;
import com.example.slagalica.presentation.fragments.match.AsocijacijeFragment;
import com.example.slagalica.presentation.fragments.match.KoZnaZnaFragment;
import com.example.slagalica.presentation.fragments.match.KorakPoKorakFragment;
import com.example.slagalica.presentation.fragments.match.MojBrojFragment;
import com.example.slagalica.presentation.fragments.match.SkockoFragment;
import com.example.slagalica.presentation.fragments.match.SpojniceFragment;
import com.example.slagalica.presentation.fragments.profile.ProfileFragment;
import com.example.slagalica.presentation.fragments.social.NotificationTargetPlaceholderFragment;
import com.example.slagalica.presentation.fragments.social.NotificationsFragment;
import com.example.slagalica.presentation.notifications.AppNotificationHelper;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppActivity extends AppCompatActivity {
    ActivityAppBinding binding;
    MatchViewModel matchViewModel;
    private int lastNavigatedGameId = -1;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom);
            return insets;
        });
        // Fix drawer padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.leftDrawer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom);
            return insets;
        });

        // Setup VM
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        observeViewModel();

        if (savedInstanceState == null) {
            FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);

            // Load profile fragment into right drawer
            FragmentTransition.to(new ProfileFragment(), this, false, R.id.rightDrawer);
        }

        // Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationOnClickListener(v -> {
            binding.main.openDrawer(GravityCompat.START);
        });

        // Profile button - opens profile drawer
        binding.profileButton.setOnClickListener(v -> {
            binding.main.openDrawer(GravityCompat.END);
        });

        // Drawer links
        View leftDrawer = binding.leftDrawer.getHeaderView(0);

        // Leave match button
        leftDrawer.findViewById(R.id.leave_match).setOnClickListener(v -> {
            showLeaveGameConfirmationDialog(() -> {
                matchViewModel.setGameActive(false);
                FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);
                binding.main.closeDrawer(GravityCompat.START);
            });
        });

        // Home button, also shows game leaving dialogue
        leftDrawer.findViewById(R.id.home).setOnClickListener(v -> {
            if (matchViewModel.getIsGameActive().getValue() != null && matchViewModel.getIsGameActive().getValue()) {
                showLeaveGameConfirmationDialog(() -> {
                    FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);
                    binding.main.closeDrawer(GravityCompat.START);
                });
            } else {
                FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);
                binding.main.closeDrawer(GravityCompat.START);
            }
        });

        // Notifications button
        leftDrawer.findViewById(R.id.notifications).setOnClickListener(v -> {
            FragmentTransition.to(new NotificationsFragment(), this, true, R.id.appContainer);
            binding.main.closeDrawer(GravityCompat.START);
        });
        handleNotificationIntent(getIntent());
        requestNotificationPermission();

        // Reset password
        leftDrawer.findViewById(R.id.reset_password).setOnClickListener(v -> {
            FragmentTransition.to(new ResetPasswordFragment(), this, true, R.id.appContainer);
            binding.main.closeDrawer(GravityCompat.START);
        });

        // Log out
        leftDrawer.findViewById(R.id.logout).setOnClickListener(v -> {
            sessionManager.clear();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            binding.main.closeDrawer(GravityCompat.START);
        });
    }

    private void showLeaveGameConfirmationDialog(Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Napusti igru")
                .setMessage("Da li ste sigurni da zelite da napustite aktivnu igru?")
                .setPositiveButton("Da", (dialog, which) -> {
                    onConfirm.run();
                })
                .setNegativeButton("Ne", null)
                .show();
    }

    // Helper method to make the view (activity) observe changes in VM
    private void observeViewModel(){
        matchViewModel.getCurrentGameId().observe(this, gameId -> {
            if (gameId == null) return;
            if (matchViewModel.getMatch() == null) return;
            if (gameId == lastNavigatedGameId) return;
            Fragment fragment = null;
            switch (gameId) {
                case 0:
                    Log.d("Match", "Match ended");
                    matchViewModel.setGameActive(false);
                    lastNavigatedGameId = 0;
                    FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);
                    break;
                case 1:
                    Log.d("Match", "Switching to ko zna zna fragment...");
                    fragment = new KoZnaZnaFragment();
                    break;
                case 2:
                    Log.d("Match", "Switching to spojnice fragment...");
                    fragment = new SpojniceFragment();
                    break;
                case 3:
                    Log.d("Match", "Switching to asocijacije fragment...");
                    fragment = new AsocijacijeFragment();
                    break;
                case 4:
                    Log.d("Match", "Switching to skocko fragment...");
                    fragment = new SkockoFragment();
                    break;
                case 5:
                    Log.d("Match", "Switching to korak po korak fragment...");
                    fragment = new KorakPoKorakFragment();
                    break;
                case 6:
                    Log.d("Match", "Switching to moj broj fragment...");
                    fragment = new MojBrojFragment();
                    break;
            }
            if (fragment != null) {
                lastNavigatedGameId = gameId;
                FragmentTransition.to(fragment, this, false, R.id.appContainer);
            }
        });

        matchViewModel.getIsGameActive().observe(this, active -> {
            if (matchViewModel.getMatch() == null) return;
            if (active == null) return;
            // When isGameActive changes in the VM, this line is triggered
            binding.gameHeader.setVisibility(active ? View.VISIBLE : View.GONE);

            // Show/hide leave match button based on game state
            View leftDrawer = binding.leftDrawer.getHeaderView(0);
            View leaveMatchButton = leftDrawer.findViewById(R.id.leave_match);
            leaveMatchButton.setVisibility(active ? View.VISIBLE : View.GONE);
        });

        matchViewModel.getPlayer1Name().observe(this, name -> {
            if (matchViewModel.getMatch() == null) return;
            if (name == null) return;
            binding.gameHeader.setPlayerNames(name, matchViewModel.getPlayer2Name().getValue());
        });

        matchViewModel.getPlayer2Name().observe(this, name -> {
            if (matchViewModel.getMatch() == null) return;
            if (name == null) return;
            binding.gameHeader.setPlayerNames(matchViewModel.getPlayer1Name().getValue(), name);
        });

        matchViewModel.getPlayer1Score().observe(this, score -> {
            if (matchViewModel.getMatch() == null) return;
            if (score == null) return;
            binding.gameHeader.setStars(score, matchViewModel.getPlayer2Score().getValue() != null ? matchViewModel.getPlayer2Score().getValue() : 0);
        });

        matchViewModel.getPlayer2Score().observe(this, score -> {
            if (matchViewModel.getMatch() == null) return;
            if (score == null) return;
            binding.gameHeader.setStars(matchViewModel.getPlayer1Score().getValue() != null ? matchViewModel.getPlayer1Score().getValue() : 0, score);
        });

        matchViewModel.getActivePlayer().observe(this, playerId -> {
            if (matchViewModel.getMatch() == null) return;
            if (playerId == null) return;
            if(Objects.equals(matchViewModel.getMatch().getPlayer1Id(), playerId)){
                binding.gameHeader.setActivePlayer(1);
            }
            else{
                binding.gameHeader.setActivePlayer(2);
            }
        });
    }

    public void setToolbarTitle(String title) {
        binding.toolbarTitle.setText(title);
    }

    public ActivityAppBinding getBinding() {
        return binding;
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String notificationId = intent.getStringExtra(AppNotificationHelper.EXTRA_NOTIFICATION_ID);
        if (notificationId == null) {
            return;
        }

        FragmentTransition.to(
                NotificationTargetPlaceholderFragment.newInstance(notificationId),
                this,
                true,
                R.id.appContainer
        );
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }
}
package com.example.slagalica.presentation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ActivityAppBinding;
import com.example.slagalica.presentation.fragments.auth.LoginFragment;
import com.example.slagalica.presentation.fragments.common.FragmentTransition;
import com.example.slagalica.presentation.fragments.common.HomeFragment;
import com.example.slagalica.presentation.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppActivity extends AppCompatActivity {
    ActivityAppBinding binding;
    MatchViewModel matchViewModel;

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
        }

        // Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationOnClickListener(v -> {
            binding.main.openDrawer(GravityCompat.START);
        });

        // Drawer links
        View leftDrawer = binding.leftDrawer.getHeaderView(0);
        leftDrawer.findViewById(R.id.home).setOnClickListener(v -> {
            FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer);
            binding.main.closeDrawer(GravityCompat.START);
        });
    }

    // Helper method to make the view (activity) observe changes in VM
    private void observeViewModel(){
        matchViewModel.getIsGameActive().observe(this, active -> {
            // When isGameActive changes in the VM, this line is triggered
            binding.gameHeader.setVisibility(active ? View.VISIBLE : View.GONE);
        });
    }

}
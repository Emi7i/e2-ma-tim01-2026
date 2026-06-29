package com.example.slagalica.presentation.fragments.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.webkit.WebViewAssetLoader;

import com.example.slagalica.R;
import com.example.slagalica.databinding.FragmentRegionMapBinding;
import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.RegionStats;
import com.example.slagalica.presentation.activities.AppActivity;
import com.example.slagalica.presentation.viewmodels.RegionViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegionMapFragment extends Fragment {

    private FragmentRegionMapBinding binding;
    private RegionViewModel regionViewModel;
    private boolean mapLoaded = false;
    private List<RegionStats> pendingStats = null;

    @Inject
    SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegionMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AppActivity) {
            ((AppActivity) getActivity()).setToolbarTitle(getString(R.string.region_map_title));
        }

        regionViewModel = new ViewModelProvider(this).get(RegionViewModel.class);

        setupWebView();
        observeViewModel();
        regionViewModel.loadRegionStats();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = binding.mapWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Serve assets from https://appassets.androidplatform.net so that
        // the page origin is https:// and external fetch() calls (Overpass API) are allowed.
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .build();

        binding.mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mapLoaded = true;
                String userRegion = getCurrentUserRegion();
                if (userRegion != null && !userRegion.isEmpty()) {
                    view.evaluateJavascript(
                            "setCurrentUserRegion('" + escapeJs(userRegion) + "');", null);
                }
                if (pendingStats != null) {
                    pushStatsToMap(pendingStats);
                    pendingStats = null;
                }
            }
        });

        binding.mapWebView.loadUrl("https://appassets.androidplatform.net/assets/region_map.html");
    }

    private void observeViewModel() {
        regionViewModel.getRegionStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            if (mapLoaded) {
                pushStatsToMap(stats);
            } else {
                pendingStats = stats;
            }
            buildRankList(stats);
        });
    }

    private void pushStatsToMap(List<RegionStats> stats) {
        JSONObject json = new JSONObject();
        try {
            for (RegionStats s : stats) {
                JSONObject entry = new JSONObject();
                entry.put("stars",         s.getTotalMonthlyStars());
                entry.put("players",       s.getTotalPlayers());
                entry.put("activePlayers", s.getActivePlayers());
                entry.put("firstPlaces",   s.getFirstPlaces());
                entry.put("secondPlaces",  s.getSecondPlaces());
                entry.put("thirdPlaces",   s.getThirdPlaces());
                entry.put("rank",          s.getRank());
                json.put(s.getRegionKey(), entry);
            }
        } catch (JSONException ignored) {}
        String escaped = json.toString().replace("'", "\\'");
        binding.mapWebView.evaluateJavascript("setRegionData('" + escaped + "');", null);
    }

    private void buildRankList(List<RegionStats> stats) {
        binding.rankListContainer.removeAllViews();
        String userRegion = getCurrentUserRegion();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (RegionStats s : stats) {
            View item = inflater.inflate(R.layout.item_region_rank, binding.rankListContainer, false);

            TextView tvRank    = item.findViewById(R.id.tvRank);
            TextView tvIcon    = item.findViewById(R.id.tvIcon);
            TextView tvName    = item.findViewById(R.id.tvRegionName);
            TextView tvPlayers = item.findViewById(R.id.tvPlayerCount);
            TextView tvStars   = item.findViewById(R.id.tvStars);

            boolean isUserRegion = s.getRegionKey().equals(userRegion);

            tvRank.setText(rankLabel(s.getRank()));
            tvIcon.setText(s.getIcon());
            tvName.setText(isUserRegion ? s.getRegionKey() + " ★" : s.getRegionKey());
            tvPlayers.setText(s.getTotalPlayers() + " " + getString(R.string.region_players_label));
            tvStars.setText(s.getTotalMonthlyStars() + " ⭐");

            if (isUserRegion) {
                item.setBackgroundResource(R.color.blue_light);
            }

            binding.rankListContainer.addView(item);
        }
    }

    private String rankLabel(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return String.valueOf(rank);
        }
    }

    private String getCurrentUserRegion() {
        UserProfile profile = sessionManager.getCurrentProfile().getValue();
        return profile != null ? profile.getRegion() : null;
    }

    private String escapeJs(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

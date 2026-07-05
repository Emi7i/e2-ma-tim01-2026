package com.example.slagalica.presentation.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ItemRankingBinding;
import com.example.slagalica.domain.model.ranking.RankingEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RankingAdapter
        extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private final List<RankingEntry> entries = new ArrayList<>();
    private final String currentUserId;

    public RankingAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void submitList(List<RankingEntry> newEntries) {
        entries.clear();

        if (newEntries != null) {
            entries.addAll(newEntries);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemRankingBinding binding = ItemRankingBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new RankingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RankingViewHolder holder,
            int position
    ) {
        holder.bind(entries.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class RankingViewHolder extends RecyclerView.ViewHolder {

        private final ItemRankingBinding binding;

        RankingViewHolder(ItemRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RankingEntry entry, int placement) {
            binding.positionText.setText(String.valueOf(placement));
            binding.usernameText.setText(
                    entry.getUsername() == null
                            ? "Nepoznat igrač"
                            : entry.getUsername()
            );
            binding.leagueText.setText(
                    entry.getLeague() == null
                            ? "Liga nije određena"
                            : entry.getLeague()
            );
            binding.starsText.setText(
                    entry.getStarsEarned() + " ★"
            );

            binding.leagueIcon.setImageResource(
                    resolveLeagueBadge(entry.getLeague())
            );

            if (placement == 1) {
                binding.positionText.setTextColor(
                        Color.parseColor("#B8860B")
                );
            } else if (placement == 2) {
                binding.positionText.setTextColor(
                        Color.parseColor("#757575")
                );
            } else if (placement == 3) {
                binding.positionText.setTextColor(
                        Color.parseColor("#A05A2C")
                );
            } else {
                binding.positionText.setTextColor(
                        Color.parseColor("#222222")
                );
            }

            boolean isCurrentUser =
                    currentUserId != null
                            && currentUserId.equals(entry.getUserId());

            binding.rankingCard.setStrokeWidth(
                    isCurrentUser ? dp(2) : 0
            );
            binding.rankingCard.setStrokeColor(
                    Color.parseColor("#6A1B9A")
            );
        }

        private int resolveLeagueBadge(String league) {
            if (league == null || league.trim().isEmpty()) {
                return R.drawable.icon_rank;
            }

            String normalized = league
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "_");

            int resourceId = itemView.getResources().getIdentifier(
                    "league_badge_" + normalized,
                    "drawable",
                    itemView.getContext().getPackageName()
            );

            return resourceId == 0
                    ? R.drawable.icon_rank
                    : resourceId;
        }

        private int dp(int value) {
            return Math.round(
                    value * itemView.getResources()
                            .getDisplayMetrics()
                            .density
            );
        }
    }
}

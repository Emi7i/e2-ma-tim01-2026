package com.example.slagalica.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slagalica.R;
import com.example.slagalica.databinding.ItemTournamentPlayerBinding;
import com.example.slagalica.domain.model.tournament.TournamentParticipant;

import java.util.ArrayList;
import java.util.List;

public class TournamentPlayerAdapter extends RecyclerView.Adapter<TournamentPlayerAdapter.TournamentPlayerViewHolder> {

    private final List<TournamentParticipant> participants = new ArrayList<>();

    public void submitList(List<TournamentParticipant> newParticipants) {
        participants.clear();
        if (newParticipants != null) {
            participants.addAll(newParticipants);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TournamentPlayerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemTournamentPlayerBinding binding = ItemTournamentPlayerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TournamentPlayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TournamentPlayerViewHolder holder,
            int position
    ) {
        holder.bind(participants.get(position));
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class TournamentPlayerViewHolder extends RecyclerView.ViewHolder {

        private final ItemTournamentPlayerBinding binding;

        TournamentPlayerViewHolder(ItemTournamentPlayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TournamentParticipant participant) {
            binding.seedText.setText(String.valueOf(participant.getSeed()));
            binding.usernameText.setText(
                    participant.getUsername() == null
                            ? "Nepoznat igrač"
                            : participant.getUsername()
            );
            binding.leagueText.setText(
                    participant.getLeague() == null
                            ? "Liga nije određena"
                            : participant.getLeague()
            );
            binding.statusText.setText(resolveStatus(participant));
            binding.leagueIcon.setImageResource(resolveLeagueBadge(participant.getLeague()));
        }

        private String resolveStatus(TournamentParticipant participant) {
            if (participant.isWinner()) {
                return "Pobednik turnira";
            }
            if (participant.isEliminated()) {
                return "Eliminisan";
            }
            return "Aktivan";
        }

        private int resolveLeagueBadge(String league) {
            if (league == null) {
                return R.drawable.icon_rank;
            }

            String normalized = league.toLowerCase();
            if (normalized.contains("praktikant")) {
                return R.drawable.icon_rank;
            }
            if (normalized.contains("inženjer") || normalized.contains("inzenjer")) {
                return R.drawable.icon_rank;
            }
            return R.drawable.icon_rank;
        }
    }
}

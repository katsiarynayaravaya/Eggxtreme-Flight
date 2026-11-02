package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eggxtremeflight.databinding.ItemLeaderBinding;

import java.util.ArrayList;
import java.util.List;

public class LeadersAdapter extends RecyclerView.Adapter<LeadersAdapter.ViewHolder> {

    private final List<LeaderEntry> items = new ArrayList<>();
    private String currentUid;

    public void submit(List<LeaderEntry> newItems, String currentUid) {
        items.clear();
        items.addAll(newItems);
        this.currentUid = currentUid;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderBinding binding = ItemLeaderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderEntry entry = items.get(position);
        Context context = holder.binding.getRoot().getContext();
        boolean isCurrentUser = entry.uid.equals(currentUid);

        holder.binding.rankText.setText(String.valueOf(position + 1));
        holder.binding.nameText.setText(isCurrentUser
                ? context.getString(R.string.leaders_you_format, entry.nickname)
                : entry.nickname);
        holder.binding.scoreText.setText(String.valueOf(entry.bestScore));

        holder.binding.getRoot().setBackgroundColor(isCurrentUser
                ? ContextCompat.getColor(context, R.color.leader_highlight)
                : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLeaderBinding binding;

        ViewHolder(ItemLeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
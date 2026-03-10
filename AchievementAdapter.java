package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartbite.R;
import com.smartbite.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.VH> {

    private final List<Achievement> list = new ArrayList<>();

    public AchievementAdapter(List<Achievement> initial) {
        list.addAll(initial);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Achievement a = list.get(position);

        h.tvIcon.setText(a.getIcon());
        h.tvTitle.setText(a.getTitle());
        h.tvDesc.setText(a.getDescription());

        h.tvPoints.setText(h.tvPoints.getContext()
                .getString(R.string.achievement_points, a.getRewardPoints()));
        h.tvProgress.setText(h.tvProgress.getContext()
                .getString(R.string.achievement_progress, a.getCurrentCount(), a.getRequiredCount()));

        h.progressBar.setProgress((int) (a.getProgress() * 100));
        h.cardView.setAlpha(a.isUnlocked() ? 1.0f : 0.5f);
        h.tvUnlocked.setVisibility(a.isUnlocked() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<Achievement> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    static class VH extends RecyclerView.ViewHolder {

        final MaterialCardView cardView;
        final TextView tvIcon, tvTitle, tvDesc, tvPoints, tvProgress, tvUnlocked;
        final ProgressBar progressBar;

        VH(View v) {
            super(v);
            cardView    = v.findViewById(R.id.cardView);
            tvIcon      = v.findViewById(R.id.tvIcon);
            tvTitle     = v.findViewById(R.id.tvTitle);
            tvDesc      = v.findViewById(R.id.tvDesc);
            tvPoints    = v.findViewById(R.id.tvPoints);
            tvProgress  = v.findViewById(R.id.tvProgress);
            tvUnlocked  = v.findViewById(R.id.tvUnlocked);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }
}
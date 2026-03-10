package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbite.R;
import com.smartbite.models.MealPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.VH> {

    public interface OnPlanSelectListener {
        void onPlanSelected(MealPlan plan);
    }

    private final List<MealPlan> list = new ArrayList<>();
    private final OnPlanSelectListener listener;

    public MealPlanAdapter(List<MealPlan> initial, OnPlanSelectListener listener) {
        list.addAll(initial);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_plan, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MealPlan plan = list.get(position);

        h.tvName.setText(plan.getName());
        h.tvDesc.setText(plan.getDescription());
        h.tvBadge.setText(plan.getBadge());

        h.tvWeeklyPrice.setText(h.tvWeeklyPrice.getContext()
                .getString(R.string.price_weekly,
                        String.format(Locale.getDefault(), "%.0f", plan.getWeeklyPrice())));
        h.tvMonthlyPrice.setText(h.tvMonthlyPrice.getContext()
                .getString(R.string.price_monthly,
                        String.format(Locale.getDefault(), "%.0f", plan.getMonthlyPrice())));

        h.btnSubscribe.setOnClickListener(v -> listener.onPlanSelected(plan));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<MealPlan> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    @SuppressWarnings("WeakerAccess")
    static class VH extends RecyclerView.ViewHolder {

        final TextView tvName, tvDesc, tvWeeklyPrice, tvMonthlyPrice, tvBadge;
        final Button btnSubscribe;

        VH(View v) {
            super(v);
            tvName         = v.findViewById(R.id.tvName);
            tvDesc         = v.findViewById(R.id.tvDesc);
            tvWeeklyPrice  = v.findViewById(R.id.tvWeeklyPrice);
            tvMonthlyPrice = v.findViewById(R.id.tvMonthlyPrice);
            tvBadge        = v.findViewById(R.id.tvBadge);
            btnSubscribe   = v.findViewById(R.id.btnSubscribe);
        }
    }
}
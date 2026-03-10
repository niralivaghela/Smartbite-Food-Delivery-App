package com.smartbite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.smartbite.R;
import com.smartbite.models.Restaurant;

import java.util.List;
import java.util.Locale;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    public static final int TYPE_FULL       = 0;
    public static final int TYPE_SMALL      = 1;
    public static final int TYPE_HORIZONTAL = 2;

    // FIX 1: Load r1-r6 directly by position — don't rely on r.getImageResId()
    private static final int[] RESTAURANT_IMAGES = {
            R.drawable.r1, R.drawable.r2, R.drawable.r3,
            R.drawable.r4, R.drawable.r5, R.drawable.r6
    };

    private final Context ctx;
    private final OnRestaurantClickListener listener;
    private final int viewType;
    private List<Restaurant> list;

    public interface OnRestaurantClickListener {
        void onClick(Restaurant r);
    }

    public RestaurantAdapter(Context ctx, List<Restaurant> list,
                             OnRestaurantClickListener listener) {
        this(ctx, list, listener, TYPE_FULL);
    }

    public RestaurantAdapter(Context ctx, List<Restaurant> list,
                             OnRestaurantClickListener listener, int viewType) {
        this.ctx      = ctx;
        this.list     = list;
        this.listener = listener;
        this.viewType = viewType;
    }

    @Override public int getItemViewType(int position) { return viewType; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        int layout;
        if      (vt == TYPE_SMALL)      layout = R.layout.item_restaurant_small;
        else if (vt == TYPE_HORIZONTAL) layout = R.layout.item_restaurant_horizontal;
        else                            layout = R.layout.item_restaurant;
        View v = LayoutInflater.from(ctx).inflate(layout, parent, false);
        return new ViewHolder(v, vt);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Restaurant r = list.get(position);

        // ── Text fields (null-checked for all 3 view types) ──────────────────
        if (h.tvName        != null) h.tvName.setText(r.getName());
        if (h.tvCuisine     != null) h.tvCuisine.setText(r.getCuisine());
        if (h.tvRating      != null) h.tvRating.setText(
                String.format(Locale.getDefault(), "%.1f ★", r.getRating()));
        if (h.tvDeliveryFee != null) h.tvDeliveryFee.setText(
                ctx.getString(R.string.delivery_fee_format, r.getDeliveryFee()));
        if (h.tvPrepTime    != null) h.tvPrepTime.setText(
                ctx.getString(R.string.prep_time_format, r.getPrepTime()));
        if (h.tvOpenClosed  != null) {
            h.tvOpenClosed.setText(r.isOpen() ? R.string.status_open : R.string.status_closed);
            h.tvOpenClosed.setBackgroundResource(
                    r.isOpen() ? R.drawable.bg_badge_green : R.drawable.bg_badge_red);
        }

        // ── FIX 2: Always load image by position, never rely on getImageResId() ──
        if (h.ivRestaurant != null) {
            int fallback     = RESTAURANT_IMAGES[position % RESTAURANT_IMAGES.length];
            String remoteUrl = r.getImage();
            if (remoteUrl != null && !remoteUrl.isEmpty()) {
                Glide.with(ctx).load(remoteUrl)
                        .placeholder(R.drawable.bg_skeleton).error(fallback)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop().into(h.ivRestaurant);
            } else {
                Glide.with(ctx).load(fallback)
                        .placeholder(R.drawable.bg_skeleton)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop().into(h.ivRestaurant);
            }
        }

        // ── Food preview photos (TYPE_FULL only) ─────────────────────────────
        if (viewType == TYPE_FULL) {
            loadFoodPhoto(h.ivFood1, r, 0);
            loadFoodPhoto(h.ivFood2, r, 1);
            loadFoodPhoto(h.ivFood3, r, 2);
            if (h.tvFoodName1 != null) h.tvFoodName1.setText(getFoodName(r, 0));
            if (h.tvFoodName2 != null) h.tvFoodName2.setText(getFoodName(r, 1));
            if (h.tvFoodName3 != null) h.tvFoodName3.setText(getFoodName(r, 2));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    private void loadFoodPhoto(ImageView iv, Restaurant r, int index) {
        if (iv == null) return;
        List<String> photos = r.getFoodPhotos();
        if (photos != null && index < photos.size()
                && photos.get(index) != null && !photos.get(index).isEmpty()) {
            Glide.with(ctx).load(photos.get(index))
                    .placeholder(R.drawable.bg_skeleton)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop().into(iv);
            return;
        }
        List<Integer> resIds = r.getFoodResIds();
        if (resIds != null && index < resIds.size() && resIds.get(index) != null) {
            Glide.with(ctx).load(resIds.get(index))
                    .placeholder(R.drawable.bg_skeleton)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop().into(iv);
            return;
        }
        iv.setImageResource(R.drawable.bg_skeleton);
    }

    private String getFoodName(Restaurant r, int index) {
        List<String> names = r.getFoodNames();
        if (names != null && index < names.size()) return names.get(index);
        return "";
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    public void updateList(List<Restaurant> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return list.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                String oid = list.get(o).getId();
                String nid = newList.get(n).getId();
                return oid != null && oid.equals(nid);
            }
            @Override
            public boolean areContentsTheSame(int o, int n) {
                // FIX 3: Always return false so onBindViewHolder always fires.
                // imageResId is transient so equals() can't detect changes —
                // returning false ensures images always get re-loaded.
                return false;
            }
        });
        list = newList;
        result.dispatchUpdatesTo(this);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivRestaurant;
        final TextView  tvName, tvCuisine, tvRating, tvDeliveryFee, tvPrepTime, tvOpenClosed;

        ImageView ivFood1, ivFood2, ivFood3;
        TextView  tvFoodName1, tvFoodName2, tvFoodName3;

        ViewHolder(@NonNull View v, int viewType) {
            super(v);
            ivRestaurant  = v.findViewById(R.id.ivRestaurant);
            tvName        = v.findViewById(R.id.tvName);
            tvCuisine     = v.findViewById(R.id.tvCuisine);
            tvRating      = v.findViewById(R.id.tvRating);
            tvDeliveryFee = v.findViewById(R.id.tvDeliveryFee);
            tvPrepTime    = v.findViewById(R.id.tvPrepTime);
            tvOpenClosed  = v.findViewById(R.id.tvOpenClosed);

            if (viewType == TYPE_FULL) {
                ivFood1     = v.findViewById(R.id.ivFood1);
                ivFood2     = v.findViewById(R.id.ivFood2);
                ivFood3     = v.findViewById(R.id.ivFood3);
                tvFoodName1 = v.findViewById(R.id.tvFoodName1);
                tvFoodName2 = v.findViewById(R.id.tvFoodName2);
                tvFoodName3 = v.findViewById(R.id.tvFoodName3);
            }
        }
    }
}
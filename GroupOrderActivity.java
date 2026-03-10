package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.databinding.ActivityGroupOrderBinding;
import com.smartbite.models.GroupOrder;

import java.util.ArrayList;
import java.util.Random;

public class GroupOrderActivity extends AppCompatActivity {

    private ActivityGroupOrderBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentGroupCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupUI();
        playEntranceAnimation();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void setupToolbar() {
        // ViewBinding guarantees toolbar is NonNull — no null check needed
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_group_order);
        }
    }

    // ── Firebase ──────────────────────────────────────────────────────────────

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) currentUserId = user.getUid();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void setupUI() {
        // ViewBinding fields from layout are NonNull — no null checks needed
        binding.btnCreateGroup.setOnClickListener(v -> {
            animateButton(v);
            createGroupOrder();
        });

        binding.btnJoinGroup.setOnClickListener(v -> {
            animateButton(v);
            // Safe getText() — TextInputEditText.getText() can return null
            android.text.Editable editable = binding.etGroupCode.getText();
            String code = editable != null ? editable.toString().trim().toUpperCase() : "";
            if (code.isEmpty()) {
                Toast.makeText(this, R.string.hint_enter_group_code, Toast.LENGTH_SHORT).show();
                shakeView(binding.etGroupCode);
                return;
            }
            joinGroupOrder(code);
        });

        binding.btnShareCode.setOnClickListener(v -> {
            if (currentGroupCode != null) shareGroupCode(currentGroupCode);
        });

        binding.btnCopyCode.setOnClickListener(v -> {
            if (currentGroupCode != null) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(
                        getString(R.string.label_group_code), currentGroupCode);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.msg_code_copied, Toast.LENGTH_SHORT).show();
                animateCopySuccess();
            }
        });

        binding.btnStartOrder.setOnClickListener(v -> {
            animateButton(v);
            startActivity(new Intent(this, HomeActivity.class));
            Toast.makeText(this, R.string.msg_choose_items, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Create group ──────────────────────────────────────────────────────────

    private void createGroupOrder() {
        if (currentUserId == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        String code = generateShareCode();
        currentGroupCode = code;

        GroupOrder group = new GroupOrder();
        group.setGroupOrderId(code);
        group.setHostUserId(currentUserId);
        group.setShareCode(code);
        group.setStatus("active");
        group.setMemberIds(new ArrayList<>());

        db.collection("groupOrders").document(code).set(group)
                .addOnSuccessListener(x -> {
                    setLoading(false);
                    showGroupCreated(code);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showGroupCreated(code); // show locally on failure
                });
    }

    // ── Join group ────────────────────────────────────────────────────────────

    private void joinGroupOrder(String code) {
        setLoading(true);
        db.collection("groupOrders").document(code).get()
                .addOnSuccessListener(doc -> {
                    setLoading(false);
                    if (doc.exists()) {
                        currentGroupCode = code;
                        showGroupJoined(code);
                    } else {
                        Toast.makeText(this, R.string.error_group_not_found,
                                Toast.LENGTH_SHORT).show();
                        shakeView(binding.etGroupCode);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            getString(R.string.error_generic, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Show states ───────────────────────────────────────────────────────────

    private void showGroupCreated(String code) {
        binding.layoutCreateJoin.setVisibility(View.GONE);
        binding.layoutGroupActive.setVisibility(View.VISIBLE);
        binding.tvGroupCode.setText(code);
        // Use getString() — not string literals — to avoid "cannot be translated" warning
        binding.tvGroupRole.setText(getString(R.string.label_you_are_host));
        binding.tvMemberCount.setText(getString(R.string.label_one_member));

        binding.cardGroupCode.setScaleX(0.5f);
        binding.cardGroupCode.setScaleY(0.5f);
        binding.cardGroupCode.setAlpha(0f);
        binding.cardGroupCode.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        Toast.makeText(this,
                getString(R.string.msg_group_created, code),
                Toast.LENGTH_LONG).show();
    }

    private void showGroupJoined(String code) {
        binding.layoutCreateJoin.setVisibility(View.GONE);
        binding.layoutGroupActive.setVisibility(View.VISIBLE);
        binding.tvGroupCode.setText(code);
        binding.tvGroupRole.setText(getString(R.string.label_you_joined));
        binding.tvMemberCount.setText(getString(R.string.label_joined_group));

        Toast.makeText(this,
                getString(R.string.msg_group_joined, code),
                Toast.LENGTH_SHORT).show();
    }

    // ── Share ─────────────────────────────────────────────────────────────────

    private void shareGroupCode(String code) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_group, code));
        startActivity(Intent.createChooser(share,
                getString(R.string.label_share_code)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateShareCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnCreateGroup.setEnabled(!loading);
        binding.btnJoinGroup.setEnabled(!loading);
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void animateButton(View v) {
        v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80)
                .withEndAction(() -> v.animate()
                        .scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    private void animateCopySuccess() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.btnCopyCode, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.btnCopyCode, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300).start();
    }

    private void shakeView(View view) {
        ObjectAnimator.ofFloat(view, "translationX",
                        0f, -18f, 18f, -12f, 12f, -6f, 6f, 0f)
                .setDuration(500).start();
    }

    private void playEntranceAnimation() {
        View[] views = {binding.cardCreate, binding.cardJoin};
        int delay = 100;
        for (View v : views) {
            v.setAlpha(0f);
            v.setTranslationY(60f);
            v.animate().alpha(1f).translationY(0f)
                    .setDuration(450).setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
            delay += 150;
        }
    }
}
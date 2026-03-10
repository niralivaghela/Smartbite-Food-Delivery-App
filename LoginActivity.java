package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import com.smartbite.R;
import com.smartbite.databinding.ActivityLoginBinding;
import com.smartbite.models.User;
import com.smartbite.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null && account.getIdToken() != null) {
                        firebaseAuthWithGoogle(account.getIdToken());
                    } else {
                        showError(getString(R.string.error_google_signin));
                    }
                } catch (ApiException e) {
                    showError(getString(R.string.error_google_signin) + " (" + e.getStatusCode() + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        setupGoogleSignIn();
        setupClickListeners();
        playEntranceAnimation();
    }

    // ── Entrance animation ────────────────────────────────────────────────────

    private void playEntranceAnimation() {
        // All children start invisible / translated down
        View[] views = {
                binding.tvSkip, binding.tilEmail, binding.tilPassword,
                binding.tvForgotPassword, binding.btnLogin,
                binding.btnGoogleSignIn
        };
        for (View v : views) { v.setAlpha(0f); v.setTranslationY(40f); }

        int delay = 200;
        for (View v : views) {
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(420)
                    .setStartDelay(delay)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            delay += 80;
        }

        // Logo / title bounce in
        ObjectAnimator titleScale = ObjectAnimator.ofFloat(binding.getRoot(), "scaleX", 0.95f, 1f);
        titleScale.setDuration(500);
        titleScale.setInterpolator(new OvershootInterpolator());
        titleScale.start();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();
    }

    private void setupClickListeners() {
        binding.tvSkip.setOnClickListener(v -> {
            animateButtonClick(v);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            animateButtonClick(v);
            String email = binding.etEmail.getText().toString().trim();
            String pass  = binding.etPassword.getText().toString().trim();
            if (validateInput(email, pass)) loginWithEmail(email, pass);
        });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            animateButtonClick(v);
            googleSignInClient.signOut().addOnCompleteListener(task ->
                    googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        });

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendPasswordReset(email);
            } else {
                binding.tilEmail.setError(getString(R.string.error_enter_email_reset));
                binding.tilEmail.requestFocus();
            }
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateInput(String email, String pass) {
        boolean valid = true;
        if (TextUtils.isEmpty(email) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_valid_email));
            shakeView(binding.tilEmail);
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_length));
            shakeView(binding.tilPassword);
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        return valid;
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    private void loginWithEmail(String email, String pass) {
        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    showLoading(false);
                    checkUserRoleAndRedirect(r.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    shakeView(binding.tilPassword);
                    showError(getString(R.string.error_login_failed, e.getMessage()));
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(r -> {
                    showLoading(false);
                    FirebaseUser user = r.getUser();
                    if (user == null) return;
                    boolean isNew = r.getAdditionalUserInfo() != null
                            && r.getAdditionalUserInfo().isNewUser();
                    if (isNew) {
                        User u = new User(user.getUid(), user.getDisplayName(),
                                user.getEmail(), "", Constants.ROLE_CUSTOMER);
                        if (user.getPhotoUrl() != null) u.setProfilePic(user.getPhotoUrl().toString());
                        db.collection(Constants.COLLECTION_USERS).document(user.getUid()).set(u)
                                .addOnSuccessListener(x -> goHome())
                                .addOnFailureListener(e ->
                                        showError(getString(R.string.error_save_user, e.getMessage())));
                    } else {
                        checkUserRoleAndRedirect(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError(getString(R.string.error_auth_failed));
                });
    }

    private void checkUserRoleAndRedirect(String uid) {
        db.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    String role = doc.exists() ? doc.getString("role") : Constants.ROLE_CUSTOMER;
                    Intent intent;
                    if (Constants.ROLE_RESTAURANT.equals(role))  intent = new Intent(this, RestaurantPanelActivity.class);
                    else if (Constants.ROLE_AGENT.equals(role))  intent = new Intent(this, DeliveryAgentActivity.class);
                    else if (Constants.ROLE_ADMIN.equals(role))  intent = new Intent(this, AdminActivity.class);
                    else                                          intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .addOnFailureListener(e -> goHome());
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void sendPasswordReset(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(u -> {
                    binding.tilEmail.setError(null);
                    Toast.makeText(this, "✅ " + getString(R.string.msg_reset_email_sent), Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> showError(getString(R.string.error_generic, e.getMessage())));
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private void showLoading(boolean show) {
        binding.btnLogin.setEnabled(!show);
        binding.btnGoogleSignIn.setEnabled(!show);
        binding.tvSkip.setEnabled(!show);
        binding.btnLogin.setText(show ? R.string.btn_loading : R.string.btn_login);
        if (show) {
            binding.btnLogin.setAlpha(0.75f);
        } else {
            binding.btnLogin.animate().alpha(1f).setDuration(200).start();
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** Subtle horizontal shake animation for invalid fields */
    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view,
                "translationX", 0f, -18f, 18f, -12f, 12f, -6f, 6f, 0f);
        shake.setDuration(500);
        shake.start();
    }

    /** Quick scale-down / scale-up on button click */
    private void animateButtonClick(View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }
}
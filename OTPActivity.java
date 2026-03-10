package com.smartbite.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartbite.R;
import com.smartbite.databinding.ActivityOtpBinding;

import java.util.Locale;

public class OTPActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private CountDownTimer countDownTimer;
    private String correctOtp = "";
    private EditText[] fields;

    private static final long RESEND_MILLIS = 60_000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        // ✅ Fixed: deprecated onBackPressed replaced
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        correctOtp = getIntent().getStringExtra("otp");

        if (phoneNumber != null)
            binding.tvPhoneNumber.setText(getString(R.string.msg_otp_sent, phoneNumber));

        setupOtpInputs();
        startTimer();
        playEntranceAnim();

        binding.btnVerifyOtp.setOnClickListener(v -> {
            animateButton(v);
            verifyOtp();
        });

        binding.tvResendOtp.setOnClickListener(v -> {
            clearOtpFields();
            startTimer();
            resetBoxColors();
            Toast.makeText(this, R.string.resend_otp, Toast.LENGTH_SHORT).show();
        });
    }

    // ── OTP input setup ───────────────────────────────────────────────────────

    private void setupOtpInputs() {
        fields = new EditText[]{
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
        };

        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null) continue;
            final int idx = i;
            fields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (s.length() == 1 && idx < fields.length - 1 && fields[idx + 1] != null) {
                        fields[idx + 1].requestFocus();
                    } else if (s.length() == 0 && idx > 0 && fields[idx - 1] != null) {
                        fields[idx - 1].requestFocus();
                    }
                    // Auto-verify when all 6 digits entered
                    if (getEnteredOtp().length() == 6) verifyOtp();
                }
            });
        }

        if (fields[0] != null) fields[0].requestFocus();
    }

    private String getEnteredOtp() {
        StringBuilder sb = new StringBuilder();
        for (EditText f : fields) {
            if (f != null && f.getText().length() > 0)
                sb.append(f.getText().toString());
        }
        return sb.toString();
    }

    private void clearOtpFields() {
        for (EditText f : fields) {
            if (f != null) f.setText("");
        }
        if (fields[0] != null) fields[0].requestFocus();
    }

    private void resetBoxColors() {
        for (EditText f : fields) {
            if (f != null) f.setBackgroundResource(R.drawable.bg_otp_box);
        }
    }

    // ── Verification ──────────────────────────────────────────────────────────

    private void verifyOtp() {
        String entered = getEnteredOtp();
        if (entered.length() < 6) {
            shakeOtpBoxes();
            Toast.makeText(this, getString(R.string.label_enter_otp), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check against passed OTP (or any logic)
        if (correctOtp != null && !correctOtp.isEmpty()) {
            if (entered.equals(correctOtp)) {
                onVerificationSuccess();
            } else {
                onVerificationFailed();
            }
        } else {
            // Demo — accept any 6-digit OTP
            onVerificationSuccess();
        }
    }

    private void onVerificationSuccess() {
        binding.btnVerifyOtp.setText(R.string.msg_otp_verified);
        binding.btnVerifyOtp.setEnabled(false);
        Toast.makeText(this, getString(R.string.msg_otp_verified), Toast.LENGTH_SHORT).show();

        // Animate success then finish
        binding.getRoot().animate().alpha(0.8f).setDuration(300)
                .withEndAction(() -> {
                    setResult(RESULT_OK);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }).start();
    }

    private void onVerificationFailed() {
        shakeOtpBoxes();
        clearOtpFields();
        Toast.makeText(this, getString(R.string.error_invalid_otp), Toast.LENGTH_SHORT).show();
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void startTimer() {
        binding.tvResendOtp.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(RESEND_MILLIS, 1000) {
            @Override
            public void onTick(long ms) {
                long secs = ms / 1000;
                binding.tvCountdown.setText(getString(R.string.label_resend_in, secs));
            }
            @Override
            public void onFinish() {
                binding.tvCountdown.setText("");
                binding.tvResendOtp.setEnabled(true);
            }
        }.start();
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void playEntranceAnim() {
        binding.cardOtp.setAlpha(0f);
        binding.cardOtp.setTranslationY(60f);
        binding.cardOtp.animate().alpha(1f).translationY(0f)
                .setDuration(500).setInterpolator(new OvershootInterpolator(1.2f)).start();
    }

    private void shakeOtpBoxes() {
        for (EditText f : fields) {
            if (f == null) continue;
            ObjectAnimator shake = ObjectAnimator.ofFloat(f, "translationX",
                    0f, -12f, 12f, -8f, 8f, -4f, 4f, 0f);
            shake.setDuration(400).start();
        }
    }

    private void animateButton(View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
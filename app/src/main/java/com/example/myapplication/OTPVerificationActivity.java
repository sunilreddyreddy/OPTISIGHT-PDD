package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Premium OTP Verification Activity for OptiSight.
 * Features: 6-digit input with auto-focus, countdown timer, resend logic, and premium animations.
 */
public class OTPVerificationActivity extends AppCompatActivity {

    private final EditText[] etOtp = new EditText[6];
    private TextView tvTimer, tvResend, tvChangeEmail, tvTitle, tvSubtitle, tvDescription;
    private MaterialButton btnVerify;
    private ProgressBar pbVerify;
    private View vPulse, vFloat1, vFloat2, otpCard;
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;
    private String email;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get email from intent extras
        email = getIntent().getStringExtra("email");
        
        initViews();
        applyAnimations();
        setupOtpInputs();
        startTimer();

        // Handle Back button click
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Handle Change Email click
        tvChangeEmail.setOnClickListener(v -> finish());

        // Handle Resend OTP click
        tvResend.setOnClickListener(v -> {
            if (isResendEnabled) {
                resendOtp();
            }
        });

        // Handle Verify button click
        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void initViews() {
        // Initialize OTP EditText array
        etOtp[0] = findViewById(R.id.etOtp1);
        etOtp[1] = findViewById(R.id.etOtp2);
        etOtp[2] = findViewById(R.id.etOtp3);
        etOtp[3] = findViewById(R.id.etOtp4);
        etOtp[4] = findViewById(R.id.etOtp5);
        etOtp[5] = findViewById(R.id.etOtp6);

        // UI Components
        tvTimer = findViewById(R.id.tvTimer);
        tvResend = findViewById(R.id.tvResend);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnVerify = findViewById(R.id.btnVerify);
        pbVerify = findViewById(R.id.pbVerify);
        
        // Animation Elements
        vPulse = findViewById(R.id.vPulse);
        vFloat1 = findViewById(R.id.vFloat1);
        vFloat2 = findViewById(R.id.vFloat2);
        otpCard = findViewById(R.id.otpCard);
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        
        // Apply fade-in to text and card
        tvTitle.startAnimation(fadeIn);
        tvSubtitle.startAnimation(fadeIn);
        tvDescription.startAnimation(fadeIn);
        otpCard.startAnimation(fadeIn);
        
        // Apply pulsing animation to background elements
        if (vPulse != null) vPulse.startAnimation(pulse);
        if (vFloat1 != null) vFloat1.startAnimation(pulse);
        if (vFloat2 != null) vFloat2.startAnimation(pulse);
    }

    private void setupOtpInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            
            // Auto-focus to next box
            etOtp[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < 5) {
                        etOtp[index + 1].requestFocus();
                    }
                    checkOtpCompletion();
                }
            });

            // Auto-backspace to previous box
            etOtp[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etOtp[index].getText().length() == 0 && index > 0) {
                        etOtp[index - 1].requestFocus();
                        etOtp[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void checkOtpCompletion() {
        StringBuilder otp = new StringBuilder();
        for (EditText et : etOtp) {
            otp.append(et.getText().toString());
        }
        // Enable Verify button only when all 6 digits are entered
        btnVerify.setEnabled(otp.length() == 6);
    }

    private void startTimer() {
        isResendEnabled = false;
        tvResend.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
        tvResend.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format(Locale.getDefault(), 
                    getString(R.string.resend_in), millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isResendEnabled = true;
                tvTimer.setVisibility(View.GONE);
                tvResend.setEnabled(true);
                tvResend.setTextColor(ContextCompat.getColor(OTPVerificationActivity.this, R.color.secondary_cyan));
            }
        }.start();
    }

    private void resendOtp() {
        Toast.makeText(this, "A new OTP has been sent!", Toast.LENGTH_SHORT).show();
        startTimer();
        // Clear inputs for new OTP
        for (EditText et : etOtp) {
            et.setText("");
        }
        etOtp[0].requestFocus();
    }

    private void verifyOtp() {
        showLoading(true);
        
        // Simulate background verification process
        new Handler().postDelayed(() -> {
            StringBuilder otp = new StringBuilder();
            for (EditText et : etOtp) {
                otp.append(et.getText().toString());
            }

            showLoading(false);

            // Mock verification: Using '123456' as the success code
            if (otp.toString().equals("123456")) {
                Toast.makeText(this, getString(R.string.otp_verified), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ResetPasswordActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.otp_error), Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnVerify.setText("");
            btnVerify.setEnabled(false);
            pbVerify.setVisibility(View.VISIBLE);
        } else {
            btnVerify.setText(R.string.verify_otp);
            btnVerify.setEnabled(true);
            pbVerify.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

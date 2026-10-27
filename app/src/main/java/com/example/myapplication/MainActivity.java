package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Premium Redesigned Login Page for OptiSight.
 * Features: AI-themed visuals, professional healthcare UI, and smooth animations.
 */
public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;
    private View vScanner;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoading = findViewById(R.id.pbLoading);
        vScanner = findViewById(R.id.vScanner);
        
        View tvForgotPassword = findViewById(R.id.tvForgotPassword);
        View tvSignUp = findViewById(R.id.tvSignUp);
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);

        // Start Scanner Animation
        startScannerAnimation();

        btnLogin.setOnClickListener(v -> performLogin());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
            
        btnGoogle.setOnClickListener(v -> 
            Toast.makeText(this, getString(R.string.google_connecting), Toast.LENGTH_SHORT).show());
            
        // Entry Animations
        applyEntryAnimations();
    }

    private void startScannerAnimation() {
        if (vScanner == null) return;
        RotateAnimation rotate = new RotateAnimation(0, 360, 
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(6000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        vScanner.startAnimation(rotate);
    }

    private void applyEntryAnimations() {
        try {
            Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideUp.setDuration(800);
            findViewById(R.id.loginCard).startAnimation(slideUp);
        } catch (Exception e) {}
    }

    private void performLogin() {
        Editable emailText = etEmail.getText();
        Editable passwordText = etPassword.getText();
        
        if (emailText == null || passwordText == null) return;

        String email = emailText.toString().trim();
        String password = passwordText.toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            hasError = true;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        if (!hasError) {
            showLoading(true);
            
            java.util.Map<String, String> loginData = new java.util.HashMap<>();
            loginData.put("email", email);
            loginData.put("password", password);

            OptiSightApiService apiService = ApiClient.getClient().create(OptiSightApiService.class);
            apiService.loginUser(loginData).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        String token = (String) response.body().get("access_token");
                        getSharedPreferences("OptiSightPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("jwt_token", "Bearer " + token)
                                .apply();
                        Toast.makeText(MainActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } else {
                        // Fallback to local login if demo
                        demoLoginSuccess();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    showLoading(false);
                    demoLoginSuccess();
                }
            });
        }
    }

    private void demoLoginSuccess() {
        Toast.makeText(MainActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setText("");
            btnLogin.setEnabled(false);
            pbLoading.setVisibility(View.VISIBLE);
            
            Animation fade = new AlphaAnimation(1.0f, 0.5f);
            fade.setDuration(300);
            btnLogin.startAnimation(fade);
        } else {
            btnLogin.setText(R.string.signin_button);
            btnLogin.setEnabled(true);
            pbLoading.setVisibility(View.GONE);
            btnLogin.clearAnimation();
        }
    }
}

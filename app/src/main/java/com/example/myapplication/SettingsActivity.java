package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Modern Settings Activity for OptiSight.
 * Features: Profile management, App settings, Privacy & Security, and Support.
 */
public class SettingsActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileSettings;
    private TextView tvUserNameSettings, tvUserEmailSettings, tvCurrentLanguage;
    private MaterialButton btnEditProfileSettings, btnLogoutSettings;
    private SwitchMaterial switchNotifications, switchDarkMode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupToolbar();
        loadSettingsData();
        setupClickListeners();
    }

    private void initViews() {
        ivProfileSettings = findViewById(R.id.ivProfileSettings);
        tvUserNameSettings = findViewById(R.id.tvUserNameSettings);
        tvUserEmailSettings = findViewById(R.id.tvUserEmailSettings);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        btnEditProfileSettings = findViewById(R.id.btnEditProfileSettings);
        btnLogoutSettings = findViewById(R.id.btnLogoutSettings);
        
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettingsData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        tvUserNameSettings.setText(prefs.getString("fullName", "Sunil Reddy"));
        tvUserEmailSettings.setText(prefs.getString("email", "sunil.reddy@example.com"));
        
        String profilePhotoUri = prefs.getString("profilePhotoUri", null);
        if (profilePhotoUri != null && ivProfileSettings != null) {
            try {
                ivProfileSettings.setImageURI(Uri.parse(profilePhotoUri));
            } catch (Exception e) {
                ivProfileSettings.setImageResource(R.drawable.ic_person);
            }
        }

        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES));

        updateLanguageDisplay();
    }

    private void updateLanguageDisplay() {
        String langCode = LocaleHelper.getLanguage(this);
        String langName;
        switch (langCode) {
            case "te": langName = "Telugu 🇮🇳"; break;
            case "hi": langName = "Hindi 🇮🇳"; break;
            case "ta": langName = "Tamil 🇮🇳"; break;
            case "ml": langName = "Malayalam 🇮🇳"; break;
            default: langName = "English 🇺🇸"; break;
        }
        tvCurrentLanguage.setText(langName);
    }

    private void setupClickListeners() {
        btnEditProfileSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.llLanguage).setOnClickListener(v -> showLanguageDialog());

        findViewById(R.id.tvChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });

        findViewById(R.id.tvAccountSecurity).setOnClickListener(v -> 
            Toast.makeText(this, "Security settings coming soon...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tvContactSupport).setOnClickListener(v -> 
            Toast.makeText(this, "Opening support center...", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.tvTermsPrivacy).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Terms & Privacy Policy...", Toast.LENGTH_SHORT).show());

        btnLogoutSettings.setOnClickListener(v -> confirmLogout());
        
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notifications", isChecked);
            Toast.makeText(this, isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show();
        });
        
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveSetting("dark_mode", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveSetting("dark_mode", false);
            }
        });
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void showLanguageDialog() {
        String[] languages = {"English 🇺🇸", "Telugu 🇮🇳", "Hindi 🇮🇳", "Tamil 🇮🇳", "Malayalam 🇮🇳"};
        String[] langCodes = {"en", "te", "hi", "ta", "ml"};
        
        new AlertDialog.Builder(this)
                .setTitle("Select App Language")
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    LocaleHelper.setLocale(this, selectedLang);
                    
                    Toast.makeText(this, "Language changed to " + languages[which], Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

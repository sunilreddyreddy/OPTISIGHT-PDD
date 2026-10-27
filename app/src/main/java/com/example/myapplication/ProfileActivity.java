package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import android.content.SharedPreferences;
import android.net.Uri;

/**
 * Premium User Profile Activity for OptiSight.
 * Displays user details, vision statistics, achievements, and account management.
 */
public class ProfileActivity extends AppCompatActivity {

    private MaterialButton btnEditProfile, btnViewReports, btnLogout;
    private ImageButton btnBack, btnSettingsTop;
    private BottomNavigationView bottomNavigation;
    private TextView tvUserNameHeader, tvFullName, tvEmail, tvMobile, tvAgeGender;
    private com.google.android.material.imageview.ShapeableImageView ivProfileLarge;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupBottomNavigation();
        applyAnimations();
        loadProfileData();

        // Header Actions
        btnBack.setOnClickListener(v -> finish());
        
        btnSettingsTop.setOnClickListener(v -> 
            Toast.makeText(this, "Opening Security & App Settings...", Toast.LENGTH_SHORT).show());

        // Quick Actions
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, VisionReportActivity.class);
            startActivity(intent);
        });
            
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            // Clear activity stack for security on logout
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Profile Photo Click
        findViewById(R.id.profileFrame).setOnClickListener(v ->
            Toast.makeText(this, "Updating Profile Picture...", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "Sunil Kumar Yadav");
        String email = prefs.getString("email", "sunil.kumar@example.com");
        String mobile = prefs.getString("mobile", "+91 93925 71978");
        String age = prefs.getString("age", "24");
        String gender = prefs.getString("gender", "Male");
        String profilePhotoUri = prefs.getString("profilePhotoUri", null);

        if (tvUserNameHeader != null) tvUserNameHeader.setText(fullName);
        if (tvFullName != null) tvFullName.setText(fullName);
        if (tvEmail != null) tvEmail.setText(email);
        if (tvMobile != null) tvMobile.setText(mobile);
        if (tvAgeGender != null) tvAgeGender.setText(getString(R.string.age_gender_param, age, gender));
        if (ivProfileLarge != null && profilePhotoUri != null) {
            try {
                ivProfileLarge.setImageURI(Uri.parse(profilePhotoUri));
            } catch (Exception e) {
                ivProfileLarge.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettingsTop = findViewById(R.id.btnSettingsTop);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        tvUserNameHeader = findViewById(R.id.tvUserNameHeader);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvMobile = findViewById(R.id.tvMobile);
        tvAgeGender = findViewById(R.id.tvAgeGender);
        ivProfileLarge = findViewById(R.id.ivProfileLarge);
    }

    private void setupBottomNavigation() {
        // Set Profile as selected item
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // FIX: Navigate to HomeActivity
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_analysis) {
                startActivity(new Intent(this, UploadEyeImageActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, VisionReportActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void applyAnimations() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            findViewById(R.id.headerLayout).startAnimation(fadeIn);
            findViewById(R.id.ivProfileLarge).startAnimation(fadeIn);
        } catch (Exception e) {}
    }
}

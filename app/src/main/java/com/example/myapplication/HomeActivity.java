package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Premium Home Dashboard for OptiSight.
 * Features: Professional healthcare UI, AI hero card with scanning animation, and quick actions.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvGreetingHome;
    private ImageView ivProfileHome, ivNotifications, ivSettingsHome;
    private MaterialCardView cardHero, cardActionAnalysis, cardActionLens, cardActionReports, cardActionTips;
    private View vScannerHero;
    private BottomNavigationView bottomNavHome;
    private FloatingActionButton fabAiAssistant;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupNavigation();
        startHeroScannerAnimation();
        applyEntranceAnimations();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        tvGreetingHome = findViewById(R.id.tvGreetingHome);
        ivProfileHome = findViewById(R.id.ivProfileHome);
        ivNotifications = findViewById(R.id.ivNotifications);
        ivSettingsHome = findViewById(R.id.ivSettingsHome);
        
        cardHero = findViewById(R.id.cardHero);
        vScannerHero = findViewById(R.id.vScannerHero);
        
        cardActionAnalysis = findViewById(R.id.cardActionAnalysis);
        cardActionLens = findViewById(R.id.cardActionLens);
        cardActionReports = findViewById(R.id.cardActionReports);
        cardActionTips = findViewById(R.id.cardActionTips);
        
        bottomNavHome = findViewById(R.id.bottomNavHome);
        fabAiAssistant = findViewById(R.id.fabAiAssistant);
    }

    private void startHeroScannerAnimation() {
        if (vScannerHero == null) return;
        RotateAnimation rotate = new RotateAnimation(0, 360, 
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(8000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        vScannerHero.startAnimation(rotate);
    }

    private void setupClickListeners() {
        // Hero Card
        findViewById(R.id.btnStartAnalysisHero).setOnClickListener(v -> handleVisionAnalysisClick());
        cardHero.setOnClickListener(v -> handleVisionAnalysisClick());

        // Quick Actions
        cardActionAnalysis.setOnClickListener(v -> handleVisionAnalysisClick());
        cardActionLens.setOnClickListener(v -> startActivity(new Intent(this, LensRecommendationActivity.class)));
        cardActionReports.setOnClickListener(v -> startActivity(new Intent(this, VisionReportActivity.class)));
        cardActionTips.setOnClickListener(v -> startActivity(new Intent(this, EyeHealthTipsActivity.class)));

        // Other Sections
        findViewById(R.id.btnViewFullReport).setOnClickListener(v -> startActivity(new Intent(this, VisionReportActivity.class)));
        findViewById(R.id.btnViewLensRec).setOnClickListener(v -> startActivity(new Intent(this, LensRecommendationActivity.class)));

        // Header
        ivNotifications.setOnClickListener(v -> Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
        ivSettingsHome.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        ivProfileHome.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        
        // FAB
        fabAiAssistant.setOnClickListener(v -> Toast.makeText(this, "AI Assistant: How can I help you today?", Toast.LENGTH_SHORT).show());
    }

    private void handleVisionAnalysisClick() {
        startActivity(new Intent(this, UploadEyeImageActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        if (bottomNavHome != null) {
            bottomNavHome.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("fullName", "Sunil Kumar");
        if (tvGreetingHome != null) tvGreetingHome.setText(name + "!");
        
        String profilePhotoUri = prefs.getString("profilePhotoUri", null);
        if (profilePhotoUri != null && ivProfileHome != null) {
            try {
                ivProfileHome.setImageURI(Uri.parse(profilePhotoUri));
            } catch (Exception e) {
                ivProfileHome.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void setupNavigation() {
        if (bottomNavHome == null) return;
        bottomNavHome.setSelectedItemId(R.id.nav_home);
        bottomNavHome.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_analysis) {
                handleVisionAnalysisClick();
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, VisionReportActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void applyEntranceAnimations() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideUp.setDuration(1000);

            findViewById(R.id.headerLayout).startAnimation(fadeIn);
            cardHero.startAnimation(slideUp);
        } catch (Exception e) {}
    }
}

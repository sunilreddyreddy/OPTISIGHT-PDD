package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Premium Upload Eye Image Activity for OptiSight.
 * Features: Camera/Gallery integration, image preview, and AI analysis navigation.
 */
public class UploadEyeImageActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView ivPreview;
    private LinearLayout layoutPlaceholder;
    private MaterialButton btnCamera, btnGallery, btnAnalyze, btnRetake;
    private MaterialCardView cardPreviewInfo;
    private TextView tvFileName, tvQuality;
    private View loadingOverlay;
    private BottomNavigationView bottomNav;
    
    private Uri imageUri; // Store the real image URI

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_eye_image);

        initViews();
        setupNavigation();
        applyAnimations();

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnRetake.setOnClickListener(v -> resetState());
        
        btnAnalyze.setOnClickListener(v -> startAnalysis());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHelp).setOnClickListener(v -> 
            Toast.makeText(this, "Ensure your eye is centered and in focus.", Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        ivPreview = findViewById(R.id.ivPreview);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnRetake = findViewById(R.id.btnRetake);
        cardPreviewInfo = findViewById(R.id.cardPreviewInfo);
        tvFileName = findViewById(R.id.tvFileName);
        tvQuality = findViewById(R.id.tvQuality);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraCaptureActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageUri = data.getData();
                if (imageUri != null) {
                    ivPreview.setImageURI(imageUri);
                    showPreview("captured_eye.jpg");
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                imageUri = data.getData();
                if (imageUri != null) {
                    ivPreview.setImageURI(imageUri);
                    showPreview("selected_gallery_image.jpg");
                }
            }
        }
    }

    private void showPreview(String fileName) {
        layoutPlaceholder.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        cardPreviewInfo.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(true);
        
        tvFileName.setText(getString(R.string.file_name_label, fileName));
        tvQuality.setText(getString(R.string.quality_indicator, getString(R.string.quality_good)));
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivPreview.startAnimation(fadeIn);
        cardPreviewInfo.startAnimation(fadeIn);

        // Update persistence for Home Dashboard validation
        SharedPreferences.Editor editor = getSharedPreferences("OptiSightData", MODE_PRIVATE).edit();
        editor.putBoolean("isEyeImageUploaded", true);
        editor.apply();
    }

    private void resetState() {
        layoutPlaceholder.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);
        cardPreviewInfo.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        btnAnalyze.setEnabled(false);
        ivPreview.setImageDrawable(null);
        imageUri = null;
        
        SharedPreferences.Editor editor = getSharedPreferences("OptiSightData", MODE_PRIVATE).edit();
        editor.putBoolean("isEyeImageUploaded", false);
        editor.apply();
    }

    private void startAnalysis() {
        if (imageUri == null) return;

        loadingOverlay.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);
        
        new Handler().postDelayed(() -> {
            loadingOverlay.setVisibility(View.GONE);
            Intent intent = new Intent(this, VisionAnalysisActivity.class);
            intent.putExtra("imageUri", imageUri.toString()); // PASS REAL IMAGE
            startActivity(intent);
        }, 1000);
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_analysis);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, VisionReportActivity.class));
                return true;
            }
            return false;
        });
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.headerLayout).startAnimation(fadeIn);
    }
}

package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Premium Camera Capture Activity for OptiSight.
 * Uses CameraX for reliable eye image capture with alignment guides.
 */
public class CameraCaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraCaptureActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private PreviewView viewFinder;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private Camera camera;

    private View layoutCapturePreview, cameraControls;
    private ImageView ivCapturedPreview;
    private Uri savedUri;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        initViews();
        
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        applyGuideAnimation();
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        layoutCapturePreview = findViewById(R.id.layoutCapturePreview);
        cameraControls = findViewById(R.id.cameraControls);
        ivCapturedPreview = findViewById(R.id.ivCapturedPreview);

        findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());
        findViewById(R.id.btnFlash).setOnClickListener(v -> toggleFlash());
        findViewById(R.id.btnSwitchCamera).setOnClickListener(v -> switchCamera());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnRetake).setOnClickListener(v -> {
            layoutCapturePreview.setVisibility(View.GONE);
            cameraControls.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnUsePhoto).setOnClickListener(v -> {
            if (savedUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.setData(savedUri);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setFlashMode(flashMode)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OptiSight");
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        savedUri = outputFileResults.getSavedUri();
                        showPreview(savedUri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
    }

    private void showPreview(Uri uri) {
        ivCapturedPreview.setImageURI(uri);
        layoutCapturePreview.setVisibility(View.VISIBLE);
        cameraControls.setVisibility(View.GONE);
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        layoutCapturePreview.startAnimation(fadeIn);
    }

    private void toggleFlash() {
        flashMode = (flashMode == ImageCapture.FLASH_MODE_OFF) ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF;
        imageCapture.setFlashMode(flashMode);
        
        ImageButton btnFlash = findViewById(R.id.btnFlash);
        btnFlash.setImageResource(flashMode == ImageCapture.FLASH_MODE_ON ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        Toast.makeText(this, flashMode == ImageCapture.FLASH_MODE_ON ? R.string.flash_on : R.string.flash_off, Toast.LENGTH_SHORT).show();
    }

    private void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }

    private void applyGuideAnimation() {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        findViewById(R.id.viewGuide).startAnimation(pulse);
        
        View scanLine = findViewById(R.id.viewScanningLine);
        float distance = 250 * getResources().getDisplayMetrics().density;
        android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(scanLine, "translationY", 0f, distance);
        animator.setDuration(2000);
        animator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        animator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        animator.start();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}

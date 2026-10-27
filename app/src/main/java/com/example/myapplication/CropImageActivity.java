package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropImageActivity extends AppCompatActivity {

    private ImageView ivToCrop;
    private View vCropOverlay;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // States
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Uri sourceUri;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        ivToCrop = findViewById(R.id.ivToCrop);
        vCropOverlay = findViewById(R.id.vCropOverlay);
        MaterialButton btnRotate = findViewById(R.id.btnRotate);
        MaterialButton btnSaveCrop = findViewById(R.id.btnSaveCrop);

        sourceUri = getIntent().getParcelableExtra("imageUri");
        if (sourceUri != null) {
            ivToCrop.setImageURI(sourceUri);
            ivToCrop.post(() -> {
                // Center the image initially
                float viewWidth = ivToCrop.getWidth();
                float viewHeight = ivToCrop.getHeight();
                float drawableWidth = ivToCrop.getDrawable().getIntrinsicWidth();
                float drawableHeight = ivToCrop.getDrawable().getIntrinsicHeight();

                float scale = Math.min(viewWidth / drawableWidth, viewHeight / drawableHeight);
                matrix.setScale(scale, scale);
                matrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeight * scale) / 2f);
                ivToCrop.setImageMatrix(matrix);
            });
        }

        ivToCrop.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }
            ivToCrop.setImageMatrix(matrix);
            return true;
        });

        btnRotate.setOnClickListener(v -> {
            matrix.postRotate(90, ivToCrop.getWidth() / 2f, ivToCrop.getHeight() / 2f);
            ivToCrop.setImageMatrix(matrix);
        });

        btnSaveCrop.setOnClickListener(v -> saveCroppedImage());

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void saveCroppedImage() {
        try {
            // Create a bitmap of the current view content
            Bitmap viewBitmap = Bitmap.createBitmap(ivToCrop.getWidth(), ivToCrop.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas viewCanvas = new Canvas(viewBitmap);
            ivToCrop.draw(viewCanvas);
            
            // Create a circular crop from the view
            Bitmap output = Bitmap.createBitmap(viewBitmap.getWidth(), viewBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            
            // Circle center and radius (center of the screen/view)
            float cx = viewBitmap.getWidth() / 2f;
            float cy = viewBitmap.getHeight() / 2f;
            float radius = Math.min(cx, cy) * 0.8f; // Use 80% of view as crop area
            
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(0xff424242);
            canvas.drawCircle(cx, cy, radius, paint);
            
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(viewBitmap, 0, 0, paint);
            
            // Now crop to the circle bounds
            int size = (int)(radius * 2);
            Bitmap finalCrop = Bitmap.createBitmap(output, 
                    (int)(cx - radius), (int)(cy - radius), 
                    size, size);

            File file = new File(getExternalFilesDir(null), "profile_cropped.png");
            FileOutputStream out = new FileOutputStream(file);
            finalCrop.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Intent intent = new Intent();
            intent.putExtra("croppedUri", Uri.fromFile(file).toString());
            setResult(RESULT_OK, intent);
            finish();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}

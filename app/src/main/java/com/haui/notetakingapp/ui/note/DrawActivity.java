package com.haui.notetakingapp.ui.note;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.FileManager;
import com.haui.notetakingapp.ui.base.BaseActivity;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class DrawActivity extends BaseActivity {
    public static final String EXTRA_DRAWING_URI = "extra_drawing_uri";
    private FloatingActionButton fabColorPicker, fabUndo;
    private SignaturePad signaturePad;
    private ImageButton btnBack, btnSave;
    private String selectedColor = "#000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.draw_screen);

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0);
            return insets;
        });

        myMapping();
        setupDrawingButtons();
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnSave.setOnClickListener(v -> saveDrawingAndFinish());
    }

    private void myMapping() {
        signaturePad = findViewById(R.id.signature_pad);
        fabColorPicker = findViewById(R.id.fb_colour_picker);
        fabUndo = findViewById(R.id.fb_undo);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupDrawingButtons() {
        // Chức năng chọn màu
        fabColorPicker.setOnClickListener(view -> {
            ArrayList<String> colors = new ArrayList<>();
            colors.add("#000000"); // Màu đen
            colors.add("#f84c44"); // Màu đỏ
            colors.add("#4CAF50"); // Màu xanh lá cây
            colors.add("#2196F3"); // Màu xanh dương
            colors.add("#FFC107"); // Màu vàng

            ColorPicker colorPicker = new ColorPicker(DrawActivity.this);
            colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                        @Override
                        public void setOnFastChooseColorListener(int position, int color) {
                            selectedColor = colors.get(position);
                            signaturePad.setPenColor(Color.parseColor(selectedColor));
                        }

                        @Override
                        public void onCancel() {

                        }
                    })
                    .setColors(colors)
                    .setDefaultColorButton(Color.parseColor(selectedColor))
                    .setColumns(5)
                    .setRoundColorButton(true)
                    .setTitle("Chọn màu")
                    .show();
        });

        fabUndo.setOnClickListener(view -> {
            if (signaturePad.isEmpty()) {
                Snackbar.make(view, "Không có gì để hoàn tác", Snackbar.LENGTH_SHORT).show();
            } else {
                signaturePad.clear();
                Snackbar.make(view, "Đã hoàn tác", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDrawingAndFinish() {
        if (signaturePad.isEmpty()) {
            Toast.makeText(this, "Vui lòng vẽ trước khi lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = signaturePad.getSignatureBitmap();
        Uri drawingUri = FileManager.saveDrawing(this, bitmap);
        if (drawingUri != null) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DRAWING_URI, drawingUri.toString());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu hình ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}

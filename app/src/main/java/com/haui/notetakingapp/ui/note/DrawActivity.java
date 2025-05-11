package com.haui.notetakingapp.ui.note;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.haui.notetakingapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class DrawActivity extends AppCompatActivity {
    private FloatingActionButton fbColourPicker, fbUndo;
    private SignaturePad signaturePad;
    private ImageView arrowLeft;
    private ImageButton btnSave;


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
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupDrawingButtons();
        btnSave.setOnClickListener(v -> saveDrawingAndFinish());
    }

    private void myMapping() {
        signaturePad = findViewById(R.id.signature_pad);
        fbColourPicker = findViewById(R.id.fb_colour_picker);
        fbUndo = findViewById(R.id.fb_undo);
        arrowLeft = findViewById(R.id.arrow_back);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupDrawingButtons() {
        // Chức năng chọn màu
        fbColourPicker.setOnClickListener(view -> {
            ArrayList<String> colors = new ArrayList<>();
            colors.add("#f84c44"); // Màu đỏ
            colors.add("#4CAF50"); // Màu xanh lá cây
            colors.add("#2196F3"); // Màu xanh dương
            colors.add("#FFC107"); // Màu vàng
            colors.add("#9C27B0"); // Màu tím

            ColorPicker colorPicker = new ColorPicker(DrawActivity.this);
            colorPicker.setColors(colors)
                    .setDefaultColorButton(Color.parseColor("#f84c44"))
                    .setColumns(5)
                    .setRoundColorButton(true)
                    .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                        @Override
                        public void onChooseColor(int position, int color) {
                            signaturePad.setPenColor(color); // Đặt màu bút vẽ
                        }

                        @Override
                        public void onCancel() {
                            // Xử lý khi người dùng hủy chọn màu
                        }
                    })
                    .show();
        });


        fbUndo.setOnClickListener(view -> {
            signaturePad.clear(); // Xóa tất cả nét vẽ
            Snackbar.make(view, "Hoàn tác thành công", Snackbar.LENGTH_SHORT)
                    .setAction("Làm lại", v -> signaturePad.clear())
                    .show();
        });
    }

    private void saveDrawingAndFinish() {
        Bitmap bitmap = signaturePad.getSignatureBitmap();
        File file = saveBitmapToFile(bitmap);

        if (file != null) {
            // Đảm bảo authority khớp với trong AndroidManifest.xml
            Uri uri = FileProvider.getUriForFile(this, "com.haui.notetakingapp.provider", file);
            Intent resultIntent = new Intent();
            resultIntent.setData(uri);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish(); // Return to the previous activity
    }


    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            // Lấy thư mục "drawings" trong thư mục "external files" của ứng dụng
            File directory = new File(getExternalFilesDir(null), "drawings");

            // Kiểm tra nếu thư mục chưa tồn tại, tạo thư mục mới
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    System.out.println("Thư mục drawings đã được tạo.");
                } else {
                    System.out.println("Không thể tạo thư mục drawings.");
                }
            }

            // Tạo file trong thư mục drawings với tên hình ảnh theo thời gian
            File file = new File(directory, "drawing_" + System.currentTimeMillis() + ".png");

            // Lưu hình ảnh vào file
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            return file; // Trả về file đã lưu
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi khi lưu tệp
        }
    }


}

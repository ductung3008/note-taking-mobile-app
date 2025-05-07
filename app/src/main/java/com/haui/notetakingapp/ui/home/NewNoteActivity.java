package com.haui.notetakingapp.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.NoteDatabase;
import com.haui.notetakingapp.data.local.entity.Note;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NewNoteActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView audioPreview;
    private Button playButton;
    private EditText editTitle, edtBelowImage;
    private ImageButton btnChecklist, btnSave, btnImage, btnRecord;
    private LinearLayout checklistContainer;
    private Uri imageUri;


    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private boolean isRecording = false;
    private String audioFilePath;
    private static final int REQUEST_RECORD_AUDIO = 102;
    private int checklistCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_note);

        // Ẩn/hiện nút khi bàn phím mở
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        myMapping();
        setupImagePickers();
        audioFilePath = null;
        setupButtonActions();
    }
    private void myMapping() {
        imageView = findViewById(R.id.imageView);
        audioPreview = findViewById(R.id.audioPreview);
        playButton = findViewById(R.id.playButton);
        editTitle = findViewById(R.id.editTitle);
        edtBelowImage = findViewById(R.id.edtBelowImage);
        btnChecklist = findViewById(R.id.btnChecklist);
        btnSave = findViewById(R.id.btnSave);
        btnImage = findViewById(R.id.btnImage);
        btnRecord = findViewById(R.id.btnRecord);
        checklistContainer = findViewById(R.id.checklistContainer);
    }
    private void setupImagePickers() {
        // Khởi tạo launcher chụp ảnh
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && imageUri != null) {
                        imageView.setImageURI(imageUri);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
        // Khởi tạo launcher chọn ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imageView.setImageURI(imageUri);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void setupButtonActions() {
        btnImage.setOnClickListener(v -> showImagePopup());
        btnRecord.setOnClickListener(v -> handleRecording());
        playButton.setOnClickListener(v -> playAudio());
        btnChecklist.setOnClickListener(v -> addChecklistItem());
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void showImagePopup() {
        PopupMenu popup = new PopupMenu(this, btnImage);
        popup.getMenuInflater().inflate(R.menu.menu_image_button, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_gallery) {
                pickImageLauncher.launch("image/*");
                return true;
            } else if (item.getItemId() == R.id.menu_camera) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, 101);
                } else {
                    openCamera();
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void handleRecording() {
        if (isRecording) {
            stopRecording();
            Toast.makeText(this, "Đã dừng ghi âm", Toast.LENGTH_SHORT).show();
            btnRecord.setImageResource(R.drawable.microphone); // đổi icon lại nếu cần
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            } else {
                startRecording();
                Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
                // btnRecord.setImageResource(R.drawable.microphone_slash); // đổi icon nếu muốn
            }
        }
    }
    private void playAudio() {
        if (audioFilePath == null) {
            Toast.makeText(this, "Không có file ghi âm để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "Đã dừng phát lại", Toast.LENGTH_SHORT).show();
        } else {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Đang phát...", Toast.LENGTH_SHORT).show();

                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                    Toast.makeText(this, "Phát xong", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể phát ghi âm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm mở camera
    private void openCamera() {
        try {
            File photoFile = new File(getExternalFilesDir(null),
                    "note_photo_" + System.currentTimeMillis() + ".jpg");
            imageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(imageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    // Kết quả sau khi xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
//        else {
//            Toast.makeText(this, "Bạn cần cấp quyền camera", Toast.LENGTH_SHORT).show();
//        }
        else if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để tiếp tục", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm ghi âm
    private void startRecording() {
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir != null) {
//            File outputFile = new File(outputDir, "note_audio_" + System.currentTimeMillis() + ".3gp");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File outputFile = new File(outputDir, "audio_note_" + timeStamp + ".3gp");

            audioFilePath = outputFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();

                Button playButton = findViewById(R.id.playButton);
                playButton.setText(outputFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể bắt đầu ghi âm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm dừng ghi âm
    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            Toast.makeText(this, "Ghi âm đã lưu", Toast.LENGTH_SHORT).show();

            // Hiện giao diện phát lại
            if (audioPreview != null) {
                audioPreview.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // Giải phóng tài nguyên ghi âm
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) stopRecording();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void addChecklistItem() {
        // Tạo layout ngang chứa checkbox và EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Tạo checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());

        // Tạo EditText
        EditText editText = new EditText(this);
        editText.setHint("Nhập mục checklist...");
        editText.setBackground(null);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setTextSize(16);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSingleLine(true);

        // Xử lý nhấn Enter để thêm checklist mới
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addChecklistItem();
                return true;
            }
            return false;
        });

        // Xử lý nhấn phím xóa (Backspace) khi ô đang trống
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    editText.getText().toString().isEmpty()) {

                // Xoá layout chứa checkbox + EditText khỏi container
                checklistContainer.removeView(layout);


                return true;
            }
            return false;
        });

        // Thêm checkbox và EditText vào layout
        layout.addView(checkBox);
        layout.addView(editText);

        // Thêm layout vào checklistContainer
        checklistContainer.addView(layout);

        // Focus vào ô mới tạo
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void saveNote() {
        String title = editTitle.getText().toString().trim();
        String content = edtBelowImage.getText().toString().trim();

        // Kiểm tra nếu tiêu đề hoặc nội dung trống
        if (title.isEmpty() || content.isEmpty()) {
            // Hiển thị thông báo nếu tiêu đề hoặc nội dung trống
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return; // Dừng hàm nếu có lỗi
        }

        // Lấy đường dẫn ảnh và âm thanh đã chọn
        List<String> imagePaths = getSelectedImagePaths();
        List<String> audioPaths = getRecordedAudioPaths();
        for (String path : imagePaths) {
            Log.d("ImagePaths", "Đường dẫn ảnh: " + path);
        }
        Log.d("ImagePaths", "Danh sách ảnh: " + imagePaths.toString());

        // Tạo một ghi chú mới
        Note note = new Note(title, content);
        note.setImagePaths(imagePaths);
        note.setAudioPaths(audioPaths);
        note.setUpdatedAt(System.currentTimeMillis());

//        // Gửi kết quả về HomeActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("objNewNote", note);
        audioFilePath = null;
        imageUri = null;
        setResult(RESULT_OK, resultIntent);
//        finish();

        // Quay lại HomeActivity
        finish();
    }



    private List<String> getSelectedImagePaths() {
        List<String> imagePaths = new ArrayList<>();

        // Nếu imageUri đã được thiết lập, tức là có ảnh đã được chọn hoặc chụp
        if (imageUri != null) {
            // Chuyển đổi URI thành đường dẫn file
            String imagePath = imageUri.getPath();
            Log.d("ImagePaths", "Danh sách ảnh: " + imageUri);
            if (imagePath != null) {
                imagePaths.add(imagePath); // Thêm đường dẫn ảnh vào danh sách
            }
        }

        // Trả về danh sách các đường dẫn ảnh
        return imagePaths;
    }

    private List<String> getRecordedAudioPaths() {
        List<String> audioPaths = new ArrayList<>();

        // Nếu có file ghi âm đã được lưu, thêm vào danh sách
        if (audioFilePath != null && new File(audioFilePath).exists()) {
            audioPaths.add(audioFilePath); // Thêm đường dẫn file âm thanh vào danh sách
            Log.d("audioFilePath", "Danh sách audioFilePath: " + audioFilePath);
        }

        // Trả về danh sách các đường dẫn âm thanh
        return audioPaths;
    }
}




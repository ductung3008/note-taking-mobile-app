package com.haui.notetakingapp.ui.note;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.viewmodel.EditNoteViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 102;
    private final int checklistCount = 0;
    private TextView audioPreview;
    private Button playButton;
    private EditText editTitle, edtContent;
    private ImageButton btnChecklist, btnSave, btnImage, btnRecord;
    private LinearLayout imageContainer, checklistContainer;
    private Uri imageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String[]> pickImageLauncher;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String audioPath;
    private EditNoteViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_note);

        viewModel = new ViewModelProvider(this).get(EditNoteViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        observeViewModel();

        // Ẩn/hiện nút khi bàn phím mở
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        myMapping();
        myDisplay();
        setupImagePickers();
        audioPath = null;
        setupButtonActions();
    }

    private void observeViewModel() {
        // Observe save success
        viewModel.saveSuccess.observe(this, success -> {
            if (success) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // Observe error messages
        viewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void myMapping() {
        audioPreview = findViewById(R.id.audioPreview);
        imageContainer = findViewById(R.id.imageContainer);
        playButton = findViewById(R.id.playButton);
        editTitle = findViewById(R.id.editTitle);
        edtContent = findViewById(R.id.edtContent);
        btnChecklist = findViewById(R.id.btnChecklist);
        btnSave = findViewById(R.id.btnSave);
        btnImage = findViewById(R.id.btnImage);
        btnRecord = findViewById(R.id.btnRecord);
        checklistContainer = findViewById(R.id.checklistContainer);
    }

    private void setupButtonActions() {
        btnImage.setOnClickListener(v -> showImagePopup());
        btnRecord.setOnClickListener(v -> handleRecording());
        playButton.setOnClickListener(v -> playAudio());
        btnChecklist.setOnClickListener(v -> addChecklistItem());
        btnSave.setOnClickListener(v -> updateNote());
    }

    private void addImageToView(Uri imageUri) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        image.setLayoutParams(params);

        imageContainer.addView(image);

        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .override(Target.SIZE_ORIGINAL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int width = resource.getWidth() / 2;
                        int height = resource.getHeight() / 2;
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, width, height, true);
                        image.getLayoutParams().width = width;
                        image.getLayoutParams().height = height;
                        image.setImageBitmap(resizedBitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void myDisplay() {
        Note note = (Note) getIntent().getSerializableExtra("noteToEdit");

        if (note != null) {
            // Pass the note to the ViewModel
            viewModel.setCurrentNote(note);

            // Set UI elements
            editTitle.setText(note.getTitle());
            edtContent.setText(note.getContent());

            if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
                for (String imageUri : note.getImagePaths()) {
                    addImageToView(Uri.parse(imageUri));
                }

                viewModel.addImagePath(imageUri);
            }

            // Handle audio if exists
            if (note.getAudioPaths() != null && !note.getAudioPaths().isEmpty()) {
                audioPath = note.getAudioPaths().get(0);
                viewModel.addAudioPath(audioPath);

                // Show audio controls if audio exists
                audioPreview.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
                playButton.setText(new File(audioPath).getName());
            }
        }
    }

    private void setupImagePickers() {
        // Khởi tạo launcher chụp ảnh
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            addImageToView(imageUri);
            viewModel.addImagePath(imageUri);
        });

        // Khởi tạo launcher chọn ảnh
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                for (Uri uri : uris) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    addImageToView(uri);
                }
                viewModel.addImagePaths(uris);
            }
        });
    }

    private void showImagePopup() {
        PopupMenu popup = new PopupMenu(this, btnImage);
        popup.getMenuInflater().inflate(R.menu.menu_image_button, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_gallery) {
                pickImageLauncher.launch(new String[]{"image/*"});
                return true;
            } else if (item.getItemId() == R.id.menu_camera) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            } else {
                startRecording();
                Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
                // btnRecord.setImageResource(R.drawable.microphone_slash); // đổi icon nếu muốn
            }
        }
    }

    private void playAudio() {
        if (audioPath == null) {
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
                mediaPlayer.setDataSource(audioPath);
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
            File photoFile = new File(getExternalFilesDir(null), "note_photo_" + System.currentTimeMillis() + ".jpg");
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(imageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    // Kết quả sau khi xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để tiếp tục", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm ghi âm
    private void startRecording() {
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File outputFile = new File(outputDir, "audio_note_" + timeStamp + ".3gp");

            audioPath = outputFile.getAbsolutePath();
            viewModel.addAudioPath(audioPath);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioPath);
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
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Tạo checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());

        // Tạo EditText
        EditText editText = new EditText(this);
        editText.setHint("Nhập mục checklist...");
        editText.setBackground(null);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setTextSize(16);
        editText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
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
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && editText.getText().toString().isEmpty()) {
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

    private void updateNote() {
        String title = editTitle.getText().toString();
        String content = edtContent.getText().toString();

        viewModel.setTitle(title);
        viewModel.setContent(content);

        viewModel.updateNote();
    }
}

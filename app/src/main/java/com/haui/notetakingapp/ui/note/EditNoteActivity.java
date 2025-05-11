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
import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.viewmodel.EditNoteViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 102;
    private final int checklistCount = 0;
    private ImageView imageView;
    private TextView audioPreview;
    private Button playButton;
    private EditText editTitle, edtContent;
    private ImageButton btnChecklist, btnSave, btnImage, btnRecord;
    private LinearLayout imageContainer, checklistContainer, audioContainer;
    private Uri imageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private List<Uri> recordedAudioPaths = new ArrayList<>();
    private ActivityResultLauncher<String[]> pickImageLauncher;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String audioFilePath;
    private EditNoteViewModel viewModel;
    private List<String> existingAudioPaths = new ArrayList<>(); // Paths from the original note
    private List<String> newAudioPaths = new ArrayList<>(); // Newly recorded paths in this edit session

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
        audioFilePath = null;
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
//        audioPreview = findViewById(R.id.audioPreview);
        playButton = findViewById(R.id.playButton);
        editTitle = findViewById(R.id.editTitle);
        edtContent = findViewById(R.id.edtContent);
        btnChecklist = findViewById(R.id.btnChecklist);
        btnSave = findViewById(R.id.btnSave);
        btnImage = findViewById(R.id.btnImage);
        btnRecord = findViewById(R.id.btnRecord);
        checklistContainer = findViewById(R.id.checklistContainer);
        imageContainer = findViewById(R.id.imageContainer);
//        imageContainerDraw = findViewById(R.id.imageContainerDraw);
//        arrowLeft = findViewById(R.id.arrow_left);
//        btnDraw = findViewById(R.id.btnDraw);
        audioContainer = findViewById(R.id.audioContainer);
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

            // Handle images if they exist
            if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
                for (String imagePath : note.getImagePaths()) {
                    addImageToView(Uri.parse(imagePath));
                }
            }

            // Handle audio if exists
            if (note.getAudioPaths() != null && !note.getAudioPaths().isEmpty()) {
                // Store existing audio paths
                existingAudioPaths.addAll(note.getAudioPaths());

                // Display each audio item in UI
                for (String audioPath : note.getAudioPaths()) {
                    addAudioItemToUI(audioPath);
                }
            }

            // Handle checklist items if they exist
            if (note.getChecklistItems() != null && !note.getChecklistItems().isEmpty()) {
                for (CheckListItem item : note.getChecklistItems()) {
                    addExistingChecklistItem(item.getText(), item.isChecked());
                }
            }
        }
    }
    // Add this method to handle existing checklist items
    private void addExistingChecklistItem(String text, boolean isChecked) {
        // Create layout ngang chứa checkbox và EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Tạo checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());
        checkBox.setChecked(isChecked); // Set the checked state

        // Tạo EditText
        EditText editText = new EditText(this);
        editText.setText(text); // Set the existing text
        editText.setBackground(null);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setTextSize(16);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
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

        // Set listeners to track changes
        checkBox.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Update your model or store the state change
            // This depends on how you're tracking checklist changes
        });

        // Thêm checkbox và EditText vào layout
        layout.addView(checkBox);
        layout.addView(editText);

        // Thêm layout vào checklistContainer
        checklistContainer.addView(layout);
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
        if (isRecording) {
            stopRecording();
            return;
        }

        Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show();
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File outputFile = new File(outputDir, "audio_note_" + timeStamp + ".3gp");

            audioFilePath = outputFile.getAbsolutePath();

            // Add the new path to our list (convert File path to Uri)
            Uri audioUri = Uri.fromFile(outputFile);
            recordedAudioPaths.add(audioUri);

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

            // Track the new recording path
            if (audioFilePath != null) {
                newAudioPaths.add(audioFilePath);

                // Add the recording to the UI
                addAudioItemToUI(audioFilePath);
            }
        }
    }


    private void addAudioItemToUI(String audioPath) {
        if (audioPath == null) return;

        // Create a horizontal layout for each audio item
        LinearLayout audioItemLayout = new LinearLayout(this);
        audioItemLayout.setOrientation(LinearLayout.HORIZONTAL);
        audioItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        audioItemLayout.setPadding(8, 8, 8, 8);

        // Create a text view to show the file name
        TextView audioFileName = new TextView(this);
        File audioFile = new File(audioPath);
        audioFileName.setText(audioFile.getName());
        audioFileName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Create a play button for this audio item
        Button playAudioButton = new Button(this);
        playAudioButton.setText("Play");
        playAudioButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Set an ID for the path to be used in the click listener
        playAudioButton.setTag(audioPath);

        // Set click listener for playing this specific audio
        playAudioButton.setOnClickListener(v -> {
            String path = (String) v.getTag();
            playSpecificAudio(path);
        });

        // Add views to the layout
        audioItemLayout.addView(audioFileName);
        audioItemLayout.addView(playAudioButton);

        // Add the layout to the audio container
        audioContainer.addView(audioItemLayout);

        // Make the container visible if it was hidden
        audioContainer.setVisibility(View.VISIBLE);
    }

    private void playSpecificAudio(String audioPath) {
        if (audioPath == null) {
            Toast.makeText(this, "Không có file ghi âm để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop any currently playing audio
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Create and start a new media player
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

    private List<CheckListItem> getChecklistItemsFromUI() {
        List<CheckListItem> checklistItems = new ArrayList<>();
        for (int i = 0; i < checklistContainer.getChildCount(); i++) {
            View itemLayout = checklistContainer.getChildAt(i);
            if (itemLayout instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) itemLayout;
                CheckBox checkBox = null;
                EditText editText = null;

                for (int j = 0; j < layout.getChildCount(); j++) {
                    View child = layout.getChildAt(j);
                    if (child instanceof CheckBox) {
                        checkBox = (CheckBox) child;
                    } else if (child instanceof EditText) {
                        editText = (EditText) child;
                    }
                }

                if (editText != null && checkBox != null) {
                    String text = editText.getText().toString().trim();
                    boolean isChecked = checkBox.isChecked();
                    if (!text.isEmpty()) {
                        checklistItems.add(new CheckListItem(text, isChecked));
                    }
                }
            }
        }
        return checklistItems;
    }

    private void updateNote() {
        String title = editTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        viewModel.setTitle(title);
        viewModel.setContent(content);

        // Save checklist items from UI
        viewModel.setChecklistItems(getChecklistItemsFromUI());

        // Clear existing audio paths and set them correctly
        viewModel.clearAudioPaths();

        // Add all existing audio paths that weren't deleted
        // (You'll need to track deletions if that's a feature)
        for (String path : existingAudioPaths) {
            viewModel.addAudioPath(path);
        }

        // Add all new audio paths
        for (String path : newAudioPaths) {
            viewModel.addAudioPath(path);
        }

        // Lưu ghi chú
        viewModel.updateNote();
    }
}

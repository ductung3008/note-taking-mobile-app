package com.haui.notetakingapp.ui.note;

import android.Manifest;
import android.app.Activity;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.viewmodel.NewNoteViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewNoteActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 102;
    private ImageView imageView, arrowLeft;
    private TextView audioPreview;
    private Button playButton;
    private EditText editTitle, edtBelowImage;
    private ImageButton btnChecklist, btnSave, btnImage, btnRecord, btnDraw;
    private LinearLayout checklistContainer, imageContainer, imageContainerDraw;
    private Uri imageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> drawScreenLauncher;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String audioFilePath;
    private final int checklistCount = 0;

    // ViewModel reference
    private NewNoteViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_note);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(NewNoteViewModel.class);

        // Observe ViewModel LiveData
        observeViewModel();

        // Ẩn/hiện nút khi bàn phím mở
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        myMapping();
        setupDraw();
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
        imageContainer = findViewById(R.id.imageContainer);
        imageContainerDraw = findViewById(R.id.imageContainerDraw);
        arrowLeft = findViewById(R.id.arrow_left);
        btnDraw = findViewById(R.id.btnDraw);
    }

    private void setupImagePickers() {
        // Khởi tạo launcher chụp ảnh
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && imageUri != null) {
                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE);
                viewModel.setImageUri(imageUri);
            }
        });
        // Khởi tạo launcher chọn ảnh
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                imageContainer.removeAllViews(); // Xóa ảnh cũ nếu có
                for (Uri uri : uris) {
                    ImageView image = new ImageView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 600);
                    params.setMargins(0, 8, 0, 8);
                    image.setLayoutParams(params);
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    image.setImageURI(uri);
                    imageContainer.addView(image);
                    viewModel.setImageUri(uri);
                }
            }
        });
    }

    private void setupButtonActions() {
        btnImage.setOnClickListener(v -> showImagePopup());
        btnRecord.setOnClickListener(v -> handleRecording());
        playButton.setOnClickListener(v -> playAudio());
        btnChecklist.setOnClickListener(v -> addChecklistItem());
        btnSave.setOnClickListener(v -> saveNote());
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnDraw.setOnClickListener( view -> {
            Intent intent = new Intent(NewNoteActivity.this, DrawActivity.class);
            drawScreenLauncher.launch(intent);

        });

    }

    private void showImagePopup() {
        PopupMenu popup = new PopupMenu(this, btnImage);
        popup.getMenuInflater().inflate(R.menu.menu_image_button, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_gallery) {
                pickImageLauncher.launch("image/*");
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
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File outputFile = new File(outputDir, "audio_note_" + timeStamp + ".3gp");

            audioFilePath = outputFile.getAbsolutePath();
            viewModel.setAudioFilePath(audioFilePath);

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

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String content = editText.getText().toString();
            CheckListItem item = new CheckListItem(content, isChecked);
            // Thêm checklist item vào danh sách
            viewModel.addChecklistItem(item);
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


    private void saveNote() {
        // Update ViewModel with latest values from UI
        viewModel.setTitle(editTitle.getText().toString().trim());
        viewModel.setContent(edtBelowImage.getText().toString().trim());
        List<CheckListItem> checklist = getChecklistItemsFromUI();
        for (int i = 0; i < checklist.size(); i++) {
            CheckListItem item = checklist.get(i);
            Log.d("ChecklistDebug", "Item " + i + ": " + item.getText() + " | Checked: " + item.isChecked());
        }
        viewModel.setChecklistItems(checklist);
        List<Uri> drawImages = getDrawImagesFromUI();
        viewModel.setDrawImages(drawImages);
        // Ask ViewModel to save the note
        viewModel.saveNote();
    }

    private void setupDraw() {
        drawScreenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Xử lý kết quả trả về từ DrawActivity ở đây
                        Uri imageUri = result.getData().getData();


                        // Hiển thị ảnh vẽ vào ImageView trong container
                        ImageView imageView = new ImageView(this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));  // Điều chỉnh kích thước ảnh
                        imageView.setImageURI(imageUri);

                        // Lưu URI của ảnh vào tag của ImageView
                        imageView.setTag(imageUri);

                        // Thêm ImageView vào container (imageContainerDraw)
                        imageContainerDraw.addView(imageView);
                    }
                }
        );
    }

    private List<Uri> getDrawImagesFromUI() {
        List<Uri> drawImages = new ArrayList<>();

        // Lấy các ảnh từ ImageView hoặc Container chứa ảnh vẽ
        for (int i = 0; i < imageContainerDraw.getChildCount(); i++) {
            View childView = imageContainerDraw.getChildAt(i);
            if (childView instanceof ImageView) {
                ImageView imageView = (ImageView) childView;
                // Lấy URI của ảnh từ ImageView (bạn có thể điều chỉnh cách lấy URI nếu cần)
                Uri imageUri = (Uri) imageView.getTag();  // Giả sử bạn lưu URI trong tag của ImageView
                drawImages.add(imageUri);
            }
        }

        return drawImages;
    }

}

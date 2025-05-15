package com.haui.notetakingapp.ui.note.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.FileManager;
import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.ui.base.BaseActivity;
import com.haui.notetakingapp.ui.note.DrawActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * BaseNoteActivity là lớp trừu tượng chứa các chức năng và UI chung
 * cho cả NewNoteActivity và EditNoteActivity
 */
public abstract class BaseNoteActivity extends BaseActivity {
    protected static final int REQUEST_RECORD_AUDIO = 102;
    protected final int checklistCount = 0;

    protected Button playButton;
    protected EditText edtTitle, edtContent;
    protected ImageButton btnChecklist, btnBack, btnSave, btnImage, btnRecord, btnDraw;
    protected LinearLayout imageContainer, checklistContainer, audioContainer, drawingContainer;

    protected Uri imageUri;
    protected ActivityResultLauncher<Uri> takePictureLauncher;
    protected ActivityResultLauncher<String[]> pickImageLauncher;
    protected ActivityResultLauncher<Intent> drawScreenLauncher;
    protected MediaRecorder mediaRecorder;
    protected MediaPlayer mediaPlayer;
    protected boolean isRecording = false;
    protected String audioFilePath;
    protected List<Uri> recordedAudioPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    protected void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected abstract void observeViewModel();

    protected abstract void bindView();

    protected void setupButtonListener() {
        btnImage.setOnClickListener(v -> showImagePopup());
        btnRecord.setOnClickListener(v -> handleRecording());
        playButton.setOnClickListener(v -> playAudio());
        btnChecklist.setOnClickListener(v -> addChecklistItem());
        btnDraw.setOnClickListener(view -> {
            Intent intent = new Intent(this, DrawActivity.class);
            drawScreenLauncher.launch(intent);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    protected void addImageToView(Uri imageUri, LinearLayout container) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        image.setLayoutParams(params);

        // Set image tag to store the URI for later reference
        image.setTag(imageUri);

        // Add long press listener to show delete option
        image.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_media_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_delete) {
                    // Remove image from view
                    container.removeView(image);
                    // Handle image deletion from ViewModel - this will be implemented in child classes
                    onImageDeleted((Uri) v.getTag());
                    return true;
                }
                return false;
            });
            // Try to show icons in popup menu
            try {
                Method method = popupMenu.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                method.setAccessible(true);
                method.invoke(popupMenu.getMenu(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            popupMenu.show();
            return true;
        });

        container.addView(image);

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

    protected void setupImagePickers() {
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                addImageToView(imageUri, imageContainer);
                onImageCaptured(imageUri);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                for (Uri uri : uris) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    addImageToView(uri, imageContainer);
                }
                onImagesSelected(uris);
            }
        });
    }

    protected abstract void onImageCaptured(Uri imageUri);

    protected abstract void onImagesSelected(List<Uri> imageUris);

    protected void openCamera() {
        try {
            imageUri = FileManager.createImageFileUri(this);
            takePictureLauncher.launch(imageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi mở camera: ", Toast.LENGTH_SHORT).show();
        }
    }

    protected void showImagePopup() {
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

    protected void handleRecording() {
        if (isRecording) {
            stopRecording();
            Toast.makeText(this, "Đã dừng ghi âm", Toast.LENGTH_SHORT).show();
            btnRecord.setImageResource(R.drawable.ic_microphone);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            } else {
                startRecording();
                Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void playAudio() {
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
                Toast.makeText(this, "Không thể phát ghi âm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void startRecording() {
        if (isRecording) {
            stopRecording();
            return;
        }

        try {
            File audioFile = FileManager.createAudioFile(this);
            audioFilePath = audioFile.getAbsolutePath();
            mediaRecorder = FileManager.prepareRecorder(audioFile);
            recordedAudioPaths.add(FileProvider.getUriForFile(this, getPackageName() + ".provider", audioFile));
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Không thể bắt đầu ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    protected void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Toast.makeText(this, "Ghi âm không thành công", Toast.LENGTH_SHORT).show();
                audioFilePath = null;
                return;
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            Toast.makeText(this, "Ghi âm đã lưu", Toast.LENGTH_SHORT).show();

            // Handle the recording
            if (audioFilePath != null) {
                Uri audioUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(audioFilePath));
                onAudioRecorded(audioUri);
                addAudioItemToUI(audioUri);
            }
        }
    }

    protected abstract void onAudioRecorded(Uri audioUri);

    protected void addAudioItemToUI(Uri audioUri) {
        if (audioUri == null) return;

        // Create a horizontal layout for each audio item with improved styling
        LinearLayout audioItemLayout = new LinearLayout(this);
        audioItemLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, 4, 0, 4);
        audioItemLayout.setLayoutParams(itemParams);
        audioItemLayout.setPadding(16, 12, 16, 12);
        audioItemLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Set a tag to store the audio path for reference
        audioItemLayout.setTag(audioUri);

        // Add long press listener to show delete option
        audioItemLayout.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_media_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_delete) {
                    audioContainer.removeView(v);
                    onAudioDeleted(audioUri.toString());
                    return true;
                }
                return false;
            });

            try {
                Method method = popupMenu.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                method.setAccessible(true);
                method.invoke(popupMenu.getMenu(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            popupMenu.show();
            return true;
        });

        // Create a TextView to display the audio file name
        TextView audioFileName = new TextView(this);
        String audioName = "Recorded";
        try {
            File audioFile = new File(FileManager.getPathFromUri(this, audioUri));
            audioName = audioFile.getName();
        } catch (Exception ignored) {
        }
        audioFileName.setText(audioName);
        audioFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        audioFileName.setEllipsize(TextUtils.TruncateAt.END);
        audioFileName.setSingleLine(true);
        audioFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Improve the play button styling
        ImageButton playAudioButton = new ImageButton(this);
        playAudioButton.setImageResource(R.drawable.ic_play);
        int buttonSize = (int) (48 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonSize, buttonSize);
        buttonParams.setMarginStart(8);
        playAudioButton.setLayoutParams(buttonParams);
        playAudioButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        playAudioButton.setBackgroundResource(R.drawable.round_button_background);
        playAudioButton.setColorFilter(getResources().getColor(R.color.color_icon));
        playAudioButton.setPadding(12, 12, 12, 12);

        // Set click listener for playing this specific audio
        playAudioButton.setTag(audioUri);
        playAudioButton.setOnClickListener(v -> {
            Uri uri = (Uri) v.getTag();
            playSpecificAudio(uri);
        });

        // Add views to the layout
        audioItemLayout.addView(audioFileName);
        audioItemLayout.addView(playAudioButton);

        // Add the layout to the audio container
        audioContainer.addView(audioItemLayout);

        // Make the container visible if it was hidden
        audioContainer.setVisibility(View.VISIBLE);
    }

    protected void playSpecificAudio(Uri audioUri) {
        if (audioUri == null) {
            Toast.makeText(this, "Không có file ghi âm để phát", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Đang phát...", Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                Toast.makeText(this, "Phát xong", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            Toast.makeText(this, "Không thể phát ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) stopRecording();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    protected void addChecklistItem() {
        // Tạo layout ngang chứa checkbox và EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Tạo checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());

        // Tạo EditText
        EditText editText = new EditText(this);
        editText.setHint("Nhập mục checklist...");
        editText.setBackground(null);
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
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                    && editText.getText().toString().isEmpty()) {
                // Xoá layout chứa checkbox + EditText khỏi container
                checklistContainer.removeView(layout);
                return true;
            }
            return false;
        });

        // Handle checkbox state changes
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onChecklistItemChanged(editText.getText().toString(), isChecked);
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

    protected abstract void onChecklistItemChanged(String text, boolean isChecked);

    protected List<CheckListItem> getChecklistItemsFromUI() {
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

    protected void setupDraw() {
        drawScreenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String drawingUriString = result.getData().getStringExtra(DrawActivity.EXTRA_DRAWING_URI);
                        if (drawingUriString != null) {
                            Uri drawingUri = Uri.parse(drawingUriString);
                            addImageToView(drawingUri, drawingContainer);
                            onDrawingCreated(drawingUri);
                        }
                    }
                });
    }

    protected abstract void onDrawingCreated(Uri drawingUri);

    // Add this abstract method to handle image deletion in child classes
    protected abstract void onImageDeleted(Uri imageUri);

    // Add this abstract method to handle audio deletion in child classes
    protected abstract void onAudioDeleted(String uriString);
}

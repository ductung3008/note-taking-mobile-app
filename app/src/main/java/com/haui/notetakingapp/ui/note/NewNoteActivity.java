package com.haui.notetakingapp.ui.note;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.ui.note.base.BaseNoteActivity;
import com.haui.notetakingapp.viewmodel.NewNoteViewModel;

import java.util.List;

public class NewNoteActivity extends BaseNoteActivity {
    private NewNoteViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(NewNoteViewModel.class);

        // Observe ViewModel LiveData
        observeViewModel();

        // Setup window insets
        setupWindowInsets();

        // Initialize UI components
        bindView();
        setupDraw();
        setupImagePickers();
        audioFilePath = null;
        setupButtonListener();
    }

    @Override
    protected void observeViewModel() {
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

    @Override
    protected void bindView() {
        playButton = findViewById(R.id.playButton);
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnBack = findViewById(R.id.btnBack);
        btnChecklist = findViewById(R.id.btnChecklist);
        btnSave = findViewById(R.id.btnSave);
        btnImage = findViewById(R.id.btnImage);
        btnRecord = findViewById(R.id.btnRecord);
        checklistContainer = findViewById(R.id.checklistContainer);
        imageContainer = findViewById(R.id.imageContainer);
        drawingContainer = findViewById(R.id.drawingContainer);
        btnDraw = findViewById(R.id.btnDraw);
        audioContainer = findViewById(R.id.audioContainer);
    }

    @Override
    protected void setupButtonListener() {
        super.setupButtonListener();
        btnSave.setOnClickListener(v -> saveNote());
    }

    @Override
    protected void onImageCaptured(Uri imageUri) {
        viewModel.addImagePath(imageUri);
    }

    @Override
    protected void onImagesSelected(List<Uri> imageUris) {
        viewModel.addImagePaths(imageUris);
    }

    @Override
    protected void onAudioRecorded(String audioPath) {
    }

    @Override
    protected void onChecklistItemChanged(String text, boolean isChecked) {
        if (!text.isEmpty()) {
            CheckListItem item = new CheckListItem(text, isChecked);
            viewModel.addChecklistItem(item);
        }
    }

    @Override
    protected void onDrawingCreated(Uri drawingUri) {
        viewModel.addDrawingPath(drawingUri);
    }

    @Override
    protected void onImageDeleted(Uri imageUri) {
        viewModel.removeImagePath(imageUri);
    }

    @Override
    protected void onAudioDeleted(String audioPath) {
        // Remove from recorded paths list
        recordedAudioPaths.remove(Uri.parse(audioPath));
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        viewModel.setTitle(title);
        viewModel.setContent(content);

        viewModel.setChecklistItems(getChecklistItemsFromUI());

        // Pass the accumulated audio paths list
        if (!recordedAudioPaths.isEmpty()) {
            viewModel.addAudioPaths(recordedAudioPaths);
        }

        // Lưu ghi chú
        viewModel.saveNote();
    }
}

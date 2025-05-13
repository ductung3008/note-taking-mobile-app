package com.haui.notetakingapp.ui.note;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.note.base.BaseNoteActivity;
import com.haui.notetakingapp.viewmodel.EditNoteViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditNoteActivity extends BaseNoteActivity {
    private EditNoteViewModel viewModel;
    private List<String> existingAudioPaths = new ArrayList<>(); // Paths from the original note
    private List<String> newAudioPaths = new ArrayList<>(); // Newly recorded paths in this edit session

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_note);

        viewModel = new ViewModelProvider(this).get(EditNoteViewModel.class);

        // Configure window insets
        setupWindowInsets();

        // Initialize ViewModel and observe changes
        observeViewModel();

        // Initialize UI components
        bindView();
        myDisplay();
        setupImagePickers();
        setupDraw();
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
        btnSave.setOnClickListener(v -> updateNote());
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
        if (audioPath != null) {
            newAudioPaths.add(audioPath);
        }
    }

    @Override
    protected void onChecklistItemChanged(String text, boolean isChecked) {
        // We handle this when saving
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
        // Remove from existing paths if it's there
        existingAudioPaths.remove(audioPath);
        
        // If it's a newly recorded audio, remove from that list too
        newAudioPaths.remove(audioPath);
        
        // Remove from viewModel
        viewModel.removeAudioPath(audioPath);
    }

    private void myDisplay() {
        Note note = (Note) getIntent().getSerializableExtra("noteToEdit");

        if (note != null) {
            // Pass the note to the ViewModel
            viewModel.setCurrentNote(note);

            // Set UI elements
            edtTitle.setText(note.getTitle());
            edtContent.setText(note.getContent());

            // Handle images if they exist
            if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
                for (String imagePath : note.getImagePaths()) {
                    addImageToView(Uri.parse(imagePath), imageContainer);
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

            // Handle drawings if they exist
            if (note.getDrawingPaths() != null && !note.getDrawingPaths().isEmpty()) {
                for (String drawingPath : note.getDrawingPaths()) {
                    addImageToView(Uri.parse(drawingPath), drawingContainer);
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
        // Using the same method signature but calling the parent class implementation
        addChecklistItem();

        // Get the last added checklist item view
        int lastIndex = checklistContainer.getChildCount() - 1;
        if (lastIndex >= 0) {
            // This is the layout that contains the checkbox and EditText
            android.view.ViewGroup layout = (android.view.ViewGroup) checklistContainer.getChildAt(lastIndex);

            // Find the checkbox and EditText
            for (int i = 0; i < layout.getChildCount(); i++) {
                android.view.View child = layout.getChildAt(i);
                if (child instanceof android.widget.CheckBox) {
                    ((android.widget.CheckBox) child).setChecked(isChecked);
                } else if (child instanceof android.widget.EditText) {
                    ((android.widget.EditText) child).setText(text);
                }
            }
        }
    }

    private void updateNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        viewModel.setTitle(title);
        viewModel.setContent(content);

        // Save checklist items from UI
        viewModel.setChecklistItems(getChecklistItemsFromUI());

        // Clear existing audio paths and set them correctly
        viewModel.clearAudioPaths();

        // Add all existing audio paths that weren't deleted
        for (String path : existingAudioPaths) {
            viewModel.addAudioPath(path);
        }

        // Add all new audio paths
        for (String path : newAudioPaths) {
            viewModel.addAudioPath(path);
        }

        // Save note - using the saveNote method which is implemented in the base class
        viewModel.saveNote();
    }
}

package com.haui.notetakingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.note.DeletedNoteActivity;
import com.haui.notetakingapp.ui.note.EditNoteActivity;
import com.haui.notetakingapp.ui.note.NewNoteActivity;
import com.haui.notetakingapp.ui.setting.Setting;
import com.haui.notetakingapp.viewmodel.HomeViewModel;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvNotes;
    private ActivityResultLauncher<Intent> newNoteActivityLauncher;

    private ActivityResultLauncher<Intent> editNoteActivityLauncher;
    private ImageButton btnTrashCan, btnSetting;
    private HomeViewModel viewModel;
    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0);
            return insets;
        });

        // Initialize UI components
        bindView();

        // Setup RecyclerView
        noteAdapter = new NoteAdapter(new ArrayList<>());
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(noteAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe notes from ViewModel
        viewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.setNotes(notes);
        });

        // Initialize activity launcher for new note creation
        setupNewNoteActivityLauncher();

        setupEditNoteActivityLauncher();

        // Setup button click listeners
        setupClickListeners();
    }

    private void bindView() {
        rvNotes = findViewById(R.id.rv_notes);
        btnTrashCan = findViewById(R.id.btn_trash_can);
        btnSetting = findViewById(R.id.btn_setting);
    }

    private void setupNewNoteActivityLauncher() {
        newNoteActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Note note = (Note) result.getData().getSerializableExtra("objNewNote");
                if (note != null) {
                    viewModel.insert(note);
                }
            }
        });
    }

    private void setupEditNoteActivityLauncher() {
        editNoteActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Note editedNote = (Note) result.getData().getSerializableExtra("objEditedNote");
                if (editedNote != null) {
                    viewModel.update(editedNote); // Update the note in database
                }
            }
        });
    }

    private void setupClickListeners() {
        FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NewNoteActivity.class);
            newNoteActivityLauncher.launch(intent);
        });

        btnTrashCan.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, DeletedNoteActivity.class);
            startActivity(intent);
        });

        btnSetting.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, Setting.class);
            startActivity(intent);
        });

        noteAdapter.setOnNoteClickListener(note -> {
            Intent intent = new Intent(HomeActivity.this, EditNoteActivity.class);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("noteToEdit", note);  // Make sure Note implements Serializable
            editNoteActivityLauncher.launch(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

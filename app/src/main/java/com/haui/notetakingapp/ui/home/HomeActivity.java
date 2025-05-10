package com.haui.notetakingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.note.DeletedNoteActivity;
import com.haui.notetakingapp.ui.note.EditNoteActivity;
import com.haui.notetakingapp.ui.note.NewNoteActivity;
import com.haui.notetakingapp.ui.setting.Setting;
import com.haui.notetakingapp.viewmodel.HomeViewModel;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {
    private ImageButton btnTrashCan, btnSetting;
    private RecyclerView rvNotes;
    private NoteAdapter noteAdapter;
    private HomeViewModel viewModel;
    private ActivityResultLauncher<Intent> newNoteActivityLauncher;
    private ActivityResultLauncher<Intent> editNoteActivityLauncher;

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
        noteAdapter = new NoteAdapter(new ArrayList<>(), this);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(int position) {
        Note selectedNote = noteAdapter.getNoteAt(position);
        Intent intent = new Intent(HomeActivity.this, EditNoteActivity.class);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("noteToEdit", selectedNote);
        editNoteActivityLauncher.launch(intent);
    }

    @Override
    public boolean onLongClick(int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_note_menu, null);
        bottomSheetDialog.setContentView(view);

        Note selectedNote = noteAdapter.getNoteAt(position);

        view.findViewById(R.id.optionPin).setOnClickListener(v -> {
            selectedNote.setPinned(!selectedNote.isPinned());
            viewModel.update(selectedNote);
            String message = selectedNote.isPinned() ? "Đã ghim ghi chú" : "Đã bỏ ghim ghi chú";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.optionDelete).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xóa ghi chú")
                    .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        viewModel.softDelete(selectedNote);
                        Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.optionShare).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, selectedNote.getContent());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ ghi chú"));
            bottomSheetDialog.dismiss();
        });
        
        bottomSheetDialog.show();
        
        return true;
    }
}

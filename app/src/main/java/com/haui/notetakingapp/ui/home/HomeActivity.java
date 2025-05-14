package com.haui.notetakingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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
import com.haui.notetakingapp.ui.base.BaseActivity;
import com.haui.notetakingapp.ui.note.DeletedNoteActivity;
import com.haui.notetakingapp.ui.note.EditNoteActivity;
import com.haui.notetakingapp.ui.note.NewNoteActivity;
import com.haui.notetakingapp.ui.setting.SettingActivity;
import com.haui.notetakingapp.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {
    private ImageButton btnTrashCan, btnSetting;
    private RecyclerView rvPinnedNotes, rvUnpinnedNotes;
    private NoteAdapter pinnedAdapter, unpinnedAdapter;
    private TextView tvPinnedHeader, tvUnpinnedHeader, tvEmptyNotes;
    private SearchView searchView;
    private View dividerPinned;
    private HomeViewModel viewModel;
    private ActivityResultLauncher<Intent> newNoteActivityLauncher;
    private ActivityResultLauncher<Intent> editNoteActivityLauncher;
    private List<Note> allNotes;

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

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize allNotes
        allNotes = new ArrayList<>();

        // Setup RecyclerViews
        pinnedAdapter = new NoteAdapter(new ArrayList<>());
        unpinnedAdapter = new NoteAdapter(new ArrayList<>());

        // Set listeners for adapters
        pinnedAdapter.setOnNoteListener(new NoteAdapter.OnNoteListener() {
            @Override
            public void onClick(int position) {
                handleNoteClick(pinnedAdapter, position);
            }

            @Override
            public boolean onLongClick(int position) {
                return handleNoteLongClick(pinnedAdapter, position);
            }
        });

        unpinnedAdapter.setOnNoteListener(new NoteAdapter.OnNoteListener() {
            @Override
            public void onClick(int position) {
                handleNoteClick(unpinnedAdapter, position);
            }

            @Override
            public boolean onLongClick(int position) {
                return handleNoteLongClick(unpinnedAdapter, position);
            }
        });

        viewModel.getLayoutSetting().observe(this, layout -> {
            int spanCount = layout.equals("Xem theo ô lưới") ? 2 : 1;
            StaggeredGridLayoutManager pinnedLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
            StaggeredGridLayoutManager unpinnedLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
            rvPinnedNotes.setLayoutManager(pinnedLayoutManager);
            rvUnpinnedNotes.setLayoutManager(unpinnedLayoutManager);
        });

        rvPinnedNotes.setAdapter(pinnedAdapter);
        rvUnpinnedNotes.setAdapter(unpinnedAdapter);

        // Observe notes and store original list
        viewModel.getAllNotes().observe(this, notes -> {
            allNotes.clear();
            allNotes.addAll(notes);
            filterNotes(""); // Hiển thị tất cả khi không có tìm kiếm
        });

        // Setup SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Không xử lý khi nhấn Enter
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });

        setupNewNoteActivityLauncher();
        setupEditNoteActivityLauncher();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshSettings();
    }

    private void bindView() {
        rvPinnedNotes = findViewById(R.id.rv_notes);
        rvUnpinnedNotes = findViewById(R.id.rv_notes_not);
        btnTrashCan = findViewById(R.id.btn_trash_can);
        btnSetting = findViewById(R.id.btn_setting);
        tvPinnedHeader = findViewById(R.id.tv_pinned_header);
        tvUnpinnedHeader = findViewById(R.id.tv_unpinned_header);
        dividerPinned = findViewById(R.id.divider_pinned);
        searchView = findViewById(R.id.search_view);
        tvEmptyNotes = findViewById(R.id.tv_empty_notes);
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
                    viewModel.update(editedNote);
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
            Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
        });
    }

    private void filterNotes(String query) {
        List<Note> pinnedNotes = new ArrayList<>();
        List<Note> unpinnedNotes = new ArrayList<>();

        if (query.trim().isEmpty()) {
            for (Note note : allNotes) {
                if (note.isPinned()) {
                    pinnedNotes.add(note);
                } else {
                    unpinnedNotes.add(note);
                }
            }
        } else {
            String queryLowerCase = query.toLowerCase();
            for (Note note : allNotes) {
                if (note.getTitle() != null && note.getTitle().toLowerCase().contains(queryLowerCase)
                        || note.getContent() != null && note.getContent().toLowerCase().contains(queryLowerCase)) {
                    if (note.isPinned()) {
                        pinnedNotes.add(note);
                    } else {
                        unpinnedNotes.add(note);
                    }
                }
            }
        }

        pinnedAdapter.setSearchTerm(query.trim());
        unpinnedAdapter.setSearchTerm(query.trim());

        pinnedAdapter.setNotes(pinnedNotes);
        unpinnedAdapter.setNotes(unpinnedNotes);

        tvPinnedHeader.setVisibility(pinnedNotes.isEmpty() ? View.GONE : View.VISIBLE);
        dividerPinned.setVisibility(pinnedNotes.isEmpty() ? View.GONE : View.VISIBLE);
        tvUnpinnedHeader.setVisibility(unpinnedNotes.isEmpty() ? View.GONE : View.VISIBLE);

        tvEmptyNotes.setVisibility(pinnedNotes.isEmpty() && unpinnedNotes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleNoteClick(NoteAdapter adapter, int position) {
        Note selectedNote = adapter.getNoteAt(position);
        if (selectedNote != null) {
            Intent intent = new Intent(HomeActivity.this, EditNoteActivity.class);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("noteToEdit", selectedNote);
            editNoteActivityLauncher.launch(intent);
        }
    }

    private boolean handleNoteLongClick(NoteAdapter adapter, int position) {
        Note selectedNote = adapter.getNoteAt(position);
        if (selectedNote == null) {
            return false;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_note_menu, null);
        bottomSheetDialog.setContentView(view);

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
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        viewModel.softDelete(selectedNote);
                        Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
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

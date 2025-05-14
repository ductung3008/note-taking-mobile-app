package com.haui.notetakingapp.ui.note;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.base.BaseActivity;
import com.haui.notetakingapp.ui.home.NoteAdapter;
import com.haui.notetakingapp.viewmodel.DeletedNoteViewModel;

import java.util.ArrayList;

public class DeletedNoteActivity extends BaseActivity implements NoteAdapter.OnNoteListener {

    private DeletedNoteViewModel viewModel;
    private RecyclerView rvDeletedNotes;
    private NoteAdapter deletedNoteAdapter;
    private ImageButton btnBack;
    private ImageButton btnEmptyTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deleted_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
    }

    private void bindViews() {
        rvDeletedNotes = findViewById(R.id.rv_deleted_notes);
        btnBack = findViewById(R.id.btn_back);
        btnEmptyTrash = findViewById(R.id.btn_empty_trash);
    }

    private void setupRecyclerView() {
        deletedNoteAdapter = new NoteAdapter(new ArrayList<>(), this);
        rvDeletedNotes.setLayoutManager(new LinearLayoutManager(this));
        rvDeletedNotes.setAdapter(deletedNoteAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DeletedNoteViewModel.class);

        // Quan sát danh sách ghi chú đã xóa từ ViewModel
        viewModel.getDeletedNotes().observe(this, notes -> {
            deletedNoteAdapter.setNotes(notes);
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEmptyTrash.setOnClickListener(v -> showEmptyTrashConfirmation());
    }

    private void showEmptyTrashConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả ghi chú đã xóa?")
                .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn tất cả ghi chú trong thùng rác không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    viewModel.emptyTrash();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    // Xử lý khi click vào một mục ghi chú trong thùng rác
    @Override
    public void onClick(int position) {
        // Ví dụ: Mở ghi chú để xem chi tiết (chỉ đọc)
        Note clickedNote = deletedNoteAdapter.getNotes().get(position);
        // Intent intent = new Intent(this, ViewDeletedNoteActivity.class); // Cần tạo ViewDeletedNoteActivity
        // intent.putExtra("note", clickedNote);
        // startActivity(intent);
    }

    @Override
    public boolean onLongClick(int position) {
        Note selectedNote = deletedNoteAdapter.getNotes().get(position);
        showDeletedNoteOptionsBottomSheet(selectedNote);
        return true;
    }

    private void showDeletedNoteOptionsBottomSheet(Note note) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_deleted_note_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView optionRestore = bottomSheetView.findViewById(R.id.optionRestore);
        TextView optionPermanentlyDelete = bottomSheetView.findViewById(R.id.optionPermanentlyDelete);

        optionRestore.setOnClickListener(v -> {
            viewModel.restoreNote(note);
            bottomSheetDialog.dismiss();
        });

        optionPermanentlyDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa vĩnh viễn ghi chú?")
                    .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn ghi chú '" + note.getTitle() + "' không? Hành động này không thể hoàn tác.")
                    .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                        viewModel.permanentlyDeleteNote(note);
                    })
                    .setNegativeButton("Hủy", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}

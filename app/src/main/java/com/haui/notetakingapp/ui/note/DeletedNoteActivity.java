package com.haui.notetakingapp.ui.note;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.home.NoteAdapter;
import com.haui.notetakingapp.viewmodel.DeletedNoteViewModel;

import java.util.ArrayList;

public class DeletedNoteActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener { // Triển khai interface listener nếu NoteAdapter hỗ trợ

    private DeletedNoteViewModel viewModel;
    private RecyclerView rvDeletedNotes;
    private NoteAdapter deletedNoteAdapter; // Sử dụng NoteAdapter hoặc tạo adapter riêng
    private ImageButton btnBackTrash;
    private ImageButton btnEmptyTrash; // Nút xóa tất cả

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
        btnBackTrash = findViewById(R.id.btn_back_trash);
        btnEmptyTrash = findViewById(R.id.btn_empty_trash); // Kết nối với nút xóa tất cả trong XML
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
            // Cập nhật dữ liệu cho adapter khi có thay đổi
            deletedNoteAdapter.setNotes(notes); // Đảm bảo NoteAdapter có phương thức setNotes(List<Note>)
            // Nếu bạn sử dụng adapter riêng, đảm bảo nó có phương thức cập nhật dữ liệu
        });
    }

    private void setupClickListeners() {
        btnBackTrash.setOnClickListener(v -> finish()); // Quay lại màn hình trước

        btnEmptyTrash.setOnClickListener(v -> showEmptyTrashConfirmation()); // Hiển thị xác nhận xóa tất cả
    }

    // Phương thức hiển thị dialog xác nhận xóa tất cả (bạn cần tự triển khai dialog này)
    private void showEmptyTrashConfirmation() {
        // TODO: Hiển thị AlertDialog hoặc DialogFragment hỏi người dùng có chắc chắn muốn xóa tất cả không
        // Ví dụ đơn giản sử dụng Toast:
        Toast.makeText(this, "Nhấn giữ nút này để xóa tất cả ghi chú đã xóa", Toast.LENGTH_SHORT).show();

        // Hoặc triển khai dialog xác nhận chi tiết hơn
         new AlertDialog.Builder(this)
             .setTitle("Xóa tất cả ghi chú đã xóa?")
             .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn tất cả ghi chú trong thùng rác không? Hành động này không thể hoàn tác.")
             .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                 viewModel.emptyTrash(); // Gọi phương thức xóa tất cả từ ViewModel
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
        Toast.makeText(this, "Clicked: " + clickedNote.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(int position) {

        Note selectedNote = deletedNoteAdapter.getNotes().get(position);
        showDeletedNoteOptionsBottomSheet(selectedNote);
        return true;
    }

    // Phương thức hiển thị PopupMenu cho ghi chú đã xóa
    private void showDeletedNoteOptionsBottomSheet(Note note) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_deleted_note_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Ánh xạ các tùy chọn từ layout mới
        TextView optionRestore = bottomSheetView.findViewById(R.id.optionRestore);
        TextView optionPermanentlyDelete = bottomSheetView.findViewById(R.id.optionPermanentlyDelete);

        // Thiết lập listener cho tùy chọn "Khôi phục"
        optionRestore.setOnClickListener(v -> {
            viewModel.restoreNote(note);
            Toast.makeText(this, "Đã khôi phục ghi chú: " + note.getTitle(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        // Thiết lập listener cho tùy chọn "Xóa vĩnh viễn"
        optionPermanentlyDelete.setOnClickListener(v -> {
            // Hiển thị dialog xác nhận trước khi xóa vĩnh viễn
            new AlertDialog.Builder(this)
                    .setTitle("Xóa vĩnh viễn ghi chú?")
                    .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn ghi chú '" + note.getTitle() + "' không? Hành động này không thể hoàn tác.")
                    .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                        viewModel.permanentlyDeleteNote(note);
                        Toast.makeText(this, "Đã xóa vĩnh viễn ghi chú", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            bottomSheetDialog.dismiss(); // Đóng bottom sheet sau khi hiển thị dialog xác nhận
        });

        bottomSheetDialog.show();
    }


    // Bạn sẽ cần cập nhật NoteAdapter hoặc tạo DeletedNoteAdapter riêng
    // để triển khai OnNoteListener interface và cung cấp phương thức getNotes()
    // Ví dụ: Interface OnNoteListener trong NoteAdapter
    public interface OnNoteListener {
        void onNoteClick(int position);
    }
}

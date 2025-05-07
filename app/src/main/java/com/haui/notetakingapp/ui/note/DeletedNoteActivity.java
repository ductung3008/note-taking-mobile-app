package com.haui.notetakingapp.ui.note;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.home.NoteAdapter; // Có thể tái sử dụng hoặc tạo adapter mới
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
        deletedNoteAdapter = new NoteAdapter(new ArrayList<>()); // Khởi tạo adapter với danh sách trống
        // Nếu bạn tạo adapter riêng cho thùng rác, sử dụng adapter đó ở đây
        // deletedNoteAdapter = new DeletedNoteAdapter(new ArrayList<>(), this); // Truyền listener

        rvDeletedNotes.setLayoutManager(new LinearLayoutManager(this)); // Sử dụng LinearLayoutManager như trong ảnh
        rvDeletedNotes.setAdapter(deletedNoteAdapter);

        // Nếu NoteAdapter hiện tại không hỗ trợ click listener, bạn cần thêm vào hoặc tạo adapter mới
        // và thiết lập listener ở đây. Ví dụ:
        // deletedNoteAdapter.setOnNoteListener(this); // Nếu adapter có phương thức này
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
        // new AlertDialog.Builder(this)
        //     .setTitle("Xóa tất cả ghi chú đã xóa?")
        //     .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn tất cả ghi chú trong thùng rác không? Hành động này không thể hoàn tác.")
        //     .setPositiveButton("Xóa tất cả", (dialog, which) -> {
        //         viewModel.emptyTrash(); // Gọi phương thức xóa tất cả từ ViewModel
        //     })
        //     .setNegativeButton("Hủy", null)
        //     .setIcon(android.R.drawable.ic_dialog_alert)
        //     .show();
    }


    // Xử lý khi click vào một mục ghi chú trong thùng rác
    @Override
    public void onNoteClick(int position) {
        // TODO: Xử lý khi click vào ghi chú trong thùng rác.
        // Có thể hiển thị một menu ngữ cảnh (PopupMenu) với các tùy chọn
        // "Khôi phục" và "Xóa vĩnh viễn".
        Note clickedNote = deletedNoteAdapter.getNotes().get(position);
        showNoteOptionsPopup(clickedNote, position);
    }

    // Phương thức hiển thị PopupMenu cho ghi chú đã xóa
    private void showNoteOptionsPopup(Note note, int position) {
        View view = rvDeletedNotes.findViewHolderForAdapterPosition(position).itemView; // Lấy view của item đó
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_deleted_note_options, popup.getMenu()); // Cần tạo file menu này

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_restore) { // ID cho tùy chọn khôi phục
                viewModel.restoreNote(note);
                Toast.makeText(this, "Đã khôi phục ghi chú", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_permanently_delete) { // ID cho tùy chọn xóa vĩnh viễn
                viewModel.permanentlyDeleteNote(note);
                Toast.makeText(this, "Đã xóa vĩnh viễn ghi chú", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }


    // Bạn sẽ cần cập nhật NoteAdapter hoặc tạo DeletedNoteAdapter riêng
    // để triển khai OnNoteListener interface và cung cấp phương thức getNotes()
    // Ví dụ: Interface OnNoteListener trong NoteAdapter
    public interface OnNoteListener {
        void onNoteClick(int position);
    }
}
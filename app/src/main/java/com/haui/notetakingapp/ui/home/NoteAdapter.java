package com.haui.notetakingapp.ui.home;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.utils.DateTimeUtils;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final int[] noteColors = {
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FEFEE6"),
            Color.parseColor("#EAF9FF"),
            Color.parseColor("#F2FCF1"),
            Color.parseColor("#FFEEE4")
    };
    private List<Note> notes;
    private OnNoteListener listener;

    // Constructor cũ
    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    // Constructor mới có thêm listener
    public NoteAdapter(List<Note> notes, OnNoteListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    public void setOnNoteListener(OnNoteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view, listener); // Truyền listener vào ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        // Sử dụng DateTimeUtils để format ngày tháng
        holder.tvDate.setText(DateTimeUtils.formatToDayMonth(note.getCreatedAt()));

        // Hiển thị/ẩn icon ghim
        holder.ivPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        // Thiết lập màu nền cho item note dựa trên vị trí
        holder.cardLayout.setBackgroundColor(noteColors[position % noteColors.length]);

        // Thiết lập hiển thị các indicator cho media
        setupMediaIndicators(holder, note);

        // Tải và hiển thị ảnh đầu tiên nếu có
        setupNoteImage(holder, note);
    }

    private void setupMediaIndicators(NoteViewHolder holder, Note note) {
        // Hiển thị indicator nếu có ít nhất một đường dẫn trong danh sách
        holder.ivImageIndicator.setVisibility((note.getImagePaths() != null && !note.getImagePaths().isEmpty()) ? View.VISIBLE : View.GONE);
        holder.ivAudioIndicator.setVisibility((note.getAudioPaths() != null && !note.getAudioPaths().isEmpty()) ? View.VISIBLE : View.GONE);
        holder.ivDrawingIndicator.setVisibility((note.getDrawingPaths() != null && !note.getDrawingPaths().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    private void setupNoteImage(NoteViewHolder holder, Note note) {
        holder.imageContainer.removeAllViews(); // Xóa các view ảnh cũ trước khi thêm mới

        // Nếu không có đường dẫn ảnh hoặc danh sách rỗng, ẩn container và thoát
        if (note.getImagePaths() == null || note.getImagePaths().isEmpty()) {
            holder.imageContainer.setVisibility(View.GONE);
            return;
        }

        // Nếu có ảnh, hiển thị container
        holder.imageContainer.setVisibility(View.VISIBLE);

        // Lấy đường dẫn ảnh đầu tiên để hiển thị thumbnail
        String imagePath = note.getImagePaths().get(0);

        // Tạo ImageView để hiển thị ảnh
        ImageView imageView = new ImageView(holder.itemView.getContext());

        // Thiết lập LayoutParams cho ImageView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(holder.itemView.getContext(), 150) // Chiều cao cố định 150dp
        );
        imageView.setLayoutParams(params);

        // Thiết lập ScaleType
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Tạo FrameLayout để chứa ImageView và badge số lượng ảnh (nếu có)
        FrameLayout frameLayout = new FrameLayout(holder.itemView.getContext());
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT // Chiều cao tự điều chỉnh theo nội dung
        ));

        // Thêm ImageView vào FrameLayout
        frameLayout.addView(imageView);

        // Sử dụng Glide để tải và hiển thị ảnh
        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .apply(new RequestOptions()
                        // Có thể thêm placeholder hoặc error image ở đây
                        .placeholder(R.drawable.ic_launcher_background) // Thay bằng drawable placeholder của bạn
                        .error(R.drawable.ic_launcher_background))    // Thay bằng drawable error của bạn
                .transition(DrawableTransitionOptions.withCrossFade()) // Hiệu ứng chuyển ảnh
                .centerCrop()
                .into(imageView);

        // Nếu có nhiều hơn một ảnh, thêm badge hiển thị số lượng ảnh còn lại
        if (note.getImagePaths().size() > 1) {
            TextView counterBadge = new TextView(holder.itemView.getContext());
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            // Đặt badge ở góc dưới bên phải của ảnh
            badgeParams.gravity = Gravity.END | Gravity.BOTTOM;
            badgeParams.setMargins(0, 0, dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 8));
            counterBadge.setLayoutParams(badgeParams);

            // Thiết lập văn bản cho badge (ví dụ: +2, +3, ...)
            counterBadge.setText("+" + (note.getImagePaths().size() - 1));
            counterBadge.setTextColor(Color.WHITE);
            // Thiết lập background cho badge
            counterBadge.setBackgroundResource(android.R.drawable.ic_dialog_info); // Có thể thay bằng drawable background tùy chỉnh
            counterBadge.setBackground(new ColorDrawable(Color.parseColor("#80000000"))); // Nền đen trong suốt
            // Thiết lập padding cho badge
            counterBadge.setPadding(dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 4),
                    dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 4));
            counterBadge.setTextSize(12); // Kích thước chữ cho badge

            // Thêm badge vào FrameLayout
            frameLayout.addView(counterBadge);
        }

        // Thêm FrameLayout chứa ảnh (và badge nếu có) vào imageContainer
        holder.imageContainer.addView(frameLayout);
    }

    // Phương thức chuyển đổi dp sang pixel
    private int dpToPx(android.content.Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // Phương thức lấy danh sách ghi chú (để sử dụng trong Activity khi click)
    public List<Note> getNotes() {
        return notes;
    }

    // Phương thức cập nhật danh sách ghi chú và thông báo cho adapter
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public Note getNoteAt(int position) {
        if (position >= 0 && position < notes.size()) {
            return notes.get(position);
        }
        return null; // Return null for invalid positions
    }

    // Định nghĩa Interface Listener
    public interface OnNoteListener {
        void onClick(int position);

        boolean onLongClick(int position);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvTitle;
        TextView tvContent;
        TextView tvDate;
        ImageView ivPin;
        ImageView ivImageIndicator;
        ImageView ivAudioIndicator;
        ImageView ivDrawingIndicator;
        LinearLayout cardLayout;
        LinearLayout imageContainer;
        OnNoteListener onNoteListener;

        public NoteViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivPin = itemView.findViewById(R.id.iv_pin);
            ivImageIndicator = itemView.findViewById(R.id.iv_image_indicator);
            ivAudioIndicator = itemView.findViewById(R.id.iv_audio_indicator);
            ivDrawingIndicator = itemView.findViewById(R.id.iv_drawing_indicator);
            cardLayout = itemView.findViewById(R.id.card_layout);
            imageContainer = itemView.findViewById(R.id.image_container);

            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Gọi phương thức onNoteClick của listener khi item được click
            if (onNoteListener != null) {
                onNoteListener.onClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (onNoteListener != null) {
                return onNoteListener.onLongClick(getAdapterPosition());
            }
            return false;
        }
    }
}

package com.haui.notetakingapp.ui.home;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
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
import com.haui.notetakingapp.utils.TextHighlighter;

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
    private String searchTerm = "";

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

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
        return new NoteViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            if (note.getTitle() != null) {
                SpannableString titleSpannable = TextHighlighter.highlightText(note.getTitle(), searchTerm);
                holder.tvTitle.setText(titleSpannable);
            } else {
                holder.tvTitle.setText("");
            }

            if (note.getContent() != null) {
                SpannableString contentSpannable = TextHighlighter.highlightText(note.getContent(), searchTerm);
                holder.tvContent.setText(contentSpannable);
            } else {
                holder.tvContent.setText("");
            }
        } else {
            holder.tvTitle.setText(note.getTitle());
            holder.tvContent.setText(note.getContent());
        }

        holder.tvDate.setText(DateTimeUtils.formatToDayMonth(note.getCreatedAt()));

        holder.ivPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        holder.cardLayout.setBackgroundColor(noteColors[position % noteColors.length]);

        setupMediaIndicators(holder, note);
        setupNoteImage(holder, note);
    }

    private void setupMediaIndicators(NoteViewHolder holder, Note note) {
        holder.ivImageIndicator.setVisibility((note.getImagePaths() != null && !note.getImagePaths().isEmpty()) ? View.VISIBLE : View.GONE);
        holder.ivAudioIndicator.setVisibility((note.getAudioPaths() != null && !note.getAudioPaths().isEmpty()) ? View.VISIBLE : View.GONE);
        holder.ivDrawingIndicator.setVisibility((note.getDrawingPaths() != null && !note.getDrawingPaths().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        notifyDataSetChanged();
    }

    private void setupNoteImage(NoteViewHolder holder, Note note) {
        holder.imageContainer.removeAllViews();

        if ((note.getImagePaths() == null || note.getImagePaths().isEmpty())
                && (note.getDrawingPaths() == null || note.getDrawingPaths().isEmpty())) {
            holder.imageContainer.setVisibility(View.GONE);
            return;
        }

        holder.imageContainer.setVisibility(View.VISIBLE);

        String imagePath = null;
        if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
            imagePath = note.getImagePaths().get(0);
        } else if (note.getDrawingPaths() != null && !note.getDrawingPaths().isEmpty()) {
            imagePath = note.getDrawingPaths().get(0);
        }

        ImageView imageView = new ImageView(holder.itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(holder.itemView.getContext(), 150)
        );
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        FrameLayout frameLayout = new FrameLayout(holder.itemView.getContext());
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        frameLayout.addView(imageView);

        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(imageView);

        if (note.getImagePaths().size() > 1) {
            TextView counterBadge = new TextView(holder.itemView.getContext());
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            badgeParams.gravity = Gravity.END | Gravity.BOTTOM;
            badgeParams.setMargins(0, 0, dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 8));
            counterBadge.setLayoutParams(badgeParams);

            counterBadge.setText("+" + (note.getImagePaths().size() - 1));
            counterBadge.setTextColor(Color.WHITE);
            counterBadge.setBackground(new ColorDrawable(Color.parseColor("#80000000")));
            counterBadge.setPadding(dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 4),
                    dpToPx(holder.itemView.getContext(), 8),
                    dpToPx(holder.itemView.getContext(), 4));
            counterBadge.setTextSize(12);

            frameLayout.addView(counterBadge);
        }

        holder.imageContainer.addView(frameLayout);
    }

    private int dpToPx(android.content.Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public Note getNoteAt(int position) {
        if (position >= 0 && position < notes.size()) {
            return notes.get(position);
        }
        return null;
    }

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

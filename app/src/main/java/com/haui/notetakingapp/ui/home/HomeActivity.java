package com.haui.notetakingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.ui.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvNotes;
    private ImageButton btnSetting;

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

        bindView();
        List<Note> sampleNotes = initSampleData();
        NoteAdapter noteAdapter = new NoteAdapter(sampleNotes);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(noteAdapter);

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Setting.class);
                startActivity(intent);
            }
        });
    }

    private void bindView() {
        rvNotes = findViewById(R.id.rv_notes);
        btnSetting = findViewById(R.id.btn_setting);
    }

    private List<Note> initSampleData() {
        List<Note> sampleNotes = new ArrayList<>();

        int[] drawableResources = new int[]{
                R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_foreground,
                R.drawable.ic_add_white,
                R.drawable.ic_setting
        };

        String[] noteTitles = {
                "Meeting Notes", "Shopping List", "Project Ideas",
                "Daily Journal", "Book Summary", "Travel Plans",
                "Recipes", "Workout Plan", "Goals", "Lecture Notes"
        };

        String[] noteContents = {
                "Discussed the timeline for the next release and assigned tasks to team members.",
                "Milk, Eggs, Bread, Butter, Cheese, Vegetables, Fruits, Chicken",
                "App to track daily habits and provide insights on productivity trends.",
                "Today I started working on my note-taking app project. Making good progress!",
                "The book explores the impact of technology on society and suggests ways to maintain human connections.",
                "Day 1: Visit museum\nDay 2: Hiking trip\nDay 3: Beach day\nDay 4: Local cuisine tour",
                "Spaghetti Carbonara:\n- 200g spaghetti\n- 100g pancetta\n- 2 eggs\n- 50g pecorino cheese\n- Black pepper",
                "Monday: Upper body\nTuesday: Cardio\nWednesday: Lower body\nThursday: Rest\nFriday: Full body",
                "1. Complete Android app project\n2. Learn a new language\n3. Read 20 books this year\n4. Exercise 3 times a week",
                "Key concepts: polymorphism, inheritance, encapsulation, and abstraction."
        };

        for (int i = 0; i < 20; i++) {
            Note note = new Note();
            note.setTitle(noteTitles[i % noteTitles.length]);
            note.setContent(noteContents[i % noteContents.length]);
            note.setCreatedAt(System.currentTimeMillis() - (i * 86400000));
            note.setUpdatedAt(System.currentTimeMillis() - (i * 3600000));

            note.setPinned(i % 5 == 0);

            if (i % 3 != 2) {
                List<String> imagePaths = new ArrayList<>();
                int imageCount = (i % 3) + 1;
                for (int j = 0; j < imageCount; j++) {
                    int drawableIndex = (i + j) % drawableResources.length;
                    imagePaths.add("android.resource://" + getPackageName() + "/" + drawableResources[drawableIndex]);
                }
                note.setImagePaths(imagePaths);
            }

            if (i % 4 == 0) {
                List<String> audioPaths = new ArrayList<>();
                audioPaths.add("sample_audio_" + (i + 1) + ".mp3");
                note.setAudioPaths(audioPaths);
            }

            if (i % 5 == 0) {
                List<String> drawingPaths = new ArrayList<>();
                drawingPaths.add("sample_drawing_" + (i + 1) + ".png");
                note.setDrawingPaths(drawingPaths);
            }

            sampleNotes.add(note);
        }

        return sampleNotes;
    }
}

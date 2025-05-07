package com.haui.notetakingapp.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.haui.notetakingapp.R;

import java.util.List;

public class Setting extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout textSizeRow, sortByRow, layoutRow, themeRow, githubRow;
    private TextView textSelectedTextSize, textSelectedSortBy, textSelectedLayout, textSelectedTheme;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindView();
        setupDropdown();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        githubRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/ductung3008/note-taking-mobile-app";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void bindView() {
        btnBack = findViewById(R.id.btn_back);

        textSizeRow = findViewById(R.id.setting_row_text_size);
        sortByRow = findViewById(R.id.setting_row_sort_by);
        layoutRow = findViewById(R.id.setting_row_layout);
        themeRow = findViewById(R.id.setting_row_theme);
        githubRow = findViewById(R.id.row_github);

        textSelectedTextSize = findViewById(R.id.text_selected_text_size);
        textSelectedSortBy = findViewById(R.id.text_selected_sort_by);
        textSelectedLayout = findViewById(R.id.text_selected_layout);
        textSelectedTheme = findViewById(R.id.text_selected_theme);
    }

    private void setupDropdown() {
        textSizeRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> textSizeOptions = List.of(getResources().getStringArray(R.array.text_size_options));
                showDropdown(v, textSizeOptions, textSizeOptions.indexOf(textSelectedTextSize.getText().toString()), textSelectedTextSize);
            }
        });

        sortByRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> sortByOptions = List.of(getResources().getStringArray(R.array.sort_by_options));
                showDropdown(v, sortByOptions, sortByOptions.indexOf(textSelectedSortBy.getText().toString()), textSelectedSortBy);
            }
        });

        layoutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> layoutOptions = List.of(getResources().getStringArray(R.array.layout_options));
                showDropdown(v, layoutOptions, layoutOptions.indexOf(textSelectedLayout.getText().toString()), textSelectedLayout);
            }
        });

        themeRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> themeOptions = List.of(getResources().getStringArray(R.array.theme_options));
                showDropdown(v, themeOptions, themeOptions.indexOf(textSelectedTheme.getText().toString()), textSelectedTheme);
            }
        });
    }

    private void showDropdown(View anchor, List<String> options, int selectedIndex, TextView targetTextView) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout dropdownLayout = new LinearLayout(this);
        dropdownLayout.setOrientation(LinearLayout.VERTICAL);
        dropdownLayout.setBackgroundResource(R.drawable.bg_dropdown);
        dropdownLayout.setClipToOutline(true);
        dropdownLayout.setPadding(0, 0, 0, 0);

        for (int i = 0; i < options.size(); i++) {
            View item = inflater.inflate(R.layout.item_drop_down, dropdownLayout, false);
            TextView textOption = item.findViewById(R.id.text_option);
            ImageView iconCheck = item.findViewById(R.id.icon_check);

            textOption.setText(options.get(i));

            if (i == selectedIndex) {
                item.setSelected(true);
                iconCheck.setVisibility(View.VISIBLE);
                item.setBackgroundColor(getResources().getColor(R.color.setting_drop_down_item_selected_bg));
                textOption.setTextColor(getResources().getColor(R.color.setting_drop_down_item_selected_text));
            } else {
                item.setSelected(false);
                iconCheck.setVisibility(View.GONE);
            }

            int finalSelectedIndex = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    targetTextView.setText(options.get(finalSelectedIndex));
                    popupWindow.dismiss();
                }
            });

            dropdownLayout.addView(item);
        }

        int popupWidth = anchor.getWidth() / 2 + 100;
        int xOffset = anchor.getWidth() - popupWidth - 100;
        popupWindow = new PopupWindow(dropdownLayout, popupWidth, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);
        popupWindow.showAsDropDown(anchor, xOffset, -20);
    }
}

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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.haui.notetakingapp.R;
import com.haui.notetakingapp.ui.auth.LoginActivity;
import com.haui.notetakingapp.ui.auth.RegisterActivity;
import com.haui.notetakingapp.ui.base.BaseActivity;
import com.haui.notetakingapp.ui.home.HomeActivity;
import com.haui.notetakingapp.viewmodel.SettingViewModel;

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends BaseActivity {
    private ImageButton btnBack;
    private LinearLayout textSizeRow, sortByRow, layoutRow, themeRow, githubRow, loginRow, registerRow;
    private TextView textSelectedTextSize, textSelectedSortBy, textSelectedLayout, textSelectedTheme;
    private List<String> textSizeOptions, sortByOptions, layoutOptions, themeOptions;
    private SettingViewModel viewModel;
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

        textSizeOptions = Arrays.asList(getResources().getStringArray(R.array.text_size_options));
        sortByOptions = Arrays.asList(getResources().getStringArray(R.array.sort_by_options));
        layoutOptions = Arrays.asList(getResources().getStringArray(R.array.layout_options));
        themeOptions = Arrays.asList(getResources().getStringArray(R.array.theme_options));

        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        bindView();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshUserState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void bindView() {
        btnBack = findViewById(R.id.btn_back);

        textSizeRow = findViewById(R.id.setting_row_text_size);
        sortByRow = findViewById(R.id.setting_row_sort_by);
        layoutRow = findViewById(R.id.setting_row_layout);
        themeRow = findViewById(R.id.setting_row_theme);
        githubRow = findViewById(R.id.row_github);
        loginRow = findViewById(R.id.row_login);
        registerRow = findViewById(R.id.row_register);

        textSelectedTextSize = findViewById(R.id.text_selected_text_size);
        textSelectedSortBy = findViewById(R.id.text_selected_sort_by);
        textSelectedLayout = findViewById(R.id.text_selected_layout);
        textSelectedTheme = findViewById(R.id.text_selected_theme);
    }

    private void observeViewModel() {
        viewModel.currentUser.observe(this, this::updateUIBasedOnAuthState);

        viewModel.settingsState.observe(this, state -> {
            if (state != null) {
                textSelectedTextSize.setText(state.getTextSize());
                textSelectedSortBy.setText(state.getSortBy());
                textSelectedLayout.setText(state.getLayout());
                textSelectedTheme.setText(state.getTheme());
            }
        });

        viewModel.textSize.observe(this, textSize -> textSelectedTextSize.setText(textSize));
        viewModel.sortBy.observe(this, sortBy -> textSelectedSortBy.setText(sortBy));
        viewModel.layout.observe(this, layout -> textSelectedLayout.setText(layout));
        viewModel.theme.observe(this, theme -> textSelectedTheme.setText(theme));

        viewModel.needsRestart.observe(this, needsRestart -> {
            if (needsRestart) {
                restartApp();
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        githubRow.setOnClickListener(v -> {
            String url = "https://github.com/ductung3008/note-taking-mobile-app";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        textSizeRow.setOnClickListener(this::showTextSizeDropdown);
        sortByRow.setOnClickListener(this::showSortByDropdown);
        layoutRow.setOnClickListener(this::showLayoutDropdown);
        themeRow.setOnClickListener(this::showThemeDropdown);
    }

    private void updateUIBasedOnAuthState(FirebaseUser currentUser) {
        if (currentUser != null) {
            if (loginRow.getChildCount() > 0) {
                loginRow.removeAllViews();
            }

            View userProfileView = LayoutInflater.from(this).inflate(
                    R.layout.user_profile_setting, loginRow, false);
            loginRow.addView(userProfileView);

            TextView profileName = userProfileView.findViewById(R.id.profile_name);
            TextView profileEmail = userProfileView.findViewById(R.id.profile_email);
            ImageView profileImage = userProfileView.findViewById(R.id.profile_image);

            profileName.setText(currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : getString(R.string.default_username));
            profileEmail.setText(currentUser.getEmail());

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.avatar_default)
                        .into(profileImage);
            }

            TextView logoutText = registerRow.findViewById(R.id.tvRegister);
            if (logoutText != null) {
                logoutText.setText(R.string.tv_setting_logout);
            } else {
                TextView textView = new TextView(this);
                textView.setText(R.string.tv_setting_logout);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                textView.setTextAppearance(R.style.SettingItem);
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, 0, 0, R.drawable.ic_chevron_right, 0);
                registerRow.removeAllViews();
                registerRow.addView(textView);
            }

            registerRow.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.logout_dialog_title)
                            .setMessage(R.string.logout_dialog_message)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                viewModel.logout();
                                Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(R.string.no, null)
                            .show());
        } else {
            loginRow.removeAllViews();
            TextView loginText = new TextView(this);
            loginText.setText(R.string.tv_login);
            loginText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            loginText.setTextAppearance(R.style.SettingItem);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(loginText, 0, 0, R.drawable.ic_chevron_right, 0);
            loginRow.addView(loginText);

            registerRow.removeAllViews();
            TextView registerText = new TextView(this);
            registerText.setText(R.string.tv_register);
            registerText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            registerText.setTextAppearance(R.style.SettingItem);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(registerText, 0, 0, R.drawable.ic_chevron_right, 0);
            registerRow.addView(registerText);

            loginRow.setOnClickListener(v -> {
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                startActivity(intent);
            });

            registerRow.setOnClickListener(v -> {
                Intent intent = new Intent(SettingActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showTextSizeDropdown(View anchor) {
        String currentTextSize = viewModel.settingsState.getValue() != null ?
                viewModel.settingsState.getValue().getTextSize() : viewModel.getInitialTextSize();
        int selectedIndex = textSizeOptions.indexOf(currentTextSize);

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        popupWindow = showDropdown(anchor, textSizeOptions, selectedIndex, this::onTextSizeSelected);
    }

    private void showSortByDropdown(View anchor) {
        String currentSortBy = viewModel.settingsState.getValue() != null ?
                viewModel.settingsState.getValue().getSortBy() : "";
        int selectedIndex = sortByOptions.indexOf(currentSortBy);

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        popupWindow = showDropdown(anchor, sortByOptions, selectedIndex, this::onSortBySelected);
    }

    private void showLayoutDropdown(View anchor) {
        String currentLayout = viewModel.settingsState.getValue() != null ?
                viewModel.settingsState.getValue().getLayout() : "";
        int selectedIndex = layoutOptions.indexOf(currentLayout);

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        popupWindow = showDropdown(anchor, layoutOptions, selectedIndex, this::onLayoutSelected);
    }

    private void showThemeDropdown(View anchor) {
        String currentTheme = viewModel.settingsState.getValue() != null ?
                viewModel.settingsState.getValue().getTheme() : viewModel.getInitialTheme();
        int selectedIndex = themeOptions.indexOf(currentTheme);

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        popupWindow = showDropdown(anchor, themeOptions, selectedIndex, this::onThemeSelected);
    }

    private void onTextSizeSelected(String textSize) {
        viewModel.saveTextSizeSetting(textSize);
        Toast.makeText(this, "Cỡ chữ đã chuyển sang " + textSize, Toast.LENGTH_SHORT).show();
    }

    private void onSortBySelected(String sortBy) {
        viewModel.saveSortBySetting(sortBy);
        Toast.makeText(this, "Sắp xếp đã chuyển sang " + sortBy, Toast.LENGTH_SHORT).show();
    }

    private void onLayoutSelected(String layout) {
        viewModel.saveLayoutSetting(layout);
        Toast.makeText(this, "Chế độ xem đã chuyển sang " + layout, Toast.LENGTH_SHORT).show();
    }

    private void onThemeSelected(String theme) {
        viewModel.saveThemeSetting(theme);
        Toast.makeText(this, "Chủ đề đã chuyển sang " + theme, Toast.LENGTH_SHORT).show();
    }

    private PopupWindow showDropdown(View anchor, List<String> options, int selectedIndex, OnItemClickListener listener) {
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
                item.setBackgroundColor(getResources().getColor(R.color.setting_drop_down_item_selected_bg, null));
                textOption.setTextColor(getResources().getColor(R.color.setting_drop_down_item_selected_text, null));
            } else {
                item.setSelected(false);
                iconCheck.setVisibility(View.GONE);
            }

            int finalSelectedIndex = i;
            item.setOnClickListener(v -> {
                String selectedOption = options.get(finalSelectedIndex);
                if (listener != null) {
                    listener.onItemClick(selectedOption);
                }
            });

            dropdownLayout.addView(item);
        }

        int popupWidth = anchor.getWidth() / 2 + 100;
        int xOffset = anchor.getWidth() - popupWidth - 100;
        popupWindow = new PopupWindow(dropdownLayout, popupWidth, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);
        popupWindow.showAsDropDown(anchor, xOffset, -20);
        return popupWindow;
    }

    private interface OnItemClickListener {
        void onItemClick(String item);
    }
}

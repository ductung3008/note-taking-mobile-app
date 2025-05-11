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
import androidx.appcompat.app.AppCompatActivity;
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
import com.haui.notetakingapp.viewmodel.SettingsViewModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout textSizeRow, sortByRow, layoutRow, themeRow, githubRow, loginRow, registerRow;
    private TextView textSelectedTextSize, textSelectedSortBy, textSelectedLayout, textSelectedTheme;
    private SettingsViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        bindView();
        setupDropdown();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshUserState();
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
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        githubRow.setOnClickListener(v -> {
            String url = "https://github.com/ductung3008/note-taking-mobile-app";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
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
            CircleImageView profileImage = userProfileView.findViewById(R.id.profile_image);

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

    private void setupDropdown() {
        textSizeRow.setOnClickListener(v -> {
            List<String> textSizeOptions = List.of(getResources().getStringArray(R.array.text_size_options));
            showDropdown(v, textSizeOptions, textSizeOptions.indexOf(textSelectedTextSize.getText().toString()), textSelectedTextSize);
        });

        sortByRow.setOnClickListener(v -> {
            List<String> sortByOptions = List.of(getResources().getStringArray(R.array.sort_by_options));
            showDropdown(v, sortByOptions, sortByOptions.indexOf(textSelectedSortBy.getText().toString()), textSelectedSortBy);
        });

        layoutRow.setOnClickListener(v -> {
            List<String> layoutOptions = List.of(getResources().getStringArray(R.array.layout_options));
            showDropdown(v, layoutOptions, layoutOptions.indexOf(textSelectedLayout.getText().toString()), textSelectedLayout);
        });

        themeRow.setOnClickListener(v -> {
            List<String> themeOptions = List.of(getResources().getStringArray(R.array.theme_options));
            showDropdown(v, themeOptions, themeOptions.indexOf(textSelectedTheme.getText().toString()), textSelectedTheme);
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
                item.setBackgroundColor(getResources().getColor(R.color.setting_drop_down_item_selected_bg, null));
                textOption.setTextColor(getResources().getColor(R.color.setting_drop_down_item_selected_text, null));
            } else {
                item.setSelected(false);
                iconCheck.setVisibility(View.GONE);
            }

            int finalSelectedIndex = i;
            item.setOnClickListener(v -> {
                targetTextView.setText(options.get(finalSelectedIndex));
                popupWindow.dismiss();
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

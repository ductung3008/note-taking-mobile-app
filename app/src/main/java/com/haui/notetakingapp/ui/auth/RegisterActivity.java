package com.haui.notetakingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.haui.notetakingapp.R;
import com.haui.notetakingapp.ui.base.BaseActivity;
import com.haui.notetakingapp.ui.home.HomeActivity;
import com.haui.notetakingapp.viewmodel.RegisterViewModel;

public class RegisterActivity extends BaseActivity {
    private EditText etEmail, etPassword, etRepeatPassword, etDisplayName;
    private Button btnRegister;
    private TextView tvSignIn;
    private ProgressBar progressBar;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        bindView();
        observeViewModel();
        setupListeners();
    }

    private void bindView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        etDisplayName = findViewById(R.id.etDisplayName);
        btnRegister = findViewById(R.id.btnRegister);
        tvSignIn = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            btnRegister.setEnabled(!isLoading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.registrationComplete.observe(this, registrationComplete -> {
            if (registrationComplete) {
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateRegisterButtonState();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etRepeatPassword.addTextChangedListener(textWatcher);
        etDisplayName.addTextChangedListener(textWatcher);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String repeatPassword = etRepeatPassword.getText().toString().trim();
            String displayName = etDisplayName.getText().toString().trim();

            if (validateInput(email, password, repeatPassword, displayName)) {
                viewModel.register(email, password, displayName);
            }
        });

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput(String email, String password, String repeatPassword, String displayName) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được để trống");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (!password.equals(repeatPassword)) {
            etRepeatPassword.setError("Mật khẩu không khớp");
            return false;
        }

        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Tên hiển thị không được để trống");
            return false;
        }

        return true;
    }

    private void updateRegisterButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();

        btnRegister.setEnabled(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(repeatPassword) && !TextUtils.isEmpty(displayName));
    }
}

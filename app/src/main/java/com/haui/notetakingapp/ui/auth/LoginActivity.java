package com.haui.notetakingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.haui.notetakingapp.viewmodel.LoginViewModel;

public class LoginActivity extends BaseActivity {
    private EditText etEmail, etPassword;
    private CheckBox cbRemember;
    private Button btnSignIn;
    private TextView tvForgot, tvSignUp;
    private ProgressBar progressBar;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        bindView();
        observeViewModel();
        setupListeners();
    }

    private void bindView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgot = findViewById(R.id.tvForgot);
        tvSignUp = findViewById(R.id.tvSignUp);
        progressBar = findViewById(R.id.progressBar);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            btnSignIn.setEnabled(!isLoading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.user.observe(this, user -> {
            if (user != null) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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
                updateLoginButtonState();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                viewModel.login(email, password);
            }
        });

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email trước", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.resetPassword(email);
                Toast.makeText(LoginActivity.this, "Email đặt lại mật khẩu đã được gửi đến " + email, Toast.LENGTH_LONG).show();
            }
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email là bắt buộc");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu là bắt buộc");
            return false;
        }

        return true;
    }

    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnSignIn.setEnabled(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password));
    }
}

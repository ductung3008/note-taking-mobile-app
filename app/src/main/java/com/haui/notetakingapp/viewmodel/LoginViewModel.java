package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.haui.notetakingapp.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();
    public final LiveData<FirebaseUser> user = _user;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            setUser(currentUser);
        }
    }

    public void login(String email, String password) {
        setLoading(true);
        setErrorMessage(null);

        authRepository.login(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        setUser(authRepository.getCurrentUser());
                    } else {
                        String errorMsg = authRepository.getLocalizedErrorMessage(task.getException());
                        setErrorMessage(errorMsg);
                    }
                });
    }

    public void resetPassword(String email) {
        setLoading(true);
        setErrorMessage(null);

        authRepository.resetPassword(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        String errorMsg = authRepository.getLocalizedErrorMessage(task.getException());
                        setErrorMessage(errorMsg);
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        mainHandler.post(() -> _isLoading.setValue(isLoading));
    }

    private void setErrorMessage(String message) {
        mainHandler.post(() -> _errorMessage.setValue(message));
    }

    private void setUser(FirebaseUser user) {
        mainHandler.post(() -> _user.setValue(user));
    }
}

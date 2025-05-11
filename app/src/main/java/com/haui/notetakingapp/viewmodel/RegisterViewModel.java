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

public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();
    public final LiveData<FirebaseUser> user = _user;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    private final MutableLiveData<Boolean> _registrationComplete = new MutableLiveData<>(false);
    public final LiveData<Boolean> registrationComplete = _registrationComplete;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public void register(String email, String password, String displayName) {
        setLoading(true);
        setErrorMessage(null);
        setRegistrationComplete(false);

        authRepository.register(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = authRepository.getCurrentUser();

                        if (firebaseUser != null) {
                            updateProfileAndSaveToFirestore(firebaseUser, email, displayName);
                        } else {
                            setLoading(false);
                            setErrorMessage("Không thể tạo tài khoản người dùng");
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = authRepository.getLocalizedErrorMessage(task.getException());
                        setErrorMessage(errorMsg);
                    }
                });
    }

    private void updateProfileAndSaveToFirestore(FirebaseUser firebaseUser, String email, String displayName) {
        authRepository.updateUserProfile(firebaseUser, displayName)
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        saveUserToFirestore(firebaseUser, email, displayName);
                    } else {
                        setLoading(false);
                        String errorMsg = authRepository.getLocalizedErrorMessage(profileTask.getException());
                        setErrorMessage(errorMsg);
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String email, String displayName) {
        authRepository.saveUserToFirestore(firebaseUser, email, displayName)
                .addOnCompleteListener(firestoreTask -> {
                    setLoading(false);
                    if (firestoreTask.isSuccessful()) {
                        setUser(firebaseUser);
                        setRegistrationComplete(true);
                    } else {
                        String errorMsg = authRepository.getLocalizedErrorMessage(firestoreTask.getException());
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

    private void setRegistrationComplete(boolean complete) {
        mainHandler.post(() -> _registrationComplete.setValue(complete));
    }
}

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

public class SettingsViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<FirebaseUser> _currentUser = new MutableLiveData<>();

    public final LiveData<FirebaseUser> currentUser = _currentUser;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        refreshUserState();
    }

    public void refreshUserState() {
        FirebaseUser user = authRepository.getCurrentUser();
        setCurrentUser(user);
    }

    public void logout() {
        authRepository.logout();
        setCurrentUser(null);
    }

    private void setCurrentUser(FirebaseUser user) {
        mainHandler.post(() -> _currentUser.setValue(user));
    }
}

package com.haui.notetakingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.haui.notetakingapp.repository.AuthRepository;
import com.haui.notetakingapp.repository.SettingRepository;

public class SettingViewModel extends AndroidViewModel {
    public final LiveData<String> textSize;
    public final LiveData<String> sortBy;
    public final LiveData<String> layout;
    public final LiveData<String> theme;
    private final AuthRepository authRepository;
    private final SettingRepository settingRepository;
    private final MutableLiveData<FirebaseUser> _currentUser = new MutableLiveData<>();
    public final LiveData<FirebaseUser> currentUser = _currentUser;
    private final MutableLiveData<Boolean> _needsRestart = new MutableLiveData<>(false);
    public final LiveData<Boolean> needsRestart = _needsRestart;
    private final MutableLiveData<Boolean> textSizeChanged = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> themeChanged = new MutableLiveData<>(false);
    private final MutableLiveData<SettingsState> _settingsState = new MutableLiveData<>();
    public final LiveData<SettingsState> settingsState = _settingsState;

    public SettingViewModel(@NonNull Application application) {
        super(application);
        // Pass application context to AuthRepository for note deletion during logout
        authRepository = new AuthRepository(application.getApplicationContext());
        settingRepository = new SettingRepository(application.getApplicationContext());

        textSize = settingRepository.textSize;
        sortBy = settingRepository.sortBy;
        layout = settingRepository.layout;
        theme = settingRepository.theme;

        updateSettingsState();

        refreshUserState();
    }

    private void updateSettingsState() {
        SettingsState state = new SettingsState(
                settingRepository.getTextSize(),
                settingRepository.getSortBy(),
                settingRepository.getLayout(),
                settingRepository.getTheme()
        );
        _settingsState.setValue(state);
    }

    public void refreshUserState() {
        FirebaseUser user = authRepository.getCurrentUser();
        setCurrentUser(user);
    }

    public void logout() {
        // This will clear all local notes as well (implemented in AuthRepository)
        authRepository.logout();
        setCurrentUser(null);
    }

    public void saveTextSizeSetting(String textSize) {
        boolean changed = settingRepository.saveTextSize(textSize);
        if (changed) {
            textSizeChanged.setValue(true);
            _needsRestart.setValue(true);
            updateSettingsState();
        }
    }

    public void saveSortBySetting(String sortBy) {
        boolean changed = settingRepository.saveSortBy(sortBy);
        if (changed) {
            _needsRestart.setValue(true);
            updateSettingsState();
        }
    }

    public void saveLayoutSetting(String layout) {
        boolean changed = settingRepository.saveLayout(layout);
        if (changed) {
            _needsRestart.setValue(true);
            updateSettingsState();
        }
    }

    public void saveThemeSetting(String theme) {
        boolean changed = settingRepository.saveTheme(theme);
        if (changed) {
            themeChanged.setValue(true);
            _needsRestart.setValue(true);
            updateSettingsState();
        }
    }

    public boolean isTextSizeChanged() {
        return textSizeChanged.getValue() != null && textSizeChanged.getValue();
    }

    public boolean isThemeChanged() {
        return themeChanged.getValue() != null && themeChanged.getValue();
    }

    public String getInitialTextSize() {
        return settingRepository.getTextSize();
    }

    public String getInitialTheme() {
        return settingRepository.getTheme();
    }

    private void setCurrentUser(FirebaseUser user) {
        _currentUser.postValue(user);
    }

    public static class SettingsState {
        private final String textSize;
        private final String sortBy;
        private final String layout;
        private final String theme;

        public SettingsState(String textSize, String sortBy, String layout, String theme) {
            this.textSize = textSize;
            this.sortBy = sortBy;
            this.layout = layout;
            this.theme = theme;
        }

        public String getTextSize() {
            return textSize;
        }

        public String getSortBy() {
            return sortBy;
        }

        public String getLayout() {
            return layout;
        }

        public String getTheme() {
            return theme;
        }
    }
}

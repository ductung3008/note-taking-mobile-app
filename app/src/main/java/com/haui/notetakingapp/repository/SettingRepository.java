package com.haui.notetakingapp.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SettingRepository {
    private static final String PREFERENCES_FILE = "settings";
    private static final String KEY_TEXT_SIZE = "textSize";
    private static final String KEY_SORT_BY = "sortBy";
    private static final String KEY_LAYOUT = "layout";
    private static final String KEY_THEME = "theme";

    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<String> _textSize = new MutableLiveData<>();
    public final LiveData<String> textSize = _textSize;
    private final MutableLiveData<String> _sortBy = new MutableLiveData<>();
    public final LiveData<String> sortBy = _sortBy;
    private final MutableLiveData<String> _layout = new MutableLiveData<>();
    public final LiveData<String> layout = _layout;
    private final MutableLiveData<String> _theme = new MutableLiveData<>();
    public final LiveData<String> theme = _theme;

    public SettingRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        loadInitialSettings();
    }

    private void loadInitialSettings() {
        _textSize.setValue(sharedPreferences.getString(KEY_TEXT_SIZE, "Vừa"));
        _sortBy.setValue(sharedPreferences.getString(KEY_SORT_BY, "Theo ngày chỉnh sửa"));
        _layout.setValue(sharedPreferences.getString(KEY_LAYOUT, "Xem theo ô lưới"));
        _theme.setValue(sharedPreferences.getString(KEY_THEME, "Sáng"));
    }

    public String getTextSize() {
        return sharedPreferences.getString(KEY_TEXT_SIZE, "Vừa");
    }

    public String getSortBy() {
        return sharedPreferences.getString(KEY_SORT_BY, "Theo ngày chỉnh sửa");
    }

    public String getLayout() {
        return sharedPreferences.getString(KEY_LAYOUT, "Xem theo ô lưới");
    }

    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, "Sáng");
    }

    public boolean saveTextSize(String textSize) {
        String oldValue = getTextSize();
        boolean changed = !oldValue.equals(textSize);

        if (changed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_TEXT_SIZE, textSize);
            editor.apply();
            _textSize.setValue(textSize);
        }

        return changed;
    }

    public boolean saveSortBy(String sortBy) {
        String oldValue = getSortBy();
        boolean changed = !oldValue.equals(sortBy);

        if (changed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SORT_BY, sortBy);
            editor.apply();
            _sortBy.setValue(sortBy);
        }

        return changed;
    }

    public boolean saveLayout(String layout) {
        String oldValue = getLayout();
        boolean changed = !oldValue.equals(layout);

        if (changed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_LAYOUT, layout);
            editor.apply();
            _layout.setValue(layout);
        }

        return changed;
    }

    public boolean saveTheme(String theme) {
        String oldValue = getTheme();
        boolean changed = !oldValue.equals(theme);

        if (changed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_THEME, theme);
            editor.apply();
            _theme.setValue(theme);
        }

        return changed;
    }
}

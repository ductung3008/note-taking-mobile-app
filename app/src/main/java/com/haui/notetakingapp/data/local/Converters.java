package com.haui.notetakingapp.data.local;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromList(List<String> list) {
        return list != null ? String.join(",", list) : null;
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }
}

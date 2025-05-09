package com.haui.notetakingapp.data.local;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haui.notetakingapp.data.local.entity.CheckListItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromList(List<String> list) {
//        return list != null ? String.join(",", list) : null;
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null || value.isEmpty()) {
//            return null;
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }

    @TypeConverter
    public static String fromCheckListItemList(List<CheckListItem> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<CheckListItem> toCheckListItemList(String value) {
        try {
            Type listType = new TypeToken<List<CheckListItem>>(){}.getType();
            return new Gson().fromJson(value, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // hoặc null
        }
    }

}

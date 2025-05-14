package com.haui.notetakingapp.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class TextHighlighter {
    public static SpannableString highlightText(String text, String searchTerm, int highlightColor) {
        if (text == null || searchTerm == null || searchTerm.isEmpty()) {
            return new SpannableString(text != null ? text : "");
        }

        SpannableString spannableString = new SpannableString(text);

        String textLower = text.toLowerCase();
        String searchTermLower = searchTerm.toLowerCase();

        int startPos = 0;
        int searchTermLength = searchTermLower.length();

        while (startPos < textLower.length()) {
            int foundPos = textLower.indexOf(searchTermLower, startPos);
            if (foundPos == -1) {
                break;
            }

            ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(highlightColor);
            spannableString.setSpan(foregroundSpan, foundPos, foundPos + searchTermLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            startPos = foundPos + searchTermLength;
        }

        return spannableString;
    }

    public static SpannableString highlightText(String text, String searchTerm) {
        return highlightText(text, searchTerm, Color.RED);
    }
}

package com.haui.notetakingapp.data.local.entity;

import java.io.Serializable;

public class CheckListItem implements Serializable {
    private String text;
    private boolean isChecked;

    public CheckListItem() {

    }

    public CheckListItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    public String getText() {
        return text;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
    public void setText(String text) {
        this.text = text;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

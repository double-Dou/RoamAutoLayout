package com.bohui.autolayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PreviewView extends FrameLayout {

    private Context mContext;
    private LocationRect location;
    public boolean isChangedL = false;//是否改变位置

    public PreviewView(@NonNull Context context) {
        this(context, null);
    }

    public PreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setLocation(LocationRect location) {
        this.location = location;
    }

    public LocationRect getLocation() {
        return location;
    }
}

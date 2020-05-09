package com.bohui.autolayout;

import android.graphics.Rect;

public class LocationRect {

    private Rect rect;
    private int location;

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}

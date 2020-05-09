package com.bohui.autolayout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoLayoutView extends FrameLayout {

    private Context mContext;
    private float rawDownX;
    private float rawDownY;
    private float beginL;
    private float beginT;
    private Map<Integer, LocationRect> locationMap = new HashMap<>();
    private ArrayList<LocationRect> rectList = new ArrayList<>();
    private PreviewView currentView;

    public AutoLayoutView(@NonNull Context context) {
        this(context, null);
    }

    public AutoLayoutView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                rawDownX = event.getRawX();
                rawDownY = event.getRawY();
                currentView = (PreviewView) getTouchTarget(this, (int) event.getRawX(), (int) event.getRawY());
                if (currentView == null) return super.onTouchEvent(event);
                beginL = currentView.getX();
                beginT = currentView.getY();
                currentView.bringToFront();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentView == null) return super.onTouchEvent(event);
                final float xDistance = event.getRawX() - rawDownX;
                final float yDistance = event.getRawY() - rawDownY;
                float l = beginL + xDistance;
                float t = beginT + yDistance;
                if (xDistance != 0 && yDistance != 0) {
                    if (l < 0) l = 0;
                    if (t < 0) t = 0;
                    if (l > ((FrameLayout) currentView.getParent()).getWidth() - currentView.getWidth())
                        l = ((FrameLayout) currentView.getParent()).getWidth() - currentView.getWidth();
                    currentView.setX(l);
                    currentView.setY(t);
                }
                checkCurrentViewLocation((int) event.getX(), (int) event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                if (currentView == null) return super.onTouchEvent(event);
                if (currentView.getY() > ((FrameLayout) currentView.getParent()).getHeight() - currentView.getHeight() / 2) {
                    //删除
                    removeView(currentView);
                }
                startTranslateAnimation(currentView);
                break;
        }
        return super.onTouchEvent(event);

    }

    //移动画面时动态布局
    private void checkCurrentViewLocation(int locationX, int locationY) {
        if (currentView == null) return;
        LocationRect oldLocation = currentView.getLocation();
        LocationRect location = null;
        for (int i = 0; i < rectList.size(); i++) {
            if (rectList.get(i) != oldLocation && rectList.get(i).getRect().contains(locationX, locationY)) {
                location = rectList.get(i);
                currentView.setLocation(location);
                break;
            }
        }
        if (location != null && oldLocation != null) {

            int changeState = 1;
            if (location.getLocation() > oldLocation.getLocation()) {
                changeState = 2;
            } else if (location.getLocation() < oldLocation.getLocation()) {
                changeState = 0;
            }

            for (int i = 0; i < getChildCount(); i++) {
                View subView = getChildAt(i);
                if (subView instanceof PreviewView && subView != currentView) {
                    boolean isChange = false;
                    if (changeState == 2) {
                        if (((PreviewView) subView).getLocation().getLocation() > oldLocation.getLocation()
                                && ((PreviewView) subView).getLocation().getLocation() <= location.getLocation()) {
                            if (!locationMap.containsKey(((PreviewView) subView).getLocation().getLocation() - 1))
                                continue;
                            ((PreviewView) subView).setLocation(locationMap.get(((PreviewView) subView).getLocation().getLocation() - 1));
                            isChange = true;
                        }
                    } else if (changeState == 0) {
                        if (((PreviewView) subView).getLocation().getLocation() < oldLocation.getLocation()
                                && ((PreviewView) subView).getLocation().getLocation() >= location.getLocation()) {
                            if (!locationMap.containsKey(((PreviewView) subView).getLocation().getLocation() + 1))
                                continue;
                            ((PreviewView) subView).setLocation(locationMap.get(((PreviewView) subView).getLocation().getLocation() + 1));
                            isChange = true;
                        }
                    }
                    if (isChange) {
                        startTranslateAnimation((PreviewView) subView);
                    }
                }
            }
        }
    }

    //执行位移缩放动画
    private void startTranslateAnimation(final PreviewView target) {
        target.isChangedL = false;
        final float width = target.getLocation().getRect().right - target.getLocation().getRect().left;
        final float height = target.getLocation().getRect().bottom - target.getLocation().getRect().top;
        float scaleX = width / (target.getLayoutParams().width == 0 ? width : target.getLayoutParams().width);
        float scaleY = height / (target.getLayoutParams().height == 0 ? height : target.getLayoutParams().height);
        target.animate().x(target.getLocation().getRect().left).y(target.getLocation().getRect().top)
//                .scaleX(scaleX - 0 < 0.0001 ? 1 : scaleX)
//                .scaleY(scaleY - 0 < 0.0001 ? 1 : scaleX)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!target.isChangedL){
                            target.isChangedL = true;
                            LayoutParams subLp = (LayoutParams) target.getLayoutParams();
                            subLp.width = (int) width;
                            subLp.height = (int) height;
                            target.setLayoutParams(subLp);
                        }
                    }
                }).start();
    }

    @Override
    public void addView(View child) {
        addView(child, child.getLayoutParams());
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() >= 6) return;
        if (params instanceof FrameLayout.LayoutParams && child instanceof PreviewView) {
            Rect rect = new Rect();
            rect.left = ((LayoutParams) params).leftMargin;
            rect.top = ((LayoutParams) params).topMargin;
            rect.right = ((LayoutParams) params).leftMargin + params.width;
            rect.bottom = ((LayoutParams) params).topMargin + params.height;
            LocationRect locationRect = new LocationRect();
            locationRect.setLocation(getChildCount());
            locationRect.setRect(rect);
            locationMap.put(getChildCount(), locationRect);
            rectList.add(locationRect);
            ((PreviewView) child).setLocation(locationRect);
            super.addView(child, params);
            reLayoutRect();
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        if (view instanceof PreviewView) {
            locationMap.remove(((PreviewView) view).getLocation());
            rectList.remove(((PreviewView) view).getLocation());
            reLayoutRect();
        }
    }

    //增删画面时重新计算位置坐标
    private void reLayoutRect() {
        switch (rectList.size()) {
            case 1:
                layoutOne();
                break;
            case 2:
                layoutTwo();
                break;
            case 3:
                layoutThree();
                break;
            case 4:
                layoutFour();
                break;
            case 5:
                layoutFive();
                break;
            case 6:
                layoutSix();
                break;
        }
        reLayoutView();

    }

    //根据自身绑定的位置坐标重置画面位置
    private void reLayoutView() {
        for (int i = 0; i < getChildCount(); i++) {
            View subView = getChildAt(i);
            if (subView instanceof PreviewView) {
                FrameLayout.LayoutParams subLp = (LayoutParams) subView.getLayoutParams();
                subLp.setMargins(((PreviewView) subView).getLocation().getRect().left,
                        ((PreviewView) subView).getLocation().getRect().top, 0, 0);
                subLp.width = ((PreviewView) subView).getLocation().getRect().right -
                        ((PreviewView) subView).getLocation().getRect().left;
                subLp.height = ((PreviewView) subView).getLocation().getRect().bottom -
                        ((PreviewView) subView).getLocation().getRect().top;
                startTranslateAnimation((PreviewView) subView);
                subView.setLayoutParams(subLp);
            }
        }
    }

    private void layoutOne() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            locationRect.getRect().left = 0;
            locationRect.getRect().top = 0;
            locationRect.getRect().right = getWidth();
            locationRect.getRect().bottom = getHeight();
        }
    }

    private void layoutTwo() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            locationRect.getRect().left = i % 2 * getWidth() / 2;
            locationRect.getRect().top = i / 2 * getHeight() / 2;
            locationRect.getRect().right = locationRect.getRect().left + getWidth() / 2;
            locationRect.getRect().bottom = i / 2 * getHeight() / 2 + getHeight();
        }
    }

    private void layoutThree() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            if (i == 0) {
                locationRect.getRect().left = 0;
                locationRect.getRect().top = 0;
                locationRect.getRect().right = getWidth();
                locationRect.getRect().bottom = getHeight() / 2;
            } else {
                locationRect.getRect().left = i % 2 * getWidth() / 2;
                locationRect.getRect().top = getHeight() / 2;
                locationRect.getRect().right = i % 2 * getWidth() / 2 + getWidth() / 2;
                locationRect.getRect().bottom = getHeight();
            }
        }
    }

    private void layoutFour() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            locationRect.getRect().left = i % 2 * getWidth() / 2;
            locationRect.getRect().top = i / 2 * getHeight() / 2;
            locationRect.getRect().right = locationRect.getRect().left + getWidth() / 2;
            locationRect.getRect().bottom = locationRect.getRect().top + getHeight() / 2;
        }
    }

    private void layoutFive() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            if (i < 2) {
                locationRect.getRect().left = i % 2 * getWidth() / 2;
                locationRect.getRect().top = 0;
                locationRect.getRect().right = locationRect.getRect().left + getWidth() / 2;
                locationRect.getRect().bottom = getHeight() / 2;
            } else {
                locationRect.getRect().left = (i + 1) % 3 * getWidth() / 3;
                locationRect.getRect().top = getHeight() / 2;
                locationRect.getRect().right = locationRect.getRect().left + getWidth() / 3;
                locationRect.getRect().bottom = getHeight();
            }
        }
    }

    private void layoutSix() {
        locationMap.clear();
        for (int i = 0; i < rectList.size(); i++) {
            LocationRect locationRect = rectList.get(i);
            locationRect.setLocation(i);
            locationMap.put(i, locationRect);
            locationRect.getRect().left = i % 3 * getWidth() / 3;
            locationRect.getRect().top = i / 3 * getHeight() / 2;
            locationRect.getRect().right = locationRect.getRect().left + getWidth() / 3;
            locationRect.getRect().bottom = locationRect.getRect().top + getHeight() / 2;
        }
    }

    //获取触摸点点击的预览view
    private FrameLayout getTouchTarget(FrameLayout view, int x, int y) {
        FrameLayout targetView = null;
        for (int i = view.getChildCount(); i > -1; i--) {
            View child = view.getChildAt(i);
            if (child instanceof FrameLayout && isTouchPointInView(child, x, y)) {
                targetView = (FrameLayout) child;
                break;
            }
        }
        return targetView;
    }

    //(x,y)是否在view的区域内
    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }
}

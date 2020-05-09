package com.bohui.autolayout;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private AutoLayoutView layoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutView = findViewById(R.id.layout_view);

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviewView subview = new PreviewView(MainActivity.this);
                FrameLayout.LayoutParams subLp = new FrameLayout.LayoutParams(layoutView.getWidth() / 3, layoutView.getHeight() / 2);
                subLp.setMargins(layoutView.getWidth() / 3, layoutView.getHeight() / 2, 0, 0);
                subview.setBackgroundColor(getRandomColor());
                layoutView.addView(subview, subLp);
            }
        });

        layoutView.post(new Runnable() {
            @Override
            public void run() {
                layoutSubviews();
            }
        });
    }

    private void layoutSubviews() {
        for (int i = 0; i < 6; i++) {
            PreviewView subview = new PreviewView(this);
            FrameLayout.LayoutParams subLp = new FrameLayout.LayoutParams(layoutView.getWidth() / 3, layoutView.getHeight() / 2);
            subLp.setMargins(i % 3 * layoutView.getWidth() / 3, i / 3 * layoutView.getHeight() / 2, 0, 0);
            subview.setBackgroundColor(getRandomColor());
            layoutView.addView(subview, subLp);
        }
    }

    private int getRandomColor(){
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }
}

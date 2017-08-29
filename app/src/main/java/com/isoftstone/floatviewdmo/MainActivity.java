package com.isoftstone.floatviewdmo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.isoftstone.floatlibrary.view.FloatViewImpl;
import com.isoftstone.floatlibrary.widget.FloatViewLayout;

public class MainActivity extends AppCompatActivity {
    private FloatViewLayout floatViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatViewLayout = (FloatViewLayout) findViewById(R.id.layout_float);
        floatViewLayout.setmFloatView(new FloatViewImpl() {
            @Override
            public View createFloatView() {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_float_view, null);
                return view;
            }

            @Override
            public int setFloatViewSideOffset() {
                return super.setFloatViewSideOffset();
            }

            @Override
            public boolean setEnableBackground() {
                return false;
            }
        });
    }
}

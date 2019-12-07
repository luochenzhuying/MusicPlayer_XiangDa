package com.music.xiangdamuxic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.music.xiangdamuxic.utils.ActivityManager;

public class SuperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
    }


    @Override
    protected void onDestroy() {
//        ActivityManager.getInstance().addActivity(this);
        super.onDestroy();
    }
}

package com.music.xiangdamuxic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.fragment.MainFragment;
import com.music.xiangdamuxic.fragment.MessageFragment;
import com.music.xiangdamuxic.fragment.MineFragment;
import com.music.xiangdamuxic.utils.ActivityManager;
import com.music.xiangdamuxic.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;


public class MainActivity extends AppCompatActivity {
    //记录当前返回键按下时间撮
    private long mPressedTime = 0;

    //底部TabLayout控件
    private SpaceTabLayout mBottomTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //状态栏透明化
        StatusBarUtil.setTransparent(this);

        //用于存储主页面的3个Fragment
        List<Fragment> fragmentList = new ArrayList<>();
        //歌曲播放主界面
        fragmentList.add(new MainFragment());
        //消息界面
        fragmentList.add(new MessageFragment());
        //个人中心界面
        fragmentList.add(new MineFragment());


        ViewPager viewPager = findViewById(R.id.Main_viewPager);
        //设置viewPager预加载fragment的数量（数量存储于常量池中）
        viewPager.setOffscreenPageLimit(Constant.MAINFRAGMENTPAGENUM);

        //获取底部TabLayout
        mBottomTabLayout = findViewById(R.id.mainActivity_bottomSliderTabLayout);

        //初始化底部TabLayout
        mBottomTabLayout.initialize(viewPager, getSupportFragmentManager(),
                fragmentList, savedInstanceState);
    }


    @Override
    //底部TabLayout数据恢复
    protected void onSaveInstanceState(Bundle outState) {
        mBottomTabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //获取第一次按键时间
        long mNowTime = System.currentTimeMillis();

        //比较两次按键时间差
        if ((mNowTime - mPressedTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_LONG).show();
            mPressedTime = mNowTime;
        } else {
            //退出程序（清除所有的活动）
            ActivityManager.getInstance().exit();
        }
    }
}

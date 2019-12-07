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
import com.music.xiangdamuxic.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;


public class MainActivity extends SuperActivity {
    /**
     * 记录当前返回键按下的时间撮
     * 默认为0，保证第一次按下后能对当前时间进行记录
     */
    private long mPressedTime = 0;


    /**
     * 底部TabLayout控件
     * 可以存储fragment，默认为3个
     */
    private SpaceTabLayout mBottomTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //状态栏透明化
        StatusBarUtil.setTransparent(this);

        //获取fragment列表，包含所以fragment
        List<Fragment> fragmentList = getFragmentList();

        //获取viewPager控件
        ViewPager viewPager = findViewById(R.id.Main_viewPager);

        //设置viewPager预加载fragment的数量（数量存储于常量池中）
        viewPager.setOffscreenPageLimit(Constant.MAINFRAGMENTPAGENUM);

        //获取底部TabLayout
        mBottomTabLayout = findViewById(R.id.mainActivity_bottomSliderTabLayout);

        //初始化底部TabLayout
        mBottomTabLayout.initialize(viewPager, getSupportFragmentManager(),
                fragmentList, savedInstanceState);


        //获取用户名
        String show_name = Utils.getString(MainActivity.this, Constant.userNameSPKey, "")+" 欢迎你！";

        //欢迎语句
        Toast.makeText(MainActivity.this,show_name,Toast.LENGTH_LONG).show();
    }


    /**
     * 获取所有fragment
     * 包含MainFragment（歌曲播放主界面），MessageFragment（消息界面），MineFragment（个人中心界面）
     *
     * @return 全体fragment List
     */
    private List<Fragment> getFragmentList() {
        //用于存储主页面的3个Fragment
        List<Fragment> fragmentList = new ArrayList<>();

        //歌曲播放主界面
        fragmentList.add(new MainFragment());

        //消息界面
        fragmentList.add(new MessageFragment());

        //个人中心界面
        fragmentList.add(new MineFragment());


        return fragmentList;
    }


    /**
     * 页面意外关闭数据恢复
     * 主要对底部TabLayout进行恢复
     *
     * @param outState 保存的数据，用于还原页面
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //数据还原
        mBottomTabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * 返回键处理
     * 主要实现了再按一次退出程序的功能
     * 记录连续两次的时间戳，如果小于2S，进行退出。
     * 注意：此退出清理完所以活动。
     */
    @Override
    public void onBackPressed() {
        //获取第一次按键时间
        long mNowTime = System.currentTimeMillis();

        //比较两次按键时间差
        if ((mNowTime - mPressedTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_LONG).show();

            //记录当前时间戳，为下次计算做准备
            mPressedTime = mNowTime;
        } else {

            //退出程序（清除所有的活动）
            ActivityManager.getInstance().exit();
        }
    }
}

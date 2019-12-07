package com.music.xiangdamuxic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;

import com.hanks.htextview.scale.ScaleTextView;
import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.Utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class WelcomePage extends SuperActivity {

    /**
     * 记录问候语播放的位置
     */
    int index = 0;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcomepage);
        //状态栏透明化
        StatusBarUtil.setTransparent(WelcomePage.this);

        //获取开始时间
        Date date = new Date();
        final long startTime = date.getTime();

        //配置页面渐变动画
        initAnimation();

        //设置动态字段
        initScaleTextView();

        //获取结束时间
        final long endTime = date.getTime();

        //进行时间计算，证页面的展示时间一定
        calculateShowTime(startTime, endTime);


    }

    /**
     * 保证页面的展示时间一定（5S），
     * 使用了sleep，得在子线程进行操作，防止主线程页面卡顿。
     * 时间计算为进入此函数之前的时间戳减去页面进入的时间戳，再用5减去之前计算的差，为子线程睡眠时间。
     * 苏醒后跳转新页面，
     * 再延迟3S销毁该activity。
     *
     * @param startTime 刚进入页面的时间戳
     * @param endTime   进入此函数之前的时间戳
     */
    private void calculateShowTime(final long startTime, final long endTime) {

        //开启子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //子线程休眠未满5s的时间
                    Thread.sleep((5000) - (endTime - startTime));

                    //意图初始
                    Intent intent =null;

                    //判断是否已经登录，如果已经登录，则跳转主页
                    //否则跳转登录界面
                    if(!Utils.getBool(WelcomePage.this,Constant.isLogOn,false)){
                        //未登录
                        intent = new Intent(WelcomePage.this, LoginActivity.class);
                    }else{
                        //已登录
                        intent = new Intent(WelcomePage.this, MainActivity.class);
                    }

                    //取消动态字体任务
                    timer.cancel();

                    //开启意图
                    startActivity(intent);

                    //跳转动画（向左滑动效果）
                    overridePendingTransition(R.anim.next_in, R.anim.next_out);

                    //延迟销毁WelcomePage
                    Thread.sleep(3000);
                    WelcomePage.this.finish();

                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    /**
     * 初始化动态字段
     * 1.5S更换一次
     */
    private void initScaleTextView() {
        //获取动态字体控件
        final ScaleTextView scaleTextView = findViewById(R.id.wlecomePage_scaleTextView);

        //设置任务内容
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //定时变换字，字体存于Constant常量池中
                scaleTextView.animateText(Constant.GREETINGSENTENCES[(index++) % Constant.GREETINGSENTENCES.length]);
            }
        };

        //开启任务，1.5S更换
        timer = new Timer();
        timer.schedule(timerTask, 0, 1500);
    }

    /**
     * 配置页面渐变动画
     * 由完全透明至完全不透明，渐变时间为3S
     */
    private void initAnimation() {
        //1.0 不透明，0.0完全透明

        //配置渐变动画
        AlphaAnimation ac = new AlphaAnimation(0.0f, 1.0f);

        //设置动画时间
        ac.setDuration(3000);

        //给整个Welcome界面设置渐变动画
        findViewById(R.id.wlecomePage_relativeLayout).startAnimation(ac);
    }

}



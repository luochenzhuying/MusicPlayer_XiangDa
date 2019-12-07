package com.music.xiangdamuxic;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.utils.ActivityManager;
import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.ParseTime;
import com.music.xiangdamuxic.utils.Utils;
import com.zcy.rotateimageview.RotateImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.zhengken.lyricview.LyricView;

public class PlayerActivity extends SuperActivity implements View.OnClickListener {

    private ServiceConnection conn;
    private static PlayerService.MyBinder mb; //中间人
    private int isOnPlay = 0;
    static SeekBar seekBar = null;
    static LyricView mLyricView = null;
    private static TextView endTime;
    private static TextView startTime;

    ImageView playButton = null;
    private View view1, view2;
    private ViewPager viewPager;  //对应的viewPager

    private List<View> viewList;//view数组


    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //更新进度
            if (msg.what == 0) {
                //获取数据
                Bundle data = msg.getData();
                int duration = data.getInt("duration");
                int currentPosition = data.getInt("currentPosition");

                //设定进度条最大进度
                seekBar.setMax(duration);
                //设定歌曲结束时间(即歌曲有多少分钟)
                endTime.setText(ParseTime.msToString(duration));
                //设定当前播放的位置
                startTime.setText(ParseTime.msToString(currentPosition));
                //设定进度条的位置
                seekBar.setProgress(currentPosition);

                //歌词进度
                mLyricView.setCurrentTimeMillis(currentPosition);

                //判断是否到达结尾，到达的话进入下一首
                if (duration - 2000 <= currentPosition) {
                    //调用服务内的下一首歌曲
                    mb.next();
                }


            } else if (msg.what == 1) {
                //切换歌曲
                //更新正在播放的位置(list)
                playingNum = msg.getData().getInt(Constant.PLAYINGNUM);

                //设定新歌词
                mLyricView.setLyricFile(songToLrc(listOfSong.get(playingNum)));

                //更新页面信息(歌曲名字，歌曲背景，歌手信息)
                upTextViewData();

            }
        }
    };

    /**
     * 更新歌曲名字，歌曲背景，歌手信息
     */
    private static void upTextViewData() {
        String name = listOfSong.get(playingNum).getName();
        if (name.contains("海阔")) {
            background.setImageResource(R.mipmap.beyond1);
            music_title.setText("海阔天空");
            music_artist.setText("Beyond");
        } else if (name.contains("再见只是陌生人")) {
            background.setImageResource(R.mipmap.zxy1);
            music_title.setText("再见只是陌生人");
            music_artist.setText("庄心妍");
        } else {
            background.setImageResource(R.mipmap.defbackground);
            String name1 = listOfSong.get(playingNum).getName();
            name1 = name1.substring(0, name1.length() - 4);
            music_title.setText(name1);
            music_artist.setText("未知歌手");
        }
    }

    private RotateImageView rotate_imageview;
    private static ImageView background;
    private static TextView music_title;
    private static TextView music_artist;
    private static int playingNum;
    private static LinkedList<File> listOfSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        //状态栏透明化
        StatusBarUtil.setColor(PlayerActivity.this, Color.parseColor("#533976"), 112);

        //获取歌曲列表
        listOfSong = Utils.getBeanFromSp(PlayerActivity.this, Constant.MUSICLISTKEY);

        //获取正在播放的序号
        playingNum = Utils.getInt(PlayerActivity.this, Constant.PLAYINGNUM, 0);

        background = findViewById(R.id.playActivity_background);
        music_title = findViewById(R.id.playActivity_music_title);
        music_artist = findViewById(R.id.playActivity_music_artist);
        viewPager = findViewById(R.id.container);

        //将歌词和旋转图片存入ViewList
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.rotateimageview, null);
        view2 = inflater.inflate(R.layout.lyricview_layout, null);
        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);

        //中央viewPager的适配器
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new ZoomInTransformer());

        seekBar = findViewById(R.id.music_seek_bar);
        //获取歌词View
        mLyricView = viewList.get(1).findViewById(R.id.playActivity_lyric_view);

        //歌词装填
        File f = songToLrc(listOfSong.get(playingNum));
        mLyricView.setLyricFile(f);


        findViewById(R.id.playActivity_preSong).setOnClickListener(this);
        findViewById(R.id.playActivity_pauseSong).setOnClickListener(this);
        findViewById(R.id.playActivity_nextSong).setOnClickListener(this);

        playButton = findViewById(R.id.playActivity_pauseSong);
        endTime = findViewById(R.id.end_time);
        startTime = findViewById(R.id.start_time);

        //旋转view的设定
        rotate_imageview = viewList.get(0).findViewById(R.id.rotate_imageview);
        //速度
        rotate_imageview.setSpeed(50);
        //停止旋转
        rotate_imageview.setRotate(false);

        //设定服务意图
        Intent intent = new Intent(this, PlayerService.class);

        //服务开启，获取服务内的功能，存储于mb中
        conn = new ServiceConnection() {
            // 服务断开
            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            // 服务连接
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mb = (PlayerService.MyBinder) service;
            }
        };

        // 绑定服务
        bindService(intent, conn, BIND_AUTO_CREATE);

        //给歌词设置监听器(用于监听用户调整歌词位置)
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(final long progress, String content) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //防止抖动，得先暂停
                        mb.pause();
                        //设定播放进度条的位置
                        seekBar.setProgress((int) progress);
                        //将服务内的正在播放的音乐滑动至当前进度
                        mb.updataMediaPlay((int) progress);
                        //设定歌词的进度位置
                        mLyricView.setCurrentTimeMillis(progress);
                        //歌曲继续播放
                        mb.start();
                        //设定为暂停按钮(现在正在播放)
                        playButton.setImageResource(R.drawable.btn_pause_selector);
                        //歌曲正在播放标志(用于按钮图标的设定，如暂停与播放按钮)
                        isOnPlay = 1;
                        //旋转图片设定旋转状态
                        rotate_imageview.setRotate(true);






                    }
                });

            }

        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //防止抖动，得先暂停
                mb.pause();
                //设定播放进度条的位置
                seekBar.setProgress(seekBar.getProgress());
                //将服务内的正在播放的音乐滑动至当前进度
                mb.updataMediaPlay(seekBar.getProgress());
                //设定歌词的进度位置
                mLyricView.setCurrentTimeMillis(seekBar.getProgress());
                //歌曲继续播放
                mb.start();
                //设定为暂停按钮(现在正在播放)
                playButton.setImageResource(R.drawable.btn_pause_selector);
                //歌曲正在播放标志(用于按钮图标的设定，如暂停与播放按钮)
                isOnPlay = 1;
                //旋转图片设定旋转状态
                rotate_imageview.setRotate(true);
            }
        });




        //播放模式的设定
        final ImageView modeImage = findViewById(R.id.btn_mode);
        final int mode = Utils.getInt(PlayerActivity.this, Constant.MODE, 0);
        switch (mode) {
            case 0:
                //全部循环
                modeImage.setImageResource(R.drawable.btn_all_repeat_selector);
                break;
            case 1:
                //单曲循环
                modeImage.setImageResource(R.drawable.btn_one_repeat_selector);
                break;
            case 2:
                //随机播放
                modeImage.setImageResource(R.drawable.btn_shuffle_selector);
                break;
        }

        //模式按钮监听器设定
        modeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前模式
                int mode1 = Utils.getInt(PlayerActivity.this, Constant.MODE, 0);

                //下一个模式
                mode1++;
                mode1 %= 3;

                //存储SP里
                Utils.putInt(PlayerActivity.this, Constant.MODE, mode1);

                //图片更换
                switch (mode1) {
                    case 0:
                        modeImage.setImageResource(R.drawable.btn_all_repeat_selector);
                        break;
                    case 1:
                        modeImage.setImageResource(R.drawable.btn_one_repeat_selector);
                        break;
                    case 2:
                        modeImage.setImageResource(R.drawable.btn_shuffle_selector);
                        break;
                }
            }
        });

        //更新歌曲名字，歌曲背景，歌手信息
        upTextViewData();

        //将此活动加入总活动管理
        ActivityManager.getInstance().addActivity(this);
    }

    /**
     * 根据歌曲文件，该后缀为lrc
     * @param file
     * @return
     */
    private static File songToLrc(File file) {
        String string = file.getAbsolutePath().toString();
        string = string.substring(0, string.length() - 3) + "lrc";
        File f = new File(string);
        return f;
    }

    /**
     * 点击事件处理
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.playActivity_preSong:
                //点击了上一首按钮
                //歌曲设定上一首
                mb.pre();
                break;

            case R.id.playActivity_pauseSong:
                //点击了暂停，播放按钮
                //0表示没有播放，点击播放后将图标置成暂停状态
                if (isOnPlay == 0) {
                    //播放
                    isOnPlay = 1;
                    mb.start();
                    playButton.setImageResource(R.drawable.btn_pause_selector);
                    rotate_imageview.setRotate(true);
                } else {
                    //暂停
                    isOnPlay = 0;
                    mb.pause();
                    playButton.setImageResource(R.drawable.btn_play_selector);
                    rotate_imageview.setRotate(false);
                }
                break;
            case R.id.playActivity_nextSong:
                //点击了下一首按钮
                //歌曲设定下一首
                mb.next();
                break;
            default:
                break;
        }
    }

    //在Activity销毁时,调用解绑方法,
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        this.unbindService(conn);
    }


    /**
     * 返回按钮按下
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
        //        super.onBackPressed();
    }

    /**
     * 左上角返回按钮按下
     * @param view
     */
    public void back(View view) {
        onBackPressed();
    }


    /**
     * 用于音乐列表点击了当前非正在播放的歌曲，需要将其歌曲切换
     *
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        //获取播放歌曲在列表中的位置
        int anInt = Utils.getInt(this, Constant.PLAYINGNUM, 0);

        //如果选定歌曲和当前不一样，需要进行更换
        if (anInt != playingNum) {
            playingNum = anInt;

            Utils.putInt(this, Constant.PLAYINGNUM, playingNum);

            if (mb != null) {
                try {
                    //mb存在
                    mb.playNumSong(playingNum);
                } catch (IOException e) {
//                    System.out.println("3333333333 haule  ");
                }
            }
        }
    }
}

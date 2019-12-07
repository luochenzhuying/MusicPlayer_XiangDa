package com.music.xiangdamuxic;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service {

    //媒体播放器
    private MediaPlayer mp;

    //当前播放的音乐位于list的位置
    private int playingNum;

    //音乐列表
    private LinkedList<File> listOfSong;

    //中间人
    IBinder b = new MyBinder();

    //定时器，用于持续播放音乐，调整进度
    Timer timer = null;

    // 自定义中间人对象,向外提供服务内的功能
    public class MyBinder extends Binder {
        //开始播放
        public void start() {
            PlayerService.this.start();
            PlayerService.this.updataSeekbar();
        }

        //暂停
        public void pause() {
            PlayerService.this.pause();
            if (timer != null)
                timer.cancel();
        }

        //更新
        public void updataMediaPlay(int i) {
            PlayerService.this.updataMediaPlay(i);
        }

        //下一首
        public void next() {
            PlayerService.this.next();
        }

        //上一首
        public void pre() {
            PlayerService.this.pre();
        }

        //播放指定位置的歌曲
        public void playNumSong(int x) throws IOException {
            PlayerService.this.playNumSong(x);
        }
    }

    /**
     * 调用服务绑定时调用，返回中间人对象
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // 设置初始音乐文件路径,正在播放的
        try {
            //服务调用，设定媒体对象的资源位置
            mp.setDataSource(listOfSong.get(playingNum).getAbsolutePath()); // 给播放器设置路径
            //预备(初始化)
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }

    /**
     * 服务解绑(需要将媒体对象释放)
     *
     * @param conn
     */
    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        if (mp != null) {
            //从内存里清除
            mp.release();
            mp = null;
        }
    }

    /**
     * 服务启动，创建媒体对象 MediaPlayer
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //创建媒体对象
        mp = new MediaPlayer();
        //获取歌曲列表
        listOfSong = Utils.getBeanFromSp(this, Constant.MUSICLISTKEY);
        //获取正在播放的序号
        playingNum = Utils.getInt(this, Constant.PLAYINGNUM, 0);
    }

    /**
     * 服务内部的开始播放函数
     */
    public void start() {
        mp.start();
    }

    /**
     * 服务内部的暂停播放函数
     */
    public void pause() {
        mp.pause();
    }

    /**
     * 歌曲不断播放，不断发消息至PlayerActivity进行视图更新
     * 服务内部的更新进度条函数(发送消息至PlayerActivity，PlayerActivity对其进行更新)
     */
    public void updataSeekbar() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int duration = mp.getDuration();
                int currentPosition = mp.getCurrentPosition();

                Message msg = Message.obtain();
                msg.what = 0;

                Bundle bundle = new Bundle();
                bundle.putInt("duration", duration);
                bundle.putInt("currentPosition", currentPosition);
                msg.setData(bundle);
                PlayerActivity.handler.sendMessage(msg);
            }
        };

        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    /**
     * 更新媒体播放器的进度(歌曲进度)
     *
     * @param process
     */
    public void updataMediaPlay(int process) {
        mp.seekTo(process);
    }

    /**
     * 下一首
     */
    public void next() {
        try {
            int mode = Utils.getInt(this, Constant.MODE, 0);
            switch (mode) {
                case 0:
                    //全部循环
                    playNumSong((++playingNum) % listOfSong.size());
                    break;
                case 1:
                    //单曲循环
                    playNumSong(playingNum);
                    break;
                case 2:
                    //随机播放
                    int x = (int) (Math.random() * (listOfSong.size() - 1));
                    playNumSong(x);
                    break;
            }

        } catch (IOException e) {
        }
    }

    /**
     * 播放指定位置(list中的位置)的歌曲
     *
     * @param x 位置
     * @throws IOException
     */
    private void playNumSong(int x) throws IOException {
        //定时发送消息任务(PlayerActivity更新视图)取消
        if (timer != null) {
            timer.cancel();
        }
        //存储当前播放位置
        Utils.putInt(this, Constant.PLAYINGNUM, x);
        //媒体播放器停止及其释放
        mp.stop();
        mp.release();
        mp = null;

        //创建新的媒体播放器
        mp = new MediaPlayer();
        mp.setDataSource(listOfSong.get(x).getAbsolutePath()); // 给播放器设置路径
        mp.prepare();

        //创建消息，使得PlayerActivity进行视图更新
        Message msg = Message.obtain();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.PLAYINGNUM, x);
        msg.setData(bundle);
        PlayerActivity.handler.sendMessage(msg);

        //歌曲播放
        start();

        //开启定时任务，视图开始更新
        updataSeekbar();

    }

    public void pre() {
        try {
            int mode = Utils.getInt(this, Constant.MODE, 0);
            switch (mode) {
                case 0:
                    //全部循环
                    playNumSong((--playingNum + listOfSong.size()) % listOfSong.size());
                    break;
                case 1:
                    //单曲循环
                    playNumSong(playingNum);
                    break;
                case 2:
                    //随机播放
                    int x = (int) (Math.random() * (listOfSong.size() - 1));
                    playNumSong(x);
                    break;
            }

        } catch (IOException e) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

}

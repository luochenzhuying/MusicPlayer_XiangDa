package com.music.xiangdamuxic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.Utils;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SearchMusicActivity extends SuperActivity {

    //搜索图标
    private ImageView imageViewSearch;

    //加载动画
    private AVLoadingIndicatorView avi;

    //当前搜索到的音乐文件名字
    private TextView tv;

    LinkedList<File> musicsList;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            //搜索已完成

            //影藏进度条
            avi.hide();

            //显示搜索按钮
            imageViewSearch.setVisibility(View.VISIBLE);

            Toast.makeText(SearchMusicActivity.this, "共查询到" + musicsList.size() + "首歌曲", Toast.LENGTH_LONG).show();

            //添加到sp里，持久化音乐列表
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utils.saveBeanToSp(SearchMusicActivity.this, musicsList, Constant.MUSICLISTKEY);
                }
            }).start();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchmusic);
        //状态栏透明化
        StatusBarUtil.setColor(SearchMusicActivity.this, Color.parseColor("#31c25c"), 112);

        //搜索图标
        imageViewSearch = findViewById(R.id.searchMusicActivity_searchImageView);

        //进度条
        avi = findViewById(R.id.searchMusicActivity_loadingView);

        //搜索文字显示
        tv = findViewById(R.id.searchMusicActivity_textView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            List<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.
                    permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.
                    permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.
                    permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, 1);
            } else {

            }
        }


    }


    /**
     * 左上返回按钮
     *
     * @param view
     */
    public void back(View view) {
        Intent intent = new Intent(SearchMusicActivity.this, MusicListActivity.class);
        startActivity(intent);
        this.finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }


    /**
     * 搜索按钮(图片)
     *
     * @param view
     */
    public void search(View view) {
        //将搜索的图片隐藏
        imageViewSearch.setVisibility(View.GONE);
        //显示进度条
        avi.setVisibility(View.VISIBLE);
        avi.show();

        //开启线程进行歌曲的搜索
        new Thread(new Runnable() {
            @Override
            public void run() {

                //存储歌曲位置
                musicsList = new LinkedList<>();

                //内部存储根目录
                File file = new File("/storage/emulated/0");
//                File file = new File("/");

                //遍历根目录(非迭代)
                //先遍历首目录
                if (file.exists()) {
                    LinkedList<File> list = new LinkedList<>();
                    File[] files = file.listFiles();
                    for (final File file2 : files) {
                        if (file2.isDirectory()) {
                            list.add(file2);
                        } else {
                            String n = file2.getName().toLowerCase();
                            //判断是否为音乐文件
                            if (n.endsWith(".mp3") || n.endsWith(".mpeg") || n.endsWith(".wma")
                                    || n.endsWith(".midi") || n.endsWith(".mpeg-4")|| n.endsWith(".flac")) {

                                //剔除小于1MB的文件
                                if (file2.length() / (1024 * 1024) >= 1) {
                                    //将其加入音乐列表
                                    musicsList.add(file2);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //更新文本
                                            tv.setText(file2.getName());
                                        }
                                    });
                                }


                            }
                        }
                    }

                    //对首目录的子目录进行遍历
                    File temp_file;
                    while (!list.isEmpty()) {
                        temp_file = list.removeFirst();
                        files = temp_file.listFiles();
                        for (final File file2 : files) {
                            if (file2.isDirectory()) {
                                list.add(file2);
                            } else {
                                String n = file2.getName().toLowerCase();
                                if (n.endsWith(".mp3") || n.endsWith(".mpeg") || n.endsWith(".wma")
                                        || n.endsWith(".midi") || n.endsWith(".mpeg-4")|| n.endsWith(".flac")) {

                                    //剔除小于1MB的文件
                                    if (file2.length() / (1024 * 1024) >= 1) {
                                        musicsList.add(file2);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv.setText(file2.getName());
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }

                //发生通知给主线程，表示搜索已完成
                Message msg = Message.obtain();
                SearchMusicActivity.this.handler.sendMessage(msg);

            }
        }).start();
    }

}

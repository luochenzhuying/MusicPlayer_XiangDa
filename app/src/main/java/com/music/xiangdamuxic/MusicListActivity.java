package com.music.xiangdamuxic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.utils.ActivityManager;
import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.Utils;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class MusicListActivity extends SuperActivity {

    private ListView musicListView;
    BaseAdapter adapter = null;


    /**
     * 音乐列表LinkedList，存储每个音乐的文件地址
     */
    private LinkedList<File> list;


    /**
     * 音乐列表listView的适配器
     */
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;

            //复用
            if (view == null) {
                v = View.inflate(MusicListActivity.this, R.layout.item_musiclist_adapter, null);
            } else {
                v = view;
            }

            TextView songName = v.findViewById(R.id.item_musicList_songName);
            TextView songer = v.findViewById(R.id.item_musicList_songer);
            AVLoadingIndicatorView loadingView = v.findViewById(R.id.item_loadingView);

            //获取歌曲文件地址
            File f = (File) getItem(i);

            //设置歌曲名字
            String name = f.getName();
            songName.setText(name.substring(0, name.length() - 4));

            //获取当前的播放位置，使得歌曲列表中的loadingView显示出来，表示其正在播放
            int currPlayingNum = Utils.getInt(MusicListActivity.this, Constant.PLAYINGNUM, 0);
            if (currPlayingNum == i) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }


            return v;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);
        //状态栏透明化
        StatusBarUtil.setTransparent(MusicListActivity.this);

        //获取音乐列表listView
        musicListView = findViewById(R.id.musicListActivity_musicListView);

        //检查是否有音乐列表
        //音乐列表LinkedList 是直接被持久化处理，现在获取看是否存在列表
        list = Utils.getBeanFromSp(this, Constant.MUSICLISTKEY);
        if (list != null) {
            if (list.size() == 0) {
                //空
                Toast.makeText(this, "未添加歌曲，点击右上方的搜索按钮可以进行搜索哦~", Toast.LENGTH_LONG).show();
            } else {
                //设置适配器
                adapter = new MyAdapter();
                musicListView.setAdapter(adapter);
            }
        }else{
            Toast.makeText(this, "未添加歌曲，点击右上方的搜索按钮可以进行搜索哦~", Toast.LENGTH_LONG).show();
        }
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //设置当前播放歌曲的位置
                if (Utils.putInt(MusicListActivity.this, Constant.PLAYINGNUM, i)) {

                    //进入播放页面
                    Intent intent = new Intent(MusicListActivity.this, PlayerActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.next_in, R.anim.next_out);
                }

            }
        });

    }


    /**
     * 左上角的返回按钮
     *
     * @param view
     */
    public void back(View view) {
        MusicListActivity.this.finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }


    /**
     * 右上角搜索音乐按钮
     *
     * @param view
     */
    public void SearchMusic(View view) {
        //开启搜索页面
        Intent intent = new Intent(this, SearchMusicActivity.class);
        startActivity(intent);
        this.finish();
        overridePendingTransition(R.anim.next_in, R.anim.next_out);

    }
}

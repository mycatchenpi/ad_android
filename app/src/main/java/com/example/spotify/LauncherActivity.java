package com.example.spotify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.spotify.model.vo.PlaylistVO;
import com.example.spotify.model.vo.TrackVO;

import java.util.HashMap;
import java.util.Map;

public class LauncherActivity extends AppCompatActivity {
    private LinearLayout mLinearLayout;
    private LinearLayout mLocationLinearLayout;
    private LinearLayout mTimeLinearLayout;
    private AppCompatButton mLoginBtn;
    private int mWidth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollview_launcher_index);

        getDeviceWidth();
        initView();
    }

    /**
     * 获得屏幕宽度
     */
    private void getDeviceWidth() {
        DisplayMetrics dm = new DisplayMetrics();// 获得屏幕分辨率
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
    }

    /**
     * 初始化组件
     */
    private void initView() {
        int[] imagePlaylists = {R.drawable.playlist1, R.drawable.playlist2, R.drawable.playlist3,
                 R.drawable.playlist4, R.drawable.playlist5, R.drawable.playlist6};// 图片数组（长度和 horizontalscrollview 的子项长度一样）

        int[] imageTracks = {R.drawable.track1, R.drawable.track2, R.drawable.track3,
                R.drawable.track4, R.drawable.track5, R.drawable.track6};

        Map<Integer, PlaylistVO> playlistMap = new HashMap<>();
        playlistMap.put(0,
                new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1DX2sUQwD7tbmL",
                        "Feel-Good Indie Rock"));
        playlistMap.put(1, new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1EIUu7mk4MUkLV",
                "BLACKPINK Mix"));
        playlistMap.put(2, new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1DX6ALfRKlHn1t",
                "Soak Up The Sun"));
        playlistMap.put(3, new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1DX4WYpdgoIcn6",
                "Chill Hits"));
        playlistMap.put(4, new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1DWVlLVXKTOAYa",
                "Pop Right Now"));
        playlistMap.put(5, new PlaylistVO("https://open.spotify.com/playlist/37i9dQZF1DX4OzrY981I1W",
                "my life is a movie"));


        Map<Integer, TrackVO> tracksMap = new HashMap<>();
        tracksMap.put(0,
                new TrackVO("https://open.spotify.com/track/2QSrwZ7iVknZgkfi4aD6cn",
                        "Saltwater", "Geowulf"));
        tracksMap.put(1,
                new TrackVO("https://open.spotify.com/track/0ibvUpSyUdMXrmuPIcg1T3",
                        "Settling", "Ripe"));
        tracksMap.put(2,
                new TrackVO("https://open.spotify.com/track/62tGzw9OJOwDcjPBnMPCuj",
                        "No More Lies", "Thundercat"));
        tracksMap.put(3,
                new TrackVO("https://open.spotify.com/track/17fL4slDQP8YopAZHWyiR3",
                        "Only Girl", "Stephen Sanchez"));
        tracksMap.put(4,
                new TrackVO("https://open.spotify.com/track/3k79jB4aGmMDUQzEwa46Rz",
                        "vampire", "Olivia Rodrigo"));
        tracksMap.put(5,
                new TrackVO("https://open.spotify.com/track/4P9Q0GojKVXpRTJCaL3kyy",
                        "All Of The Girls You Loved Before", "Taylor Swift"));

        // daily playlists
        mLinearLayout = (LinearLayout) findViewById(R.id.scrollview_dailyPlaylists);// 实例化线性布局
        mLinearLayout.removeAllViews();// 删除以前的组件（如此保证每次都是horizontalscrollview中只有6个组件）
        addPlaylists(6, imagePlaylists, playlistMap);

        // recommendation songs based on location
        mLocationLinearLayout = findViewById(R.id.scrollview_location);
        mLocationLinearLayout.removeAllViews();
        addLocationTracks(2, imageTracks, tracksMap);

        // recommendation songs based on time
        mTimeLinearLayout = (LinearLayout) findViewById(R.id.scrollview_time);
        mTimeLinearLayout.removeAllViews();
        addTimeTracks(2, imageTracks, tracksMap);

        // Login
        mLoginBtn = (AppCompatButton) findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(LauncherActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

    }

    private void addPlaylists(int numOfPlaylists, int[] imageArray, Map<Integer, PlaylistVO> playlistMap) {

        for (int i = 0; i < numOfPlaylists; i++) {// 在 horizontalscrollview中添加6个组件
            int width = mWidth / 3;  // 首页显示3个playlists
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    LauncherActivity.this, R.layout.scrollview_dailyplaylists_item, null);// 动态实例化一个LinearLayout
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));// 设置宽度为一张屏幕显示三个组件
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);// 设置垂直居中

            ImageView mImageView = (ImageView) itemLayout
                    .findViewById(R.id.daily_playlist_image);
            TextView mTextView = (TextView) itemLayout
                    .findViewById(R.id.daily_playlist_name);

            // through key get value
            PlaylistVO playlists = playlistMap.get(i);
            String url = playlists.getUrl();
            String name = playlists.getName();
            mImageView.setBackgroundResource(imageArray[i]);
            mTextView.setText(name);

            mLinearLayout.addView(itemLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 处理点击事件
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(intent);

                    startWebView(url);

                    Toast.makeText(LauncherActivity.this, "clicked " + name,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addLocationTracks(int numOfTracks, int[] imageArray, Map<Integer, TrackVO> tracksMap) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout groupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    LauncherActivity.this, R.layout.scrollview_location_groupeditem, null);
            //groupedItemLayout.setLayoutParams(new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.MATCH_PARENT)); // Make sure the width is mWidth
            for (int j = 0; j < 3; j++) { // Assuming each grouped item contains 3 location items
                View locationItem = groupedItemLayout.getChildAt(j);
                ImageView imageView = locationItem.findViewById(R.id.location_image);
                TextView nameView = locationItem.findViewById(R.id.location_song_name);
                TextView artistView = locationItem.findViewById(R.id.location_artist_name);
                imageView.setImageResource(imageArray[index]); // Change this to your actual image resource
                TrackVO track = tracksMap.get(index);
                String url = track.getUrl();
                String name = track.getName();
                String artist = track.getArtist();
                nameView.setText(name); // Change this to your actual text
                artistView.setText(artist);
                index++;
                locationItem.setTag(url); // store url in view as tag

                locationItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 处理点击事件
                        String url = (String) v.getTag(); // access url from view tag
                        if (url != null && !url.isEmpty()) {
                            // 创建Intent打开URL
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                            startActivity(intent);
                            startWebView(url);
                        }
                    }
                });
            }
            mLocationLinearLayout.addView(groupedItemLayout);
        }
    }
    private void addTimeTracks(int numOfTracks, int[] imageArray, Map<Integer, TrackVO> tracksMap) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout timeGroupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    LauncherActivity.this, R.layout.scrollview_time_groupeditem, null);
            //timeGroupedItemLayout.setLayoutParams(new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.MATCH_PARENT)); // Make sure the width is mWidth
            for (int j = 0; j < 3; j++) { // Assuming each grouped item contains 3 location items
                View locationItem = timeGroupedItemLayout.getChildAt(j);
                ImageView imageView = locationItem.findViewById(R.id.time_image);
                TextView nameView = locationItem.findViewById(R.id.time_song_name);
                TextView artistView = locationItem.findViewById(R.id.time_artist_name);
                imageView.setImageResource(imageArray[index]); // Change this to your actual image resource
                TrackVO track = tracksMap.get(index);
                String url = track.getUrl();
                String name = track.getName();
                String artist = track.getArtist();
                nameView.setText(name); // Change this to your actual text
                artistView.setText(artist);
                index++;
                locationItem.setTag(url); // 将url作为tag存储在View中

                locationItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 处理点击事件
                        String url = (String) v.getTag(); // 从View的tag中获取url
                        if (url != null && !url.isEmpty()) {
                            // 创建Intent打开URL
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                            startActivity(intent);
                            startWebView(url);
                        }
                    }
                });
            }
            mTimeLinearLayout.addView(timeGroupedItemLayout);
        }
    }

    public void startWebView(String url) {
        Intent webViewIntent = new Intent(LauncherActivity.this, SongWebViewActivity.class);
        webViewIntent.putExtra("url", url);
        startActivity(webViewIntent);
    }

}

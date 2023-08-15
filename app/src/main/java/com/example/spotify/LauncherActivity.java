package com.example.spotify;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.spotify.constant.CommonConstant;
import com.example.spotify.model.dto.SongDTO;
import com.example.spotify.util.RetrofitUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        Map<Integer, SongDTO> firstDefaultMap = new HashMap<>();
        Map<Integer, SongDTO> moreDefaultMap = new HashMap<>();
        Map<Integer, SongDTO> manyMoreDefaultMap = new HashMap<>();
        getAllDefaultRecommendationSongs(firstDefaultMap, moreDefaultMap, manyMoreDefaultMap );
    }

    private void getDeviceWidth() {
        DisplayMetrics dm = new DisplayMetrics();// 获得屏幕分辨率
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
    }

    // this page is the first page user will see
    // so if users click back they will exit our app directly
    // cause before this page there is no page anymore
    // so that we start Launcher page again and finish current activity
    // then user wont exit our app
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            startActivity(new Intent(this, LauncherActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {

        // daily public songs
        // removeAllViews(): delete previous components so that always only 6 / 12 components on our page
        mLinearLayout = (LinearLayout) findViewById(R.id.scrollview_dailyPlaylists);
        mLinearLayout.removeAllViews();

        // more songs
        mLocationLinearLayout = findViewById(R.id.scrollview_location);
        mLocationLinearLayout.removeAllViews();

        // many more songs
        mTimeLinearLayout = (LinearLayout) findViewById(R.id.scrollview_time);
        mTimeLinearLayout.removeAllViews();

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

    private void getAllDefaultRecommendationSongs(Map<Integer, SongDTO> firstDefaultMap,
                                                  Map<Integer, SongDTO> moreDefaultMap,
                                                  Map<Integer, SongDTO> manyMoreDefaultMap) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getSongsPublic();
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("popular songs: ", song.getName());
                    }

                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        if(i < 6) {
                            firstDefaultMap.put(i, song);
                        } else if (i >= 6 && i < 12) {
                            moreDefaultMap.put(i - 6, song);
                        } else {
                            manyMoreDefaultMap.put(i - 12, song);
                        }
                    }
                    addFirstDefaultSongs(6, firstDefaultMap);
                    addMoreTracks(2, moreDefaultMap);
                    addManyMoreTracks(2, manyMoreDefaultMap);
                } else {
                    Log.d("LauncherActivity", "Failed to get public recommendation songs");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void addFirstDefaultSongs(int numOfTracks, Map<Integer, SongDTO> songs) {
        // add 6 components in horizontalScrollview,
        // int width = mWidth / 3  -> show 3 components on the default page width
        for (int i = 0; i < numOfTracks; i++) {
            int width = mWidth / 3;
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                                        LauncherActivity.this,
                                        R.layout.scrollview_dailyplaylists_item,
                                        null);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);  // 设置垂直居中

            ImageView mImageView = (ImageView) itemLayout.findViewById(R.id.daily_playlist_image);
            TextView mTextView = (TextView) itemLayout.findViewById(R.id.daily_playlist_name);

            // get songUrl, imageUrl, songName
            SongDTO song = songs.get(i);
            if (song != null) {
                String songUrl = CommonConstant.TRACK_BASE_URL + song.getUri();
                itemLayout.setTag(songUrl);

                String imageUrl = song.getImageUrl();
                String songName = song.getName();
                mTextView.setText(songName);

                // download and set background image
                Glide.with(this)
                        .load(imageUrl)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                mImageView.setImageDrawable(resource);
                            }
                        });
            } else {
                Log.d("LauncherActivity", "Song at index " + i + " is null");
            }

            mLinearLayout.addView(itemLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 处理点击事件
                    String songUrl = (String) v.getTag(); // access url from view tag
                    if (songUrl != null && !songUrl.isEmpty()) {
                        startWebView(songUrl);
                    }
                }
            });
        }
    }

    private void addMoreTracks(int numOfTracks, Map<Integer, SongDTO> songs) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout groupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    LauncherActivity.this, R.layout.scrollview_location_groupeditem, null);

            // Assuming each grouped item contains 3 location items
            for (int j = 0; j < 3; j++) {
                View moreItem = groupedItemLayout.getChildAt(j);
                ImageView imageView = moreItem.findViewById(R.id.location_image);
                TextView nameView = moreItem.findViewById(R.id.location_song_name);
                TextView artistView = moreItem.findViewById(R.id.location_artist_name);

                // get songUrl, imageUrl, songName, artistName
                SongDTO song = songs.get(index);
                String songUrl = CommonConstant.TRACK_BASE_URL + song.getUri();
                String imageUrl = song.getImageUrl();
                String songName = song.getName();
                String artistName = song.getArtist();
                nameView.setText(songName); // Change this to your actual text
                artistView.setText(artistName);

                Glide.with(this)
                        .load(imageUrl)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource,
                                                        @Nullable Transition<? super Drawable> transition) {
                                // set background image for each component
                                imageView.setImageDrawable(resource);
                            }
                        });

                index++;
                // store url in view as tag
                moreItem.setTag(songUrl);

                moreItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // access url from view tag
                        String songUrl = (String) v.getTag();
                        if (songUrl != null && !songUrl.isEmpty()) {
                            startWebView(songUrl);
                        }
                    }
                });
            }
            mLocationLinearLayout.addView(groupedItemLayout);
        }
    }
    private void addManyMoreTracks(int numOfTracks, Map<Integer, SongDTO> songs) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout manyMoreGroupItemLayout = (LinearLayout) LinearLayout.inflate(
                    LauncherActivity.this, R.layout.scrollview_time_groupeditem, null);

            // Assuming each grouped item contains 3 items
            for (int j = 0; j < 3; j++) {
                View manyMoreItem = manyMoreGroupItemLayout.getChildAt(j);
                ImageView imageView = manyMoreItem.findViewById(R.id.time_image);
                TextView nameView = manyMoreItem.findViewById(R.id.time_song_name);
                TextView artistView = manyMoreItem.findViewById(R.id.time_artist_name);

                // get songUrl, imageUrl, songName, artistName
                SongDTO song = songs.get(index);
                String songUrl = CommonConstant.TRACK_BASE_URL + song.getUri();
                String imageUrl = song.getImageUrl();
                String songName = song.getName();
                String artistName = song.getArtist();
                nameView.setText(songName); // Change this to your actual text
                artistView.setText(artistName);

                Glide.with(this)
                        .load(imageUrl)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imageView.setImageDrawable(resource); // 设置背景图片
                            }
                        });

                index++;
                manyMoreItem.setTag(songUrl); // 将url作为tag存储在View中

                manyMoreItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 处理点击事件
                        String songUrl = (String) v.getTag(); // 从View的tag中获取url
                        if (songUrl != null && !songUrl.isEmpty()) {
                            startWebView(songUrl);
                        }
                    }
                });
            }
            mTimeLinearLayout.addView(manyMoreGroupItemLayout);
        }
    }

    public void startWebView(String url) {
        Intent webViewIntent = new Intent(LauncherActivity.this, SongWebViewActivity.class);
        webViewIntent.putExtra("url", url);
        startActivity(webViewIntent);
    }
}



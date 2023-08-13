package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.spotify.constant.CommonConstant;
import com.example.spotify.model.dto.ReceivedLocationDTO;
import com.example.spotify.model.dto.SongDTO;
import com.example.spotify.util.RetrofitUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mLinearLayout;
    private LinearLayout mLocationLinearLayout;
    private LinearLayout mTimeLinearLayout;
    private LinearLayout mHolidayLayout;
    private LinearLayout mUserPopularSongs;
    private AppCompatButton mLogoutBtn;
    private TextView mHelloUser;
    private int mWidth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String username = sp.getString("username", "");
        mHelloUser = (TextView)findViewById(R.id.hello_user);
        mHelloUser.setText("Hello " + username);

        getDeviceWidth();

        // daily playlists
        mLinearLayout = (LinearLayout) findViewById(R.id.user_dailyPlaylists);// 实例化线性布局
        mLinearLayout.removeAllViews();// 删除以前的组件（如此保证每次都是horizontalscrollview中只有6个组件）
        showRecommendationDailySongs(username);

        // recommendation songs based on location
        mLocationLinearLayout = findViewById(R.id.user_location);
        mLocationLinearLayout.removeAllViews();
        showRecommendationSongsBasedOnLocation(username);

        // recommendation songs based on time
        mTimeLinearLayout = (LinearLayout) findViewById(R.id.user_time);
        mTimeLinearLayout.removeAllViews();
        showRecommendationSongsBasedOnTime(username);

        // recommendation songs based on holiday
        mHolidayLayout = (LinearLayout)findViewById(R.id.user_holiday_songs);
        mHolidayLayout.setVisibility(View.VISIBLE);
        mHolidayLayout.removeAllViews();
        showRecommendationSongsBasedOnHoliday(username);

        // recommended popular songs
        mUserPopularSongs = (LinearLayout)findViewById(R.id.user_popular_songs);
        mUserPopularSongs.removeAllViews();
        showRecommendedPopularSongs(username);

        // Logout
        mLogoutBtn = (AppCompatButton) findViewById(R.id.logout_btn);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                Toast.makeText(MainActivity.this, "Logout Succesfully!", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    /**
     * 获得屏幕宽度
     */
    private void getDeviceWidth() {
        DisplayMetrics dm = new DisplayMetrics();// 获得屏幕分辨率
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
    }

    private void clear() {
        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        sp = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sp.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.commit();
    }

    private void showRecommendationSongsBasedOnLocation(String username) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getRecommendationSongByLocation(
                new ReceivedLocationDTO(1.3521, 103.8198, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("recommendationSongs location: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }

                    // call method to set and render page
                    addLocationTracks(2, songsMap);
                } else {
                    Log.d("MainActivity", "Failed to get recommendation songs based on location");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {

            }
        });
    }

    private void showRecommendationSongsBasedOnTime(String username) {

        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getRecommendationSongByTime(
                new ReceivedLocationDTO(1.3521, 103.8198, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("recommendationSongs time: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }

                    // call method to set and render page
                    addTimeTracks(2, songsMap);
                } else {
                    Log.d("MainActivity", "Failed to get recommendation songs based on time");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showRecommendationDailySongs(String username) {

        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getSongsAfterLogin(
                new ReceivedLocationDTO(1.3521, 103.8198, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("daily songs: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }

                    // call method to set and render page
                    addDailySongs(6, songsMap);
                } else {
                    Log.d("MainActivity", "Failed to get daily recommendation songs");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showRecommendedPopularSongs(String username) {

        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getSongsPublic(
                new ReceivedLocationDTO(1.3521, 103.8198, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("popular songs: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }

                    // call method to set and render page
                    addPopularSongs(6, songsMap);
                } else {
                    Log.d("MainActivity", "Failed to get popular recommendation songs");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showRecommendationSongsBasedOnHoliday(String username) {

        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().holidayCheck();
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if(response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for(SongDTO song: recommendationSongs) {
                        Log.d("holiday songs: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for(int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }
                    // call method to set and render page
                    addHolidaySongs(6, songsMap);

                } else {
                    // if currently not in a holiday duration, doesn't show holiday view
                    mHolidayLayout.setVisibility(View.GONE);
                    HorizontalScrollView mScrollviewHoliday = (HorizontalScrollView)findViewById(R.id.scrollview_holiday);
                    mScrollviewHoliday.setVisibility(View.GONE);
                    TextView mHolidaySongsRecName = (TextView)findViewById(R.id.holiday_songs_rec_name);
                    mHolidaySongsRecName.setVisibility(View.GONE);
                    Log.d("MainActivity", "Failed to get recommendation songs based on holiday");
                }
            }

            @Override
            public void onFailure(Call<List<SongDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void addDailySongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {

        for (int i = 0; i < numOfPlaylists; i++) {  // 在 horizontalscrollview中添加6个组件
            int width = mWidth / 3;  // 首页显示3个playlists
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_dailyplaylists_item, null);// 动态实例化一个LinearLayout
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));// 设置宽度为一张屏幕显示三个组件
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);// 设置垂直居中

            ImageView mImageView = (ImageView) itemLayout
                    .findViewById(R.id.daily_playlist_image);
            TextView mTextView = (TextView) itemLayout
                    .findViewById(R.id.daily_playlist_name);

            // through key get value
            SongDTO song = tracksMap.get(i);
            String url = CommonConstant.TRACK_BASE_URL + song.getUri();
            String imageUrl = song.getImageUrl();

            String name = song.getName();
            mTextView.setText(name);

            // dowload imageUrl
            Glide.with(this)
                    .load(imageUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mImageView.setImageDrawable(resource); // 设置背景图片
                        }
                    });

            mLinearLayout.addView(itemLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 处理点击事件 打开网页
                    // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    // startActivity(intent);

                    startWebView(url);

                    Toast.makeText(MainActivity.this, "clicked " + name,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addHolidaySongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {

        for (int i = 0; i < numOfPlaylists; i++) {// 在 horizontalscrollview中添加6个组件
            int width = mWidth / 3;  // 首页显示3个playlists
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_dailyplaylists_item, null);// 动态实例化一个LinearLayout
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));// 设置宽度为一张屏幕显示三个组件
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);// 设置垂直居中

            ImageView mImageView = (ImageView) itemLayout
                    .findViewById(R.id.daily_playlist_image);
            TextView mTextView = (TextView) itemLayout
                    .findViewById(R.id.daily_playlist_name);

            // through key get value
            SongDTO song = tracksMap.get(i);
            String url = CommonConstant.TRACK_BASE_URL + song.getUri();
            String imageUrl = song.getImageUrl();

            String name = song.getName();
            mTextView.setText(name);


            // dowload imageUrl
            Glide.with(this)
                    .load(imageUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mImageView.setImageDrawable(resource); // 设置背景图片
                        }
                    });

            mHolidayLayout.addView(itemLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 处理点击事件
                    // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    // startActivity(intent);

                    startWebView(url);
                    Toast.makeText(MainActivity.this, "clicked " + name,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addPopularSongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {

        for (int i = 0; i < numOfPlaylists; i++) {// 在 horizontalscrollview中添加6个组件
            int width = mWidth / 3;  // 首页显示3个playlists
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_dailyplaylists_item, null);// 动态实例化一个LinearLayout
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));// 设置宽度为一张屏幕显示三个组件
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);// 设置垂直居中

            ImageView mImageView = (ImageView) itemLayout
                    .findViewById(R.id.daily_playlist_image);
            TextView mTextView = (TextView) itemLayout
                    .findViewById(R.id.daily_playlist_name);

            // through key get value
            SongDTO song = tracksMap.get(i);
            String url = CommonConstant.TRACK_BASE_URL + song.getUri();
            String imageUrl = song.getImageUrl();

            String name = song.getName();
            mTextView.setText(name);


            // dowload imageUrl
            Glide.with(this)
                    .load(imageUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mImageView.setImageDrawable(resource); // 设置背景图片
                        }
                    });

            mUserPopularSongs.addView(itemLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 处理点击事件
                    // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    // startActivity(intent);

                    startWebView(url);

                    Toast.makeText(MainActivity.this, "clicked " + name,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void addLocationTracks(int numOfTracks, Map<Integer, SongDTO> tracksMap) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout groupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_location_groupeditem, null);
            //groupedItemLayout.setLayoutParams(new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.MATCH_PARENT)); // Make sure the width is mWidth
            for (int j = 0; j < 3; j++) { // Assuming each grouped item contains 3 location items
                View locationItem = groupedItemLayout.getChildAt(j);
                ImageView imageView = locationItem.findViewById(R.id.location_image);
                TextView nameView = locationItem.findViewById(R.id.location_song_name);
                TextView artistView = locationItem.findViewById(R.id.location_artist_name);

                SongDTO track = tracksMap.get(index);
                String url = CommonConstant.TRACK_BASE_URL + track.getUri();
                String name = track.getName();
                String artist = track.getArtist();
                String imageUrl = track.getImageUrl();

                nameView.setText(name); // Change this to your actual text
                artistView.setText(artist);
                // dowload imageUrl
                Glide.with(this)
                    .load(imageUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource); // 设置背景图片
                        }
                    });

                index++;
                locationItem.setTag(url); // 将url作为tag存储在View中

                locationItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 处理点击事件
                        String url = (String) v.getTag(); // 从View的tag中获取url
                        if (url != null && !url.isEmpty()) {
                            // 创建Intent打开URL
                            // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            // startActivity(intent);

                            startWebView(url);
                        }
                    }
                });
            }
            mLocationLinearLayout.addView(groupedItemLayout);
        }
    }


    private void addTimeTracks(int numOfTracks, Map<Integer, SongDTO> tracksMap) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout timeGroupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_time_groupeditem, null);
            //timeGroupedItemLayout.setLayoutParams(new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.MATCH_PARENT)); // Make sure the width is mWidth
            for (int j = 0; j < 3; j++) { // Assuming each grouped item contains 3 location items
                View timeItem = timeGroupedItemLayout.getChildAt(j);
                ImageView imageView = timeItem.findViewById(R.id.time_image);
                TextView nameView = timeItem.findViewById(R.id.time_song_name);
                TextView artistView = timeItem.findViewById(R.id.time_artist_name);

                SongDTO track = tracksMap.get(index);
                String trackUri = track.getUri();
                String url = CommonConstant.TRACK_BASE_URL + trackUri;
                String name = track.getName();
                String artist = track.getArtist();
                String imageUrl = track.getImageUrl();

                nameView.setText(name); // Change this to your actual text
                artistView.setText(artist);
                // dowload imageUrl
                Glide.with(this)
                        .load(imageUrl)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imageView.setImageDrawable(resource); // 设置背景图片
                            }
                        });

                index++;
                timeItem.setTag(url); // 将url作为tag存储在View中

                timeItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //startActivity(track);

                        // 处理点击事件
                        String url = (String) v.getTag(); // 从View的tag中获取url
                        if (url != null && !url.isEmpty()) {
                            // 创建Intent打开URL
                            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            //startActivity(intent);
                            startWebView(url);
                        }
                    }
                });
            }
            mTimeLinearLayout.addView(timeGroupedItemLayout);
        }
    }

    public void startWebView(String url) {
        Intent webViewIntent = new Intent(MainActivity.this, SongWebViewActivity.class);
        webViewIntent.putExtra("url", url);
        startActivity(webViewIntent);
    }

}
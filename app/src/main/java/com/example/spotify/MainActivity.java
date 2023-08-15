package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import com.example.spotify.model.dto.SongDataWithLocationDTO;
import com.example.spotify.util.RetrofitUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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
    private AppCompatButton mLogoutBtn;
    private TextView mHelloUser;
    private int mWidth;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // after user login, request user permission for location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        String username = getUsername();
        mHelloUser = (TextView)findViewById(R.id.hello_user);
        mHelloUser.setText("Hello " + username);

        getDeviceWidth();

        // daily playlists after user login
        mLinearLayout = (LinearLayout) findViewById(R.id.user_dailyPlaylists);
        mLinearLayout.removeAllViews();
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

        // Logout
        mLogoutBtn = (AppCompatButton) findViewById(R.id.logout_btn);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                Toast.makeText(MainActivity.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    // get username from shared preferences object
    private String getUsername() {
        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String username = sp.getString("username", "");
        return username;
    }

    // get screen width
    private void getDeviceWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
    }

    // when user click log out button, clear user info from shared preferences
    private void clear() {
        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        sp = getSharedPreferences("login_info", MODE_PRIVATE);
        editor = sp.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.commit();
    }

    // after user login, when user click back key
    // will restart current homepage activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // restart homepage and keep tasks
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                    addLocationTracks(recommendationSongs.size() / 3, songsMap);
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

                    addTimeTracks(recommendationSongs.size() / 3, songsMap);
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

                    addDailySongs(recommendationSongs.size(), songsMap);
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
                    addHolidaySongs(recommendationSongs.size(), songsMap);

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
            String uri = song.getUri();
            String url = CommonConstant.TRACK_BASE_URL + uri;
            String imageUrl = song.getImageUrl();
            Integer duration = song.getDuration();

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
                    createLocationCallback(uri, getUsername());
                    startLocationUpdates();
                    startWebView(url);
                }
            });
        }
    }

    private void addHolidaySongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {

        for (int i = 0; i < numOfPlaylists; i++) {// 在 horizontalScrollview中添加6个组件
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
            String uri = song.getUri();
            String url = CommonConstant.TRACK_BASE_URL + uri;
            String imageUrl = song.getImageUrl();

            String name = song.getName();
            mTextView.setText(name);

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
            for (int j = 0; j < 3; j++) { // Assuming each grouped item contains 3 location items
                View locationItem = groupedItemLayout.getChildAt(j);
                ImageView imageView = locationItem.findViewById(R.id.location_image);
                TextView nameView = locationItem.findViewById(R.id.location_song_name);
                TextView artistView = locationItem.findViewById(R.id.location_artist_name);

                SongDTO track = tracksMap.get(index);
                String uri = track.getUri();
                String url = CommonConstant.TRACK_BASE_URL + uri;
                String name = track.getName();
                String artist = track.getArtist();
                String imageUrl = track.getImageUrl();

                nameView.setText(name);
                artistView.setText(artist);

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
                            createLocationCallback(uri, getUsername());
                            startLocationUpdates();
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
            // Assuming each grouped item contains 3 location items
            for (int j = 0; j < 3; j++) {
                View timeItem = timeGroupedItemLayout.getChildAt(j);
                ImageView imageView = timeItem.findViewById(R.id.time_image);
                TextView nameView = timeItem.findViewById(R.id.time_song_name);
                TextView artistView = timeItem.findViewById(R.id.time_artist_name);

                SongDTO track = tracksMap.get(index);
                String uri = track.getUri();
                String url = CommonConstant.TRACK_BASE_URL + uri;
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
                        String url = (String) v.getTag(); // 从View的tag中获取url
                        if (url != null && !url.isEmpty()) {
                            createLocationCallback(uri, getUsername());
                            startLocationUpdates();
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

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Toast.makeText(MainActivity.this, "Allowed to access location", Toast.LENGTH_SHORT).show();
            } else {
                // Create an AlertDialog to inform the user
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Denied")
                        .setMessage("Send location will help you access more attractive songs. Please enable it in your settings.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Open App System settings
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null) // Add a "Cancel" button
                        .show();
            }
        }
    }

    //This method is to start getting the location based on Latitude and Longitude
    private void createLocationCallback(String uri, String username) {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("latitude", String.valueOf(location.getLatitude()));
                        Log.d("longitude", String.valueOf(location.getLongitude()));

                        //Create a LocationData object to be sent to Java Backend
                        SongDataWithLocationDTO.LocationData locationData = new SongDataWithLocationDTO.LocationData(latitude, longitude);
                        SongDataWithLocationDTO songDataWithLocation = new SongDataWithLocationDTO(uri, locationData, username);

                        // 延迟500ms
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 封装位置信息并发送到后端
                                sendLocationToBackend(songDataWithLocation);
                            }
                        }, 500);
                    }
                }
            }
        };
    }

    private void sendLocationToBackend(SongDataWithLocationDTO songDataWithLocation) {
        // Send the location data to the backend using Retrofit
        Call<String> call = RetrofitUtil.getApiService().sendSongWithLocationRecord(songDataWithLocation);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    // Handle success
                    Log.d("MainActivity", "record successfully!");

                } else {
                    // Handle error
                    Log.d("MainActivity", "record Failed!");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //This method is to update the location every 10000ms and 5000ms
    //(these timing can be changed)
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(300000);  // Update interval in milliseconds (5 minutes)
        locationRequest.setFastestInterval(100000);  // Fastest update interval in milliseconds (2.5 minutes)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
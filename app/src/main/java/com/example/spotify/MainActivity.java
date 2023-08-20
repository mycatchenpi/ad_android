package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check location permission and access last location info if user allowed location permission
        initPermissionAndLocation();

        // after user login, check their location permission first inside this method
        // if granted, send current location to restful api to get recommendation songs
        // if didn't grant, request location permission
        showRecommendationSongsBasedOnLocation();
        mLocationLinearLayout = findViewById(R.id.user_location);
        mLocationLinearLayout.removeAllViews();

        // get and display username for greeting
        String username = getUsername();
        mHelloUser = (TextView) findViewById(R.id.hello_user);
        mHelloUser.setText("Hello " + username);

        // get the width of user's device
        getDeviceWidth();

        // show recommended daily songs after user login
        mLinearLayout = (LinearLayout) findViewById(R.id.user_dailyPlaylists);
        mLinearLayout.removeAllViews();
        showRecommendationDailySongs(username);

        // show recommended songs based on current time
        mTimeLinearLayout = (LinearLayout) findViewById(R.id.user_time);
        mTimeLinearLayout.removeAllViews();
        showRecommendationSongsBasedOnTime(username);

        // show recommended songs based on holiday
        mHolidayLayout = (LinearLayout) findViewById(R.id.user_holiday_songs);
        mHolidayLayout.setVisibility(View.VISIBLE);
        mHolidayLayout.removeAllViews();
        showRecommendationSongsBasedOnHoliday(username);

        // Logout
        mLogoutBtn = (AppCompatButton) findViewById(R.id.logout_btn);
        logout(mLogoutBtn);
    }

    /**
     * at the first time user login, will request user permission for location
     * after user granted, access the last location info
     */
    private void initPermissionAndLocation() {
        if (!isLocationPermission()) {
            showRecommendationSongsBasedOnLocationRequestBackend(getUsername(), 0.0, 0.0);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // obtain an instance of FusedLocationProviderClient,
        // which is responsible for accessing location information
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 设置请求优先级
                .setInterval(10000) // 设置位置更新的时间间隔，单位为毫秒
                .setFastestInterval(5000); // 设置最快的位置更新时间间隔，单位为毫秒

        checkLocationPermission();
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {

            }
        }, null);
    }

    /**
     * get username from shared preferences object
     * @return username
     */
    private String getUsername() {
        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        String username = sp.getString("username", "");
        return username;
    }

    /**
     * get screen width
     */
    private void getDeviceWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
    }

    /**
     * logout
     */
    private void logout(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                Toast.makeText(MainActivity.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    /**
     *  when user click log out button, clear user info from shared preferences
     */
    private void clear() {
        SharedPreferences sp = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.commit();
    }

    /**
     * after user login, when user click back key
     * will restart current homepage activity instead of go back to login page
     * @param keyCode The value in event.getKeyCode().
     * @param event Description of the key event.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // restart homepage and keep tasks
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * after user login, check their location permission first
     * if granted, send current location to java restful api to get recommended songs
     * if didn't grant, request location permission
     */
    private void showRecommendationSongsBasedOnLocation() {
        checkLocationPermission();
        Task<Location> lastLocation = fusedLocationClient.getLastLocation();
        lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                double lat = 0.0;
                double lon = 0.0;
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
                showRecommendationSongsBasedOnLocationRequestBackend(getUsername(), lat, lon);
            }
        });
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
//                @Override
//                public void onLocationChanged(@NonNull Location location) {
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
//                    showRecommendationSongsBasedOnLocationRequestBackend(getUsername(), location.getLatitude(), location.getLongitude());
//                }
//            });
//        }

        //fusedLocationClient.getCurrentLocation(CurrentLocationRequest currentLocationrequest, )

    }

    /**
     * utilize retrofit2 for connecting and accessing recommended songs based on location from java restful api
     * @param username the name user use for login
     * @param latitude a double value indicates latitude of a location
     * @param longitude a double value indicates longitude of a location
     */
    private void showRecommendationSongsBasedOnLocationRequestBackend(String username, double latitude, double longitude) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getRecommendationSongByLocation(
                new ReceivedLocationDTO(latitude, longitude, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if (response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for (SongDTO song : recommendationSongs) {
                        Log.d("recommendationSongs location: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for (int i = 0; i < recommendationSongs.size(); i++) {
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

    /**
     * utilize retrofit2 for connecting and accessing recommended songs based on time from java restful api
     * @param username the name user use for login
     */
    private void showRecommendationSongsBasedOnTime(String username) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getRecommendationSongByTime(
                new ReceivedLocationDTO(0.0, 0.0, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if (response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for (SongDTO song : recommendationSongs) {
                        Log.d("recommendationSongs time: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for (int i = 0; i < recommendationSongs.size(); i++) {
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

    /**
     * utilize retrofit2 for connecting and accessing recommended daily songs from java restful api
     * @param username the name user use for login
     */
    private void showRecommendationDailySongs(String username) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().getSongsAfterLogin(
                new ReceivedLocationDTO(0.0, 0.0, username));
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if (response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for (int i = 0; i < recommendationSongs.size(); i++) {
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

    /**
     * utilize retrofit2 for connecting and accessing recommended songs based on holiday from java restful api
     * @param username
     */
    private void showRecommendationSongsBasedOnHoliday(String username) {
        retrofit2.Call<List<SongDTO>> call = RetrofitUtil.getApiService().holidayCheck();
        call.enqueue(new Callback<List<SongDTO>>() {
            @Override
            public void onResponse(Call<List<SongDTO>> call, Response<List<SongDTO>> response) {
                if (response.isSuccessful()) {
                    List<SongDTO> recommendationSongs = response.body();
                    for (SongDTO song : recommendationSongs) {
                        Log.d("holiday songs: ", song.getName());
                    }

                    Map<Integer, SongDTO> songsMap = new HashMap<>();
                    for (int i = 0; i < recommendationSongs.size(); i++) {
                        SongDTO song = recommendationSongs.get(i);
                        songsMap.put(i, song);
                    }
                    // call method to set and render page
                    addHolidaySongs(recommendationSongs.size(), songsMap);
                } else {
                    // if currently not in a holiday duration, doesn't show holiday view
                    mHolidayLayout.setVisibility(View.GONE);
                    HorizontalScrollView mScrollviewHoliday = (HorizontalScrollView) findViewById(R.id.scrollview_holiday);
                    mScrollviewHoliday.setVisibility(View.GONE);
                    TextView mHolidaySongsRecName = (TextView) findViewById(R.id.holiday_songs_rec_name);
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

    /**
     * render image and song info
     * @param numOfPlaylists the number of recommended songs
     * @param tracksMap store recommended song obj with index
     */
    private void addDailySongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {
        // add 6 components in horizontalScrollview
        // int width = mWidth / 3  -> show 3 components on the default page width
        for (int i = 0; i < numOfPlaylists; i++) {
            int width = mWidth / 3;
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_dailyplaylists_item, null);
            // set width with value 3
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

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

            // download image
            glideDownloadImage(imageUrl, mImageView);

            mLinearLayout.addView(itemLayout);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendLocationToBackend(uri);
                    startWebView(url);
                }
            });
        }
    }

    private void addHolidaySongs(int numOfPlaylists, Map<Integer, SongDTO> tracksMap) {
        for (int i = 0; i < numOfPlaylists; i++) {
            int width = mWidth / 3;
            LinearLayout itemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_dailyplaylists_item, null);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

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

            // download and set image
            glideDownloadImage(imageUrl, mImageView);
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

    private void addLocationTracks(int numOfTracks, Map<Integer, SongDTO> tracksMap) {
        int index = 0;
        for (int i = 0; i < numOfTracks; i++) {
            LinearLayout groupedItemLayout = (LinearLayout) LinearLayout.inflate(
                    MainActivity.this, R.layout.scrollview_location_groupeditem, null);

            // Assuming each grouped item contains 3 location items
            for (int j = 0; j < 3; j++) {
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
                // download image
                glideDownloadImage(imageUrl,imageView);
                index++;
                // store tag to view and then we can get this value from view
                locationItem.setTag(url);
                locationItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = (String) v.getTag();
                        if (url != null && !url.isEmpty()) {
                            sendLocationToBackend(uri);
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

                nameView.setText(name);
                artistView.setText(artist);
                // download image
                glideDownloadImage(imageUrl, imageView);
                index++;
                timeItem.setTag(url);
                timeItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = (String) v.getTag();
                        if (url != null && !url.isEmpty()) {
                            sendLocationToBackend(uri);
                            startWebView(url);
                        }
                    }
                });
            }
            mTimeLinearLayout.addView(timeGroupedItemLayout);
        }
    }

    /**
     * utilize Glide framework for image downloading
     * @param imageUrl the url for downloading an image
     * @param mImageView the view which will use image as background
     */
    private void glideDownloadImage(String imageUrl, ImageView mImageView) {
        Glide.with(this)
                .load(imageUrl)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mImageView.setImageDrawable(resource); // 设置背景图片
                    }
                });
    }

    /**
     * after user click a item, redirect them to a webView
     * @param url the weblink of a recommended song
     */
    public void startWebView(String url) {
        Intent webViewIntent = new Intent(MainActivity.this, SongWebViewActivity.class);
        webViewIntent.putExtra("url", url);
        startActivity(webViewIntent);
    }

    /**
     * if isLocationPermission() return true, location permission granted
     * else location permission denied, set latitude = 0.0 & longitude = 0.0 and request for location permission
     */
    private void checkLocationPermission() {
        isLocationPermission();
    }

    /**
     * check if user grated location permission
     * @return if granted, return true; else, return false
     */
    private boolean isLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * receive the outcome after request for location permission
     * if user granted, prompt a message for success info
     * if user denied, prompt a AlertDialog guiding user go to settings page allowing location permission
     *
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
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

    /**
     * after user login, will call this method for location accessing
     * @param uri the uri of a song
     */
    private void sendLocationToBackend(String uri) {
        checkLocationPermission();
        Task<Location> lastLocation = fusedLocationClient.getLastLocation();
        lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                SongDataWithLocationDTO.LocationData locationData = new SongDataWithLocationDTO.LocationData(location.getLatitude(), location.getLongitude());
                SongDataWithLocationDTO songDataWithLocation = new SongDataWithLocationDTO(uri, locationData, getUsername());
                getLocationAndSendToBackend(songDataWithLocation);
            }
        });
    }

    /**
     * utilize retrofit2 for connecting with Java restful api for accessing recommend songs based on current location
     * @param songDataWithLocation an obj encapsulates songUri, location obj, and username
     */
    private void getLocationAndSendToBackend(SongDataWithLocationDTO songDataWithLocation) {
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

    /**
     * after user granted for location permission from settings page
     * refresh main page with permission
     * so that can get current location and send it to java backend
     * then we can update our main page data with recommendation songs based on location
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isLocationPermission()) {
            // if user granted location permission, refresh page
            refreshPage();
        }
    }

    /**
     * send location to java backend
     * then can get recommendation songs from java backend
     */
    private void refreshPage() {
        showRecommendationSongsBasedOnLocation();
    }


}
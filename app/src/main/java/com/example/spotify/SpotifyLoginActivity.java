package com.example.spotify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotify.entity.JsonResponseEntity;
import com.example.spotify.util.RetrofitUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotifyLoginActivity extends AppCompatActivity {

    private static final String REDIRECT_URI = "http://192.168.1.97:9091/android_callback";

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "a20159075ec841cd8cd19e1e28a6413c";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);


        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        //AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AuthorizationClient.openLoginActivity(SpotifyLoginActivity.this, REQUEST_CODE, request);
            }
        }, 2000); // 2 秒延迟

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Use the access token to authenticate the user and start retrieving user data
                    String accessToken = response.getAccessToken();
                    Log.e("LoginActivity", "accessToken = " + accessToken);

                    // store token
                    storeTokenAsSP(accessToken);

                    // test for login 后用户页面
                    Intent userIntent = new Intent(SpotifyLoginActivity.this, MainActivity.class);
                    startActivity(userIntent);

                    // 通过spotify uri拿到 json
                    String url = "https://api.spotify.com/v1/tracks/" + "3cEYpjA9oz9GiPac4AsH4n";

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + accessToken);
                    Call<ResponseBody> call = RetrofitUtil
                            .getSportfyApiService()
                            .getSportfySingleTrack("11dFghVXANMlKmJXsNCbNl", headers);
                    // Call<ResponseBody> call = RetrofitUtil.getApiService().getSingleTrack(1);

                    call.enqueue(new retrofit2.Callback<ResponseBody>() {

                        @Override
                        public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                            try {
                                String responseData = null;
                                responseData = String.valueOf(response.body().string());
                                Log.d("Testc", responseData);
                                //changeWithJSONObject(responseData);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                            System.out.println(t);
                        }
                    });
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e("LoginActivity", "Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("LoginActivity", "Auth result: " + response.getType());
            }
        }
    }

    private void storeTokenAsSP(String token) {
        SharedPreferences sp = SpotifyLoginActivity.this.getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.commit();
    }

    //测试webview登录成功后返回值
    //     使用access_token和user_id进行相关操作，例如获取用户信息和播放历史等数据
    private void retrieveUserData(String token) {
        // 在这里使用access_token和userId进行网络请求，获取用户信息和播放历史等数据
        // 使用Retrofit或其他网络请求库发送请求
        // 示例代码：
        retrofit2.Call<ResponseBody> call = RetrofitUtil.getApiService().getUsername(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 解析返回的数据
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        // 使用Gson将responseData解析成对应的数据对象
                        Gson gson = new Gson();
                        JsonResponseEntity userData = gson.fromJson(responseData, JsonResponseEntity.class);

                        // 在这里处理获取到的用户信息和播放历史等数据
                        String userName = userData.getUserName();
//                        String accessToken = userData.getAccessToken();
//                        Object recentlyPlayedTracks = userData.getRecentlyPlayedTracks();

                        // 调用startActivity方法，将数据传递给RedirectActivity
                        //startActivity(token, userName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // 修改这里：定义一个私有方法用于启动RedirectActivity，并传递参数
//    private void startActivity(String accessToken, String userName) {
//        Intent intent = new Intent(LoginActivity.this, RedirectActivity.class);
//        intent.putExtra("accessToken", accessToken);
//        intent.putExtra("userName", userName);
//        // 因为recentlyPlayedTracks是一个Object类型，如果需要传递自定义对象，需要将其转换成JSON字符串或其他可序列化类型
////        // 在这里，我们使用Gson将Object转换成JSON字符串
////        Gson gson = new Gson();
////        String recentlyPlayedTracksJson = gson.toJson(recentlyPlayedTracks);
////        intent.putExtra("recentlyPlayedTracks", recentlyPlayedTracksJson);
//        startActivity(intent);
//    }
}

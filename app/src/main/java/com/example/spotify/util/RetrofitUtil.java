package com.example.spotify.util;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.1.115:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    private static ApiService apiService = retrofit.create(ApiService.class);

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static ApiService getApiService() {
        return apiService;
    }
    public static HashMap<String, String> headersMap = new HashMap<>();

}

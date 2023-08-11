package com.example.spotify.util;

import java.util.HashMap;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {
    private static Retrofit retrofit=new Retrofit.Builder()
            .baseUrl("http://192.168.1.97:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static Retrofit retrofit_sportfy=new Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static ApiService apiService = retrofit.create(ApiService.class);
    private static ApiService apiService_sportfy = retrofit_sportfy.create(ApiService.class);

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static ApiService getApiService() {
        return apiService;
    }
    public static HashMap<String, String> headersMap = new HashMap<>();
    public static ApiService getSportfyApiService() {

        return apiService_sportfy;
    }

}

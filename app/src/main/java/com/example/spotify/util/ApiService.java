package com.example.spotify.util;
import com.example.spotify.model.dto.ReceivedLocationDTO;
import com.example.spotify.model.dto.SongDTO;
import com.example.spotify.model.dto.UserDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/login")
    Call<ResponseBody> loginCheck(@Body UserDTO user);

    @POST("/location")
    Call<List<SongDTO>> getRecommendationSongByLocation(@Body ReceivedLocationDTO location);

    @POST("/time")
    Call<List<SongDTO>> getRecommendationSongByTime(@Body ReceivedLocationDTO location);

    @GET("/holidays/android")
    Call<List<SongDTO>> holidayCheck();

    @POST("/after")
    Call<List<SongDTO>> getSongsAfterLogin(@Body ReceivedLocationDTO location);

    @GET("/publicForAndroid")
    Call<List<SongDTO>> getSongsPublic();

}
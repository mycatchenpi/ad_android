package com.example.spotify.util;
import com.example.spotify.dto.ReceivedLocationDTO;
import com.example.spotify.dto.SongDTO;
import com.example.spotify.dto.UserDTO;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @POST("/public")
    Call<List<SongDTO>> getSongsPublic(@Body ReceivedLocationDTO location);

}
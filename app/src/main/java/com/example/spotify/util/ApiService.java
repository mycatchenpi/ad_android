package com.example.spotify.util;
import com.example.spotify.dto.ReceivedLocationDTO;

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
    @GET("/")
    Call<ResponseBody> getAuthorizationURL();

    @GET("/callback")
    Call<ResponseBody> handleCallback();

    @GET("/username")
    Call<ResponseBody> getUsername(String token);

    @GET("redirect")
    Call<ResponseBody> redirectToCallbackSuccess();

    @GET("userPlaylists")
    Call<ResponseBody> getUserPlaylists();

    @GET("recentTracks")
    Call<ResponseBody> getRecentTracks();

    @GET("song/{id}")
    Call<ResponseBody> getSingleTrack(@Path("id") int id);

    @GET("v1/tracks/{id}")
    Call<ResponseBody> getSportfySingleTrack(@Path("id") String id, @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("/login/android")
    Call<ResponseBody> loginCheck(@Field("username") String username, @Field("password") String password);

    @GET("/holidays")
    Call<ResponseBody> holidayCheck();

    @POST("/location")
    Call<ResponseBody> getRecommendationSongByLocation(@Body ReceivedLocationDTO location);

    @GET("/get-access-token")
    Call<ResponseBody> getToken();

    @GET("/get-track-details")
    Call<ResponseBody> getTrackInfo(@Query("trackId") String trackId);
}
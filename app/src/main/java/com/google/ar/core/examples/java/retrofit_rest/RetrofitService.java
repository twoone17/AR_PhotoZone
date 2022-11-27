package com.google.ar.core.examples.java.retrofit_rest;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {

    @POST("/tmap/routes/pedestrian")
    Call<Object> getPosts(
            @Query("version") int version,
            @Query("callback") String callback,
            @Query("appkey") String appKey,
            @Query("startX") Double st_lng,
            @Query("startY") Double st_lat,
            @Query("endX") Double ed_lng,
            @Query("endY") Double ed_lat,
            @Query("startName") String startName,
            @Query("endName") String endName);
}

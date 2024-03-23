package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserFeaturesService {

    @POST("user-features/swipe-of-darshans")
    Call<Void> swipedDarshan(@Body User user);

    @GET("user-features/swipe-of-darshans")
    Call<Boolean> isSwipeDarshanPugAnimationEnabled(@Query("userId") String userId);
}

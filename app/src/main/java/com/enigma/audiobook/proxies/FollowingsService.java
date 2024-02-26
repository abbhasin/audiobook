package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.Following;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FollowingsService {

    @POST("followings/follow")
    Call<Void> addFollowing(@Body Following following);

    @POST("followings/unfollow")
    Call<Void> removeFollowing(@Body Following following);
}

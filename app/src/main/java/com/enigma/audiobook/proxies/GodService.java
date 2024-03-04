package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.responses.GodForUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GodService {

    @GET("gods/users")
    Call<List<GodForUser>> getGodsForUser(@Query("limit") int limit,
                                          @Query("userId") String userId,
                                          @Query("onlyFollowed") boolean onlyFollowed);

    @GET("gods/users/pagination")
    Call<List<GodForUser>> getGodsForUserNext(@Query("limit") int limit,
                                              @Query("userId") String userId,
                                              @Query("lastGodId") String lastGodId,
                                              @Query("onlyFollowed") boolean onlyFollowed);


}

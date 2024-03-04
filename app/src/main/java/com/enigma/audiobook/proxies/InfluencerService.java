package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.responses.InfluencerForUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface InfluencerService {

    @GET("influencers/users")
    Call<List<InfluencerForUser>> getInfleuncersForUser(@Query("limit") int limit,
                                                        @Query("userId") String userId,
                                                        @Query("onlyFollowed") boolean onlyFollowed);

    @GET("influencers/users/pagination")
    Call<List<InfluencerForUser>> getInfleuncersForUserNext(@Query("limit") int limit,
                                                            @Query("userId") String userId,
                                                            @Query("lastInfluencerId") String lastGodId,
                                                            @Query("onlyFollowed") boolean onlyFollowed);
}

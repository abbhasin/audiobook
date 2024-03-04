package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.responses.MandirForUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MandirService {

    @GET("mandirs/users")
    Call<List<MandirForUser>> getMandirsForUser(@Query("limit") int limit,
                                                @Query("userId") String userId,
                                                @Query("onlyFollowed") boolean onlyFollowed);

    @GET("mandirs/users/pagination")
    Call<List<MandirForUser>> getMandirsForUserNext(@Query("limit") int limit,
                                                    @Query("userId") String userId,
                                                    @Query("lastMandirId") String lastMandirId,
                                                    @Query("onlyFollowed") boolean onlyFollowed);
}

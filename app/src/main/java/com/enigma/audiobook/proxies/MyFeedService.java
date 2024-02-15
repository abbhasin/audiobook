package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.requests.CuratedFeedRequest;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MyFeedService {
    @POST("feed/page")
    Call<FeedPageResponse> getFeedPage(@Body CuratedFeedRequest curatedFeedRequest);
}

package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.requests.CuratedFeedRequest;
import com.enigma.audiobook.backend.models.requests.GodFeedRequest;
import com.enigma.audiobook.backend.models.requests.InfluencerFeedRequest;
import com.enigma.audiobook.backend.models.requests.MandirFeedRequest;
import com.enigma.audiobook.backend.models.responses.FeedPageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface MyFeedService {
    @POST("feed/page")
    Call<FeedPageResponse> getFeedPage(@Body CuratedFeedRequest curatedFeedRequest);

    @POST("feed/god/page")
    Call<FeedPageResponse> getFeedPageOfGod(@Body GodFeedRequest curatedFeedRequest);

    @POST("feed/mandir/page")
    Call<FeedPageResponse> getFeedPageOfMandir(@Body MandirFeedRequest curatedFeedRequest);

    @POST("feed/influencer/page")
    Call<FeedPageResponse> getFeedPageOfInfluencer(@Body InfluencerFeedRequest curatedFeedRequest);
}

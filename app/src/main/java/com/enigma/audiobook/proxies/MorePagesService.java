package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.responses.MorePages;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MorePagesService {

    @GET("more-pages")
    Call<MorePages> getMorePages(@Query("userId") String userId);
}

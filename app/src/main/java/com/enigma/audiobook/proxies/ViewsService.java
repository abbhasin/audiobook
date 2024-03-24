package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.DarshanView;
import com.enigma.audiobook.backend.models.View;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ViewsService {

    @POST("views/posts")
    Call<Void> addViewing(@Body View view);

    @POST("views/darshans")
    Call<Void> addDarshanViewing(@Body DarshanView view);
}

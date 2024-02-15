package com.enigma.audiobook.proxies;

import retrofit2.Call;
import retrofit2.http.GET;
import com.enigma.audiobook.backend.models.Darshan;

import java.util.List;

public interface DarshanService {

    @GET("darshans")
    Call<List<Darshan>> getDarshans();
}

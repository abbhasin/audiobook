package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.User;

import retrofit2.Call;
import retrofit2.http.POST;

public interface UserRegistrationService {

    @POST("users")
    Call<User> createUser();
}

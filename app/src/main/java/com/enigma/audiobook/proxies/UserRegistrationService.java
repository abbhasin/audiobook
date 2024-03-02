package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.User;
import com.enigma.audiobook.backend.models.requests.UserRegistrationInfo;
import com.enigma.audiobook.backend.models.responses.UserAssociationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserRegistrationService {

    @POST("users")
    Call<User> createUser();

    @POST("users/associations")
    Call<UserAssociationResponse>
    associateAuthenticatedUser(@Body UserRegistrationInfo userRegistrationInfo);
}

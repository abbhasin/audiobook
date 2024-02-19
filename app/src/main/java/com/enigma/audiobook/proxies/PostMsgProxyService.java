package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.requests.PostContentUploadReq;
import com.enigma.audiobook.backend.models.requests.PostInitRequest;
import com.enigma.audiobook.backend.models.responses.PostCompletionResponse;
import com.enigma.audiobook.backend.models.responses.PostInitResponse;

import retrofit2.Call;
import retrofit2.http.POST;

public interface PostMsgProxyService {

    @POST("posts/initialization")
    Call<PostInitResponse> initPost(PostInitRequest initRequest);

    @POST("posts/update-completion")
    Call<PostCompletionResponse> completePost(PostContentUploadReq contentUploadReq);
}

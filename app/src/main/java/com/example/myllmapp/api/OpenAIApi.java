package com.example.myllmapp.api;

import com.example.myllmapp.model.OpenAIRequest;
import com.example.myllmapp.model.OpenAIResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OpenAIApi {
    @POST("v1/chat/completions")
    Call<OpenAIResponse> createChatCompletion(
            @Header("Authorization") String authorization,
            @Body OpenAIRequest request
    );
}
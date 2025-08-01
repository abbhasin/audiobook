package com.enigma.audiobook.proxies;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitFactory {
//    private static String BASE_URL = "http://192.168.1.7:8080";
private static String BASE_URL = "https://backend.ekbhagwan.com";
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private static volatile RetrofitFactory INSTANCE;

    public static synchronized RetrofitFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RetrofitFactory();
        }
        return INSTANCE;
    }

    public <T> T createService(Class<T> clazz) {
        return retrofit.create(clazz);
    }
}

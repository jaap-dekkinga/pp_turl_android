package com.tuneurl.podcastplayer.core.service;

import com.tuneurl.podcastplayer.core.ClientConfig;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class UserAgentInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", ClientConfig.USER_AGENT)
                .build());
    }
}

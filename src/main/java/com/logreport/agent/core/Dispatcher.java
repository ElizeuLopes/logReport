package com.logreport.agent.core;

import com.google.gson.Gson;
import com.logreport.agent.config.AgentConfig;
import com.logreport.agent.util.GsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Component;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class Dispatcher {

    private final AgentConfig config;
    private final Gson gson;
    private ExecutorService executor;
    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(config.getMaxThreads());
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    public void dispatch(String type, Object data) {
        executor.submit(() -> {
            String json = GsonUtils.toJsonWithMetadata(gson, data, config);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(config.getMetricEndpoint())
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("[LogReport] Failed to send metrics: " + response.code());
                }
            } catch (Exception e) {
                System.err.println("[LogReport] Error sending metrics: " + e.getMessage());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
package com.logreport.agent.core;

import com.logreport.agent.config.AgentConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class HealthChecker {

    private final AgentConfig config;
    private final Dispatcher dispatcher;
    private final OkHttpClient httpClient = new OkHttpClient();
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkHealth, 0,
                config.getHealthCheckInterval(), TimeUnit.SECONDS);
    }

    private void checkHealth() {
        try {
            Request request = new Request.Builder()
                    .url(config.getHealthUrl())
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                HealthStatus status = new HealthStatus(
                        response.isSuccessful(),
                        System.currentTimeMillis(),
                        config
                );
                dispatcher.dispatch("health", status);
            }
        } catch (Exception e) {
            System.err.println("[LogReport] Health check failed: " + e.getMessage());
            dispatcher.dispatch("health", new HealthStatus(false, System.currentTimeMillis(), config));
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class HealthStatus {
        private final boolean healthy;
        private final long timestamp;
        private final String service;
        private final String pod;
        private final String container;

        public HealthStatus(boolean healthy, long timestamp, AgentConfig config) {
            this.healthy = healthy;
            this.timestamp = timestamp;
            this.service = config.getServiceName();
            this.pod = config.getPodName();
            this.container = config.getContainerId();
        }
    }
}
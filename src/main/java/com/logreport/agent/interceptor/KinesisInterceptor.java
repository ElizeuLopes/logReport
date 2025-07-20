package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;

import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KinesisInterceptor {

    private final Dispatcher dispatcher;

    @RuntimeType
    public Object interceptPutRecord(
            @SuperCall Callable<?> callable,
            @Argument(0) PutRecordRequest request) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            sendKinesisMetric("putRecord", request.streamName(), 1, duration);
        }
    }

    @RuntimeType
    public Object interceptPutRecords(
            @SuperCall Callable<?> callable,
            @Argument(0) PutRecordsRequest request) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            sendKinesisMetric("putRecords", request.streamName(),
                    request.records().size(), duration);
        }
    }

    private void sendKinesisMetric(String action, String stream, int recordCount, long duration) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "kinesis");
        data.put("action", action);
        data.put("stream", stream);
        data.put("recordCount", recordCount);
        data.put("duration", duration);

        dispatcher.dispatch("kinesis", data);
    }
}
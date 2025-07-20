package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaInterceptor {

    private final Dispatcher dispatcher;

    @RuntimeType
    public Object interceptProducer(
            @SuperCall Callable<?> callable,
            @Argument(0) Object record) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            Map<String, Object> data = new HashMap<>();
            data.put("type", "kafka");
            data.put("topic", getTopic(record));
            data.put("action", "produce");
            data.put("duration", duration);

            dispatcher.dispatch("kafka", data);
        }
    }

    private String getTopic(Object record) {
        try {
            return (String) record.getClass().getMethod("topic").invoke(record);
        } catch (Exception e) {
            return "unknown-topic";
        }
    }
}
package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BatchInterceptor {

    private final Dispatcher dispatcher;

    @RuntimeType
    public Object interceptJob(
            @SuperCall Callable<?> callable,
            @Argument(0) JobExecution jobExecution) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            sendBatchMetric("job", jobExecution.getJobInstance().getJobName(), duration);
        }
    }

    @RuntimeType
    public Object interceptStep(
            @SuperCall Callable<?> callable,
            @Argument(0) StepExecution stepExecution) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            sendBatchMetric("step", stepExecution.getStepName(), duration);
        }
    }

    private void sendBatchMetric(String type, String name, long duration) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "batch");
        data.put("executionType", type);
        data.put("name", name);
        data.put("duration", duration);

        dispatcher.dispatch("batch", data);
    }
}
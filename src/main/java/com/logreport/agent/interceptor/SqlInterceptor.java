package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlInterceptor {

    private final Dispatcher dispatcher;

    @RuntimeType
    public Object intercept(
            @SuperCall Callable<?> callable,
            @Argument(0) String sql) throws Exception {

        long start = System.nanoTime();
        boolean success = true;

        try {
            return callable.call();
        } catch (SQLException e) {
            success = false;
            throw e;
        } finally {
            long duration = System.nanoTime() - start;
            Map<String, Object> data = new HashMap<>();
            data.put("type", "sql");
            data.put("query", sql);
            data.put("duration", duration);
            data.put("success", success);

            dispatcher.dispatch("sql", data);
        }
    }
}
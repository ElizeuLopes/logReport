package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamelInterceptor {

    private final Dispatcher dispatcher;

    @RuntimeType
    public Object intercept(
            @SuperCall Callable<?> callable,
            @Argument(0) Exchange exchange) throws Exception {

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            Map<String, Object> data = new HashMap<>();
            data.put("type", "camel");
            data.put("exchangeId", exchange.getExchangeId());
            data.put("fromRoute", exchange.getFromRouteId());
            data.put("duration", duration);

            dispatcher.dispatch("camel", data);
        }
    }
}
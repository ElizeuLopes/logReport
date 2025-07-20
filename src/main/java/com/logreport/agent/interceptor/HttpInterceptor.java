package com.logreport.agent.interceptor;

import com.logreport.agent.core.Dispatcher;
import com.logreport.agent.model.LogRequest;
import com.logreport.agent.model.Payload;
import com.logreport.agent.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@RequiredArgsConstructor
public class HttpInterceptor {

    private final Dispatcher dispatcher;
    private final ServletUtils servletUtils;

    @RuntimeType
    public Object intercept(
            @SuperCall Callable<?> callable,
            @AllArguments Object[] args) throws Exception {

        HttpServletRequest request = null;
        HttpServletResponse response = null;
        ServletUtils.RequestResponseWrapper responseWrapper = null;

        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) request = (HttpServletRequest) arg;
            if (arg instanceof HttpServletResponse) {
                response = (HttpServletResponse) arg;
                responseWrapper = new ServletUtils.RequestResponseWrapper((HttpServletResponse) response);
            }
        }

        if (request == null || response == null) {
            return callable.call();
        }

        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long duration = System.nanoTime() - start;
            LogRequest logRequest = buildLogRequest(request, response, responseWrapper, duration);
            dispatcher.dispatch("http", logRequest);
        }
    }

    private LogRequest buildLogRequest(HttpServletRequest request,
                                       HttpServletResponse response,
                                       ServletUtils.RequestResponseWrapper wrapper,
                                       long duration) {
        LogRequest logRequest = new LogRequest();
        logRequest.setMethod(request.getMethod());
        logRequest.setDuration(duration);
        logRequest.setHttpStatusCode(wrapper != null ? wrapper.getStatus() : response.getStatus());

        Payload payload = new Payload();
        payload.setParams(ServletUtils.extractParams(request));
        payload.setRequest(ServletUtils.captureRequestBody(request));
        payload.setResponse(wrapper != null ? wrapper.getContent() : "");

        logRequest.setPayload(payload);
        return logRequest;
    }
}
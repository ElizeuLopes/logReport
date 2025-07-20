package com.logreport.agent.util;


import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.util.Collections;

public class ServletUtils {

    public static Map<String, List<String>> extractParams(HttpServletRequest request) {
        Map<String, List<String>> params = new HashMap<>();

        // Query parameters
        request.getParameterMap().forEach((key, values) ->
                params.put((String) key, Arrays.asList(values.toString())));

        // Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            params.put(name, Collections.list(request.getHeaders(name)));
        }

        return params;
    }

    public static String captureRequestBody(HttpServletRequest request) {
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static class RequestResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final ServletOutputStream outputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            public void write(int b) {
                buffer.write(b);
            }
        };
        private final PrintWriter writer = new PrintWriter(outputStream);

        public RequestResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        public String getContent() {
            writer.flush();
            return buffer.toString();
        }

        public int getStatus() {
            return ((HttpServletResponse) getResponse()).getStatus();
        }
    }
}
package com.logreport.agent.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.logreport.agent.config.AgentConfig;

public class GsonUtils {

    private static final Gson gson = new GsonBuilder().create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static String toJsonWithMetadata(Gson gson, Object data, AgentConfig config) {
        JsonObject json = new JsonObject();
        json.add("data", gson.toJsonTree(data));
        json.addProperty("service", config.getServiceName());
        json.addProperty("pod", config.getPodName());
        json.addProperty("container", config.getContainerId());
        json.addProperty("node", config.getNodeName());
        json.addProperty("namespace", config.getNamespace());
        json.addProperty("timestamp", System.currentTimeMillis());
        return json.toString();
    }
}
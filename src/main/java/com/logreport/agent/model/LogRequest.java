package com.logreport.agent.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LogRequest {
    private String method;
    private long duration;
    private String appName;
    private int httpStatusCode;
    private Payload payload;
    private String podName;
    private String containerId;
}
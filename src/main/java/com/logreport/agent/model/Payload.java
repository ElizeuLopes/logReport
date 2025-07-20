package com.logreport.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Payload {
    private Map<String, List<String>> params;
    private String request;
    private String response;
}
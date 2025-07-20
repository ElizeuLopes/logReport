package com.logreport.agent.config;

import com.logreport.agent.util.EnvironmentUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Getter
@Component
public class AgentConfig {

    @Value("${SERVICE_NAME:unknown-service}")
    private String serviceName;

    @Value("${METRIC_ENDPOINT}")
    private String metricEndpoint;

    @Value("${HEALTH_URL:http://localhost:8080/actuator/health}")
    private String healthUrl;

    @Value("${METRIC_THREADS:10}")
    private int maxThreads;

    @Value("${HEALTH_CHECK_INTERVAL:30}")
    private int healthCheckInterval;

    @Value("${MAX_PAYLOAD_SIZE:10240}") // 10KB
    private int maxPayloadSize;

    @Value("${POD_NAME:unknown-pod}")
    private String podName;

    @Value("${CONTAINER_ID:unknown-container}")
    private String containerId;

    @Value("${NODE_NAME:unknown-node}")
    private String nodeName;

    @Value("${NAMESPACE:default}")
    private String namespace;

    @PostConstruct
    public void init() {
        // Preencher valores do Kubernetes se disponíveis
        this.containerId = EnvironmentUtils.getContainerId();
        this.podName = EnvironmentUtils.getPodName();
        this.nodeName = EnvironmentUtils.getNodeName();
        this.namespace = EnvironmentUtils.getNamespace();
    }
}
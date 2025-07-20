package com.logreport.agent.core;

import com.logreport.agent.config.AgentConfig;
import jakarta.annotation.PostConstruct;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.springframework.stereotype.Component;
import com.logreport.agent.interceptor.*;
import lombok.RequiredArgsConstructor;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;


@Component
@RequiredArgsConstructor
public class Agent {

    private final AgentConfig config;
    private final HttpInterceptor httpInterceptor;
    private final SqlInterceptor sqlInterceptor;
    private final KafkaInterceptor kafkaInterceptor;
    private final RabbitMQInterceptor rabbitMQInterceptor;
    private final KinesisInterceptor kinesisInterceptor;
    private final CamelInterceptor camelInterceptor;
    private final BatchInterceptor batchInterceptor;

    @PostConstruct
    public void init() {
        System.out.println("[LogReportAgent] Starting agent for service: " + config.getServiceName());
    }

    public static void premain(String args, Instrumentation inst) {
        AgentInitializer.initialize(inst);
    }
    public void install(Instrumentation inst) {
        new AgentBuilder.Default()
                // Spring Web
                .type(hasSuperType(named("org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("handle"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(httpInterceptor)))

                // JDBC
                .type(hasSuperType(named("java.sql.Statement")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(nameStartsWith("execute"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(sqlInterceptor)))

                // Kafka Producer
                .type(hasSuperType(named("org.apache.kafka.clients.producer.Producer")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("send"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(kafkaInterceptor)))

                // RabbitMQ
                .type(hasSuperType(named("com.rabbitmq.client.Channel")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("basicPublish"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(rabbitMQInterceptor)))

                // Kinesis
                .type(named("software.amazon.awssdk.services.kinesis.KinesisClient"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("putRecord").or(named("putRecords")))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(kinesisInterceptor)))

                // Spring Batch
                .type(hasSuperType(named("org.springframework.batch.core.step.AbstractStep")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("execute"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(batchInterceptor)))

                // Apache Camel
                .type(hasSuperType(named("org.apache.camel.Processor")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(named("process"))
                                .intercept(net.bytebuddy.implementation.MethodDelegation.to(camelInterceptor)))

                .installOn(inst);
    }
}
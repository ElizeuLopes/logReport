package com.logreport.agent.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.instrument.Instrumentation;

public class AgentInitializer {
    private static AnnotationConfigApplicationContext context;

    public static void initialize(Instrumentation inst) {
        context = new AnnotationConfigApplicationContext();
        context.scan("com.logreport.agent");
        context.refresh();

        Agent agent = context.getBean(Agent.class);
        agent.install(inst);
    }

    public static void shutdown() {
        if (context != null) {
            context.close();
        }
    }
}
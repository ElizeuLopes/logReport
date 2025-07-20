package com.logreport.agent.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class EnvironmentUtils {

    public static String getContainerId() {
        return readFirstLine("/proc/self/cgroup")
                .map(line -> {
                    if (line.contains("docker")) return line.substring(line.lastIndexOf("/") + 1);
                    if (line.contains("containerd")) return line.substring(line.lastIndexOf("/") + 1);
                    return null;
                })
                .orElse(System.getenv("HOSTNAME"));
    }

    public static String getPodName() {
        return System.getenv("POD_NAME");
    }

    public static String getNodeName() {
        return System.getenv("NODE_NAME");
    }

    public static String getNamespace() {
        return System.getenv("POD_NAMESPACE");
    }

    private static Optional<String> readFirstLine(String path) {
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines.findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
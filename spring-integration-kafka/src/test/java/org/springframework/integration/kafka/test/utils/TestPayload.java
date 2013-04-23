package org.springframework.integration.kafka.test.utils;

import java.io.Serializable;

public class TestPayload implements Serializable {

    private static final long serialVersionUID = -8560378224929007403L;
    private final String part1;
    private final String part2;

    public TestPayload(final String part1, final String part2){
        this.part1 = part1;
        this.part2 = part2;
    }

    public String getPart1() {
        return part1;
    }

    public String getPart2() {
        return part2;
    }
}

package org.springframework.integration.kafka.test.utils;

import java.io.Serializable;

public class TestKey implements Serializable {

    private static final long serialVersionUID = -6415387283545560656L;
    private final String keyPart1;
    private final String keyPart2;

    public TestKey(String keyPart1, String keyPart2) {
        this.keyPart1 = keyPart1;
        this.keyPart2 = keyPart2;
    }

    public String getKeyPart1() {
        return keyPart1;
    }

    public String getKeyPart2() {
        return keyPart2;
    }

}

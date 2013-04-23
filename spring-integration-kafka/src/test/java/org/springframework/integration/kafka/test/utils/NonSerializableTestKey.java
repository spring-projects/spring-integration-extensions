package org.springframework.integration.kafka.test.utils;

public class NonSerializableTestKey {
    private final String keyPart1;
        private final String keyPart2;

        public NonSerializableTestKey(String keyPart1, String keyPart2) {
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

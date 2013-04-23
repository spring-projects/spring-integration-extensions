package org.springframework.integration.kafka.test.utils;

public class NonSerializableTestPayload {

    private final String part1;
        private final String part2;

        public NonSerializableTestPayload(final String part1, final String part2){
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

package org.springframework.integration.kafka.outbound;

import kafka.producer.Partitioner;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/29/13
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class NaivePartitioner<T> implements Partitioner<T> {

    /**
     * Uses the key to calculate a partition bucket id for routing
     * the data to the appropriate broker partition
     * @return an integer between 0 and numPartitions-1
     */
    @Override
    public int partition(T key, int numPartitions) {
        return Math.abs(key.hashCode()) % numPartitions;
    }
}

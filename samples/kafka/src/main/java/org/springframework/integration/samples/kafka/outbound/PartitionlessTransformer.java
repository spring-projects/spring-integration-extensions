package org.springframework.integration.samples.kafka.outbound;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class PartitionlessTransformer implements Transformer {

    @Override
    @SuppressWarnings("unchecked")
    public Message<?> transform(Message<?> message) {

        Map<String, Map<Integer, List<Object>>> origData =
                (Map<String, Map<Integer, List<Object>>>) message.getPayload();

        Map<String, List<Object>> nonPartitionedData = new HashMap<String, List<Object>>();

        for(String topic : origData.keySet()) {
            Map<Integer, List<Object>> partitionedData = origData.get(topic);
            Collection<List<Object>> nonPartitionedDataFromTopic = partitionedData.values();
            List<Object> mergedList = new ArrayList<Object>();
            for (List<Object> l : nonPartitionedDataFromTopic){
                mergedList.addAll(l);
            }
            nonPartitionedData.put(topic, mergedList);
        }

        return MessageBuilder.withPayload(nonPartitionedData).build();
    }
}

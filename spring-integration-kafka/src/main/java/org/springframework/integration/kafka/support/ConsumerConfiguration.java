package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.MessagingException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Soby Chacko
 */
public class ConsumerConfiguration {
    protected final Log logger = LogFactory.getLog(getClass());

    private final ConsumerMetadata consumerMetadata;
    private final ConsumerConnector consumerConnector;
    private volatile int count = 0;
    private int maxMessages = 1;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private BlockingQueue<MessageAndMetadata> messageLeftOverFromPreviousPoll = new LinkedBlockingDeque<MessageAndMetadata>();

    public ConsumerConfiguration(final ConsumerMetadata consumerMetadata,
                                 final ConsumerConnector consumerConnector) {
        this.consumerMetadata = consumerMetadata;
        this.consumerConnector = consumerConnector;
    }

    public ConsumerMetadata getConsumerMetadata() {
        return consumerMetadata;
    }

    public ConsumerConnector getConsumerConnector() {
        return consumerConnector;
    }

    public Map<String, List<Object>> receive() {
        count = messageLeftOverFromPreviousPoll.size();
        final Map<String, List<Object>> messages = new ConcurrentHashMap<String, List<Object>>();

        populateAnyLeftOverMessages(messages);

        final List<Callable<List<MessageAndMetadata>>> tasks = new LinkedList<Callable<List<MessageAndMetadata>>>();
        final Object lock = new Object();

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = getConsumerMapWithMessageStreams();
        for (List<KafkaStream<byte[], byte[]>> streams : consumerMap.values()) {
            for (final KafkaStream<byte[], byte[]> stream : streams) {
                tasks.add(new Callable<List<MessageAndMetadata>>() {
                    @Override
                    public List<MessageAndMetadata> call() throws Exception {
                        final List<MessageAndMetadata> rawMessages = new ArrayList<MessageAndMetadata>();
                        try {
                            while (count < maxMessages) {
                                final MessageAndMetadata messageAndMetadata = stream.iterator().next();
                                synchronized (lock) {
                                    if (count < maxMessages) {
                                        rawMessages.add(messageAndMetadata);
                                    } else {
                                        messageLeftOverFromPreviousPoll.put(messageAndMetadata);
                                    }
                                    count++;
                                }
                            }
                        } catch (ConsumerTimeoutException cte) {
                            logger.info("Consumer timed out");
                        }
                        return rawMessages;
                    }
                });
            }
        }
        return executeTasks(tasks, messages);
    }

    private Map<String, List<Object>> executeTasks(List<Callable<List<MessageAndMetadata>>> tasks, Map<String, List<Object>> messages) {
        try {
            for (Future<List<MessageAndMetadata>> result : executorService.invokeAll(tasks)) {
                if (!result.get().isEmpty()) {
                    final String topic = result.get().get(0).topic();
                    if (!messages.containsKey(topic)) {
                        messages.put(topic, getPayload(result.get()));
                    } else {
                        final List<Object> exsitingPayloadList = messages.get(topic);
                        exsitingPayloadList.addAll(getPayload(result.get()));
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        }
        if (messages.isEmpty()) {
            return null;
        }
        return messages;
    }

    private void populateAnyLeftOverMessages(Map<String, List<Object>> messages) {
        while (messageLeftOverFromPreviousPoll.iterator().hasNext()) {
            MessageAndMetadata mamd = messageLeftOverFromPreviousPoll.iterator().next();
            final List<Object> l = new ArrayList<Object>();
            l.add(mamd.message());
            messages.put(mamd.topic(), l);
        }
        messageLeftOverFromPreviousPoll.clear();
    }

    private List<Object> getPayload(List<MessageAndMetadata> messageAndMetadatas) {
        final List<Object> payloadList = new ArrayList<Object>();

        for (MessageAndMetadata messageAndMetadata : messageAndMetadatas) {
            payloadList.add(messageAndMetadata.message());
        }
        return payloadList;
    }

    public Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams() {
        if (consumerMetadata.getKafkaDecoder() != null) {
            return consumerConnector.createMessageStreams(
                    consumerMetadata.getTopicStreamMap(),
                    consumerMetadata.getKafkaDecoder(),
                    consumerMetadata.getKafkaDecoder());
        }
        return consumerConnector.createMessageStreams(consumerMetadata.getTopicStreamMap());
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }
}

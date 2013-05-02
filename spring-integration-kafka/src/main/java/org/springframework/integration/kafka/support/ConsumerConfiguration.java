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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Soby Chacko
 */
public class ConsumerConfiguration {
    protected final Log logger = LogFactory.getLog(getClass());

    private final ConsumerMetadata consumerMetadata;
    private final ConsumerConnectionProvider consumerConnectionProvider;
    private final MessageLeftOverTracker messageLeftOverTracker;
    private ConsumerConnector consumerConnector;
    private volatile int count = 0;
    private int maxMessages = 1;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public ConsumerConfiguration(final ConsumerMetadata consumerMetadata,
                                 final ConsumerConnectionProvider consumerConnectionProvider,
                                 final MessageLeftOverTracker messageLeftOverTracker) {
        this.consumerMetadata = consumerMetadata;
        this.consumerConnectionProvider = consumerConnectionProvider;
        this.messageLeftOverTracker = messageLeftOverTracker;
    }

    public ConsumerMetadata getConsumerMetadata() {
        return consumerMetadata;
    }

    public Map<String, Map<Integer, List<Object>>> receive() {
        count = messageLeftOverTracker.getCurrentCount();
        Map<String, Map<Integer, List<Object>>> messages = new ConcurrentHashMap<String, Map<Integer, List<Object>>>();

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
                                        count++;
                                    } else {
                                        messageLeftOverTracker.addMessageAndMetadata(messageAndMetadata);
                                    }
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

    private Map<String, Map<Integer, List<Object>>> executeTasks(List<Callable<List<MessageAndMetadata>>> tasks,
                                                                 final Map<String, Map<Integer, List<Object>>> messages) {
        try {
            for (Future<List<MessageAndMetadata>> result : executorService.invokeAll(tasks)) {
                if (!result.get().isEmpty()) {
                    final String topic = result.get().get(0).topic();
                    if (!messages.containsKey(topic)) {
                        messages.put(topic, getPayload(result.get()));
                    } else {

                        final Map<Integer, List<Object>> existingPayloadMap = messages.get(topic);
                        getPayload(result.get(), existingPayloadMap);
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

    private void populateAnyLeftOverMessages(Map<String, Map<Integer, List<Object>>> messages) {
        for (MessageAndMetadata mamd : messageLeftOverTracker.getMessageLeftOverFromPreviousPoll()) {
            final String topic = mamd.topic();
            if (!messages.containsKey(topic)) {
                final List<MessageAndMetadata> l = new ArrayList<MessageAndMetadata>();
                l.add(mamd);
                messages.put(topic, getPayload(l));
            } else {

                final Map<Integer, List<Object>> existingPayloadMap = messages.get(topic);
                final List<MessageAndMetadata> l = new ArrayList<MessageAndMetadata>();
                l.add(mamd);
                getPayload(l, existingPayloadMap);
            }
        }
        messageLeftOverTracker.clearMessagesLeftOver();
    }

    private Map<Integer, List<Object>> getPayload(List<MessageAndMetadata> messageAndMetadatas) {
        Map<Integer, List<Object>> payloadMap = new ConcurrentHashMap<Integer, List<Object>>();

        for (MessageAndMetadata messageAndMetadata : messageAndMetadatas) {
            if (!payloadMap.containsKey(messageAndMetadata.partition())) {
                List<Object> payload = new ArrayList<Object>();
                payload.add(messageAndMetadata.message());
                payloadMap.put(messageAndMetadata.partition(), payload);
            } else {
                List<Object> payload = payloadMap.get(messageAndMetadata.partition());
                payload.add(messageAndMetadata.message());
            }

        }
        return payloadMap;
    }

    private void getPayload(List<MessageAndMetadata> messageAndMetadatas, Map<Integer, List<Object>> existingPayloadMap) {

        for (MessageAndMetadata messageAndMetadata : messageAndMetadatas) {
            if (!existingPayloadMap.containsKey(messageAndMetadata.partition())) {
                List<Object> payload = new ArrayList<Object>();
                payload.add(messageAndMetadata.message());
                existingPayloadMap.put(messageAndMetadata.partition(), payload);
            } else {
                List<Object> payload = existingPayloadMap.get(messageAndMetadata.partition());
                payload.add(messageAndMetadata.message());
            }

        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams() {
        if (consumerMetadata.getValueDecoder() != null) {
            return getConsumerConnector().createMessageStreams(
                    consumerMetadata.getTopicStreamMap(),
                    consumerMetadata.getValueDecoder(),
                    consumerMetadata.getValueDecoder());
        }
        return getConsumerConnector().createMessageStreams(consumerMetadata.getTopicStreamMap());
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public ConsumerConnector getConsumerConnector() {
        if (consumerConnector == null) {
            consumerConnector = consumerConnectionProvider.getConsumerConnector();
            return consumerConnector;
        } else {
            return consumerConnector;
        }
    }
}

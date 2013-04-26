package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.support.MessageBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContext implements BeanFactoryAware {

    protected final Log logger = LogFactory.getLog(getClass());
    private Map<String, ConsumerConfiguration> consumerConfigurations;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Collection<ConsumerConfiguration> getConsumerConfigurations() {
        return consumerConfigurations.values();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        consumerConfigurations = ((ListableBeanFactory) beanFactory).getBeansOfType(ConsumerConfiguration.class);
    }

    public Message<Map<String, List<Object>>> receive(final int maxMessagesPerPoll) {
        final CountDownLatch latch = new CountDownLatch(maxMessagesPerPoll);
        final List<Callable<List<MessageAndMetadata>>> tasks = new LinkedList<Callable<List<MessageAndMetadata>>>();

        for (final ConsumerConfiguration consumerConfiguration : getConsumerConfigurations()) {
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConfiguration.getConsumerMapWithMessageStreams();
            for (List<KafkaStream<byte[], byte[]>> streams : consumerMap.values()) {
                for (final KafkaStream<byte[], byte[]> stream : streams) {
                    tasks.add(new Callable<List<MessageAndMetadata>>() {
                        @Override
                        public List<MessageAndMetadata> call() throws Exception {
                            final List<MessageAndMetadata> messages = new ArrayList<MessageAndMetadata>();
                            try {
                                while (latch.getCount() > 0) {
                                    final MessageAndMetadata messageAndMetadata = stream.iterator().next();
                                    messages.add(messageAndMetadata);
                                    latch.countDown();
                                }
                            } catch (ConsumerTimeoutException cte) {
                                logger.info("Consumer timed out");
                            } finally {
                                clearAllLatches(latch);
                            }
                            return messages;
                        }
                    });
                }
            }
        }
        return executeTasks(tasks);
    }

    private Message<Map<String, List<Object>>> executeTasks(List<Callable<List<MessageAndMetadata>>> tasks) {
        final Map<String, List<Object>> messages = new HashMap<String, List<Object>>();

        try {
            for (Future<List<MessageAndMetadata>> result : executorService.invokeAll(tasks)) {
                final String topic = result.get().get(0).topic();
                if (messages.get(topic) == null){
                    messages.put(topic, getPayload(result.get()));
                }
                else {
                    final List<Object> exsitingPayloadList = messages.get(topic);
                    exsitingPayloadList.addAll(getPayload(result.get()));
                }
            }
        } catch (Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        }
        if (messages.isEmpty()){
            return null;
        }
        return MessageBuilder.withPayload(messages).build();
    }

    private List<Object> getPayload(List<MessageAndMetadata> messageAndMetadatas) {
        final List<Object> payloadList = new ArrayList<Object>();

        for (MessageAndMetadata messageAndMetadata : messageAndMetadatas){
            payloadList.add(messageAndMetadata.message());
        }
        return payloadList;
    }

    private void clearAllLatches(CountDownLatch latch) {
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }
}

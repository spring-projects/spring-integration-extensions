package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
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

    public Collection<ConsumerConfiguration> getConsumerConfigurations() {
        return consumerConfigurations.values();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        consumerConfigurations = ((ListableBeanFactory) beanFactory).getBeansOfType(ConsumerConfiguration.class);
    }


    public Message<List<Object>> receive(final int maxMessagesPerPoll) {
        final CountDownLatch latch = new CountDownLatch(maxMessagesPerPoll);
        final List<Callable<List<Object>>> tasks = new LinkedList<Callable<List<Object>>>();
        int numOfStreams = 0;

        for (final ConsumerConfiguration consumerConfiguration : getConsumerConfigurations()) {
            final ConsumerConnector consumerConnector = consumerConfiguration.getConsumerConnector();

            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = getConsumerMapWithMessageStreams(
                    consumerConfiguration.getConsumerMetadata().getTopicStreamMap(), consumerConfiguration);
            numOfStreams += consumerConfiguration.getTotalStreams();
            Collection<List<KafkaStream<byte[], byte[]>>> s = consumerMap.values();

            while (s.iterator().hasNext()) {
                for (final KafkaStream<byte[], byte[]> stream : s.iterator().next()) {
                    tasks.add(new Callable<List<Object>>() {
                        @Override
                        public List<Object> call() throws Exception {
                            final List<Object> messages = new ArrayList<Object>();
                            try {
                                while (latch.getCount() > 0) {
                                    final Object obj = stream.iterator().next().message();
                                    //System.out.println("message: " + new String((byte[])obj));
                                    messages.add(obj);
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

        final ExecutorService executorService = Executors.newFixedThreadPool(numOfStreams);
        final List<Object> messageAggregate = new ArrayList<Object>();
        try {
            for (Future<List<Object>> result : executorService.invokeAll(tasks)) {
                messageAggregate.add(result.get());
            }

        } catch (Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        } finally {
            executorService.shutdown();
        }
        if (messageAggregate.isEmpty()){
                    return null;
                }

                return MessageBuilder.withPayload(messageAggregate).build();
    }

    private Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams(Map<String, Integer> topicStreamMap,
                                                                                            ConsumerConfiguration consumerConfiguration) {
        if (consumerConfiguration.getConsumerMetadata().getKafkaDecoder() != null) {
            return consumerConfiguration.getConsumerConnector().createMessageStreams(topicStreamMap,
                    consumerConfiguration.getConsumerMetadata().getKafkaDecoder(),
                    consumerConfiguration.getConsumerMetadata().getKafkaDecoder());
        }
        return consumerConfiguration.getConsumerConnector().createMessageStreams(topicStreamMap);

    }

    private void clearAllLatches(CountDownLatch latch) {
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }
}

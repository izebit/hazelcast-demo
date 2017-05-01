package ru.izebit.common.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.izebit.common.model.Message;

import java.util.function.Consumer;

import static ru.izebit.common.other.HazelcastEntityNames.MESSAGE_QUEUE;
import static ru.izebit.common.other.HazelcastEntityNames.NEWS_TOPIC;

/**
 * @author Artem Konovalov
 *         creation date  5/1/17.
 * @since 1.0
 */
@Service
public class MessageService {
    private final IQueue<Message> messageQueue;
    private final ITopic<String> newsTopic;

    public MessageService(@Autowired HazelcastInstance instance) {
        messageQueue = instance.getQueue(MESSAGE_QUEUE);
        newsTopic = instance.getTopic(NEWS_TOPIC);
    }

    @SneakyThrows
    public void send(Message message) {
        messageQueue.put(message);
    }

    @SneakyThrows
    public Message receive() {
        return messageQueue.poll();
    }

    public void removeAll() {
        messageQueue.clear();
    }

    public void addCallBackForNews(Consumer<String> consumer) {
        newsTopic.addMessageListener(message -> consumer.accept(message.getMessageObject()));
    }

    public void publish(String news) {
        newsTopic.publish(news);
    }
}

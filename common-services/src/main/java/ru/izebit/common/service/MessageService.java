package ru.izebit.common.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.izebit.common.model.Message;

import static ru.izebit.common.other.HazelcastEntityNames.MESSAGE_QUEUE;

/**
 * @author Artem Konovalov
 *         creation date  5/1/17.
 * @since 1.0
 */
@Service
public class MessageService {
    private final IQueue<Message> messageQueue;

    public MessageService(@Autowired HazelcastInstance instance) {
        messageQueue = instance.getQueue(MESSAGE_QUEUE);
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
}

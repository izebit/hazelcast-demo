package ru.izebit.server;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Scope;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author Artem Konovalov
 *         creation date  4/16/17.
 * @since 1.0
 */

@Scope(SCOPE_SINGLETON)
public class HazelcastCluster {
    private final List<HazelcastInstance> instances;
    private final Config config;

    public HazelcastCluster(int nodeCount, Config config) {
        this.instances = new CopyOnWriteArrayList<>();
        this.config = config;

        ensureSize(nodeCount);
    }

    //TODO there is something wrong with shutdown
    public synchronized void ensureSize(int nodeCount) {
        if (instances.size() == nodeCount)
            return;

        List<Future<?>> futures = new ArrayList<>();
        int diff = max(instances.size(), nodeCount) - min(instances.size(), nodeCount);

        if (instances.size() < nodeCount)
            for (int i = 0; i < diff; i++) {
                Future<?> future = commonPool().submit(() -> instances.add(newHazelcastInstance(config)));
                futures.add(future);
            }
        else
            for (int i = 0; i < diff; i++) {
                int index = i;
                Future<?> future = commonPool().submit(() -> instances.remove(index).shutdown());
                futures.add(future);
            }

        for (Future<?> future : futures)
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
    }

    @PreDestroy
    public void shutdown() {
        ensureSize(0);
    }
}

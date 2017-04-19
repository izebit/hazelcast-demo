package ru.izebit.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@SpringBootApplication
@Configuration
public class ApplicationLauncher {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance();
    }

    @Bean
    @Lazy(false)
    public HazelcastCluster hazelcastCluster() {
        int nodeCount = Integer.getInteger("hazelcast.node.count", 4);

        return new HazelcastCluster(nodeCount);
    }
}

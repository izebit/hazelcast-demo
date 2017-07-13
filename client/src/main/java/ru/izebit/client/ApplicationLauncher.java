package ru.izebit.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@SpringBootApplication
@Configuration
@ComponentScan("ru.izebit")
public class ApplicationLauncher {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }

    @Bean(destroyMethod = "shutdown", name = "hazelcast-client")
    public HazelcastInstance hazelcastInstance(@Autowired ClientConfig config) {
        return HazelcastClient.newHazelcastClient(config);
    }

    @Bean
    public ClientConfig clientConfig() {
        ClientConfig config = new ClientConfig();
        config
                .getSerializationConfig();
        return config;
    }
}

package ru.izebit.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MultiMapConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static ru.izebit.common.other.HazelcastEntityNames.FRIENDS_MULTI_MAP_NAME;
import static ru.izebit.common.other.HazelcastEntityNames.PERSON_MAP_NAME;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration(
        exclude = HazelcastAutoConfiguration.class
)
public class ApplicationLauncher {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }


    @Bean
    @Lazy(false)
    public HazelcastCluster hazelcastCluster(@Autowired Config config) {
        int nodeCount = Integer.getInteger("hazelcast.node.count", 1);

        return new HazelcastCluster(nodeCount, config);
    }

    @Bean
    public Config config() {
        Config config = new Config();

        MapConfig personMapConfig = new MapConfig();
        personMapConfig.setName(PERSON_MAP_NAME);

        MapIndexConfig ageIndexConfig = new MapIndexConfig("age", true);
        personMapConfig.addMapIndexConfig(ageIndexConfig);
        config.addMapConfig(personMapConfig);


        MultiMapConfig friendMultiMapConfig = new MultiMapConfig(FRIENDS_MULTI_MAP_NAME);
        friendMultiMapConfig.setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        config.addMultiMapConfig(friendMultiMapConfig);

//        QueueConfig emailConfig = new QueueConfig(MESSAGE_QUEUE);
//        QueueStoreConfig queueStoreConfig = new QueueStoreConfig();
//        queueStoreConfig.setStoreImplementation(queueStore);
//        emailConfig.setQueueStoreConfig(queueStoreConfig);
//        config.addQueueConfig(emailConfig);


        return config;
    }
}

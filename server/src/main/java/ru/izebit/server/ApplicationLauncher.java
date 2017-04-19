package ru.izebit.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.izebit.common.PersonDao;

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
    public HazelcastCluster hazelcastCluster(@Autowired Config config) {
        int nodeCount = Integer.getInteger("hazelcast.node.count", 4);

        return new HazelcastCluster(nodeCount, config);
    }

    @Bean
    public Config config() {
        Config config = new Config();

        MapConfig personMapConfig = new MapConfig();
        personMapConfig.setName(PersonDao.MAP_NAME);

        MapIndexConfig ageIndexConfig = new MapIndexConfig("age", true);
        personMapConfig.addMapIndexConfig(ageIndexConfig);


        return config;
    }
}

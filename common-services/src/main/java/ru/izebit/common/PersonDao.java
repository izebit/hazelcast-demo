package ru.izebit.common;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@Component
public class PersonDao {
    private static final String MAP_NAME = "personal";

    private final HazelcastInstance instance;
    private final IMap<String, Person> personalMap;

    @Autowired
    public PersonDao(HazelcastInstance instance) {
        this.instance = instance;
        this.personalMap = instance.getMap(MAP_NAME);
    }


    @PreDestroy
    public void destroy() {
        this.instance.shutdown();
    }

    public Person getByName(String name) {
        return personalMap.get(name);
    }

    public void put(Person person) {
        this.personalMap.put(person.getName(), person);
    }

    public void removeAll() {
        this.personalMap.clear();
    }
}

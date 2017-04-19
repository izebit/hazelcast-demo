package ru.izebit.common;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collection;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@Component
public class PersonDao {
    public static final String MAP_NAME = "personal-map";

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

    @SuppressWarnings("unchecked")
    public <T> T processWithName(String name, EntryProcessor<String, Person> processor) {
        return (T) personalMap.executeOnKey(name, processor);
    }

    public Collection<Person> findByName(String name) {
        SqlPredicate predicate = new SqlPredicate(String.format("name = %s", name));
        return personalMap.values(predicate);
    }


    public Collection<Person> findBySurnameAndAgeLessThen(String surname, int age) {
        Predicate predicate = Predicates.equal("surname", surname);
        predicate = Predicates.and(predicate, Predicates.lessThan("age", age));

        return personalMap.values(predicate);
    }
}

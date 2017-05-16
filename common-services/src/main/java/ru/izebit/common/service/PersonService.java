package ru.izebit.common.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.izebit.common.model.Person;

import java.util.Collection;
import java.util.List;

import static ru.izebit.common.other.HazelcastEntityNames.FRIENDS_MULTI_MAP_NAME;
import static ru.izebit.common.other.HazelcastEntityNames.PERSON_MAP_NAME;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@Component
public class PersonService {

    private final IMap<String, Person> personalMap;
    private final MultiMap<String, String> friends;

    public PersonService(@Autowired HazelcastInstance instance) {
        this.personalMap = instance.getMap(PERSON_MAP_NAME);
        this.friends = instance.getMultiMap(FRIENDS_MULTI_MAP_NAME);
    }

    public Person getByName(String name) {
        return personalMap.get(name);
    }

    public void put(Person person) {
        this.personalMap.put(person.getName(), person);
    }

    public void removeAll() {
        personalMap.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T processWithName(String name, EntryProcessor<String, Person> processor) {
        return (T) personalMap.executeOnKey(name, processor);
    }

    @SuppressWarnings("unchecked")
    public <T> T processing(String name, EntryProcessor<String, Person> processor) {
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

    public void changeAge(String name, int delta) {
        try {
            personalMap.lock(name);
            Person person = personalMap.get(name);
            person.setAge(person.getAge() + delta);
            personalMap.put(name, person);
        } finally {
            personalMap.unlock(name);
        }
    }

    public void addFriends(String name, List<String> friendList) {
        friendList.forEach(friend -> friends.put(name, friend));
    }

    public Collection<String> getFriendsFor(String name) {
        return this.friends.get(name);
    }
}

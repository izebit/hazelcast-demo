package ru.izebit.common.processors;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import ru.izebit.common.model.Address;
import ru.izebit.common.model.AddressKey;
import ru.izebit.common.model.Overview;
import ru.izebit.common.model.Person;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ru.izebit.common.other.TableNames.ADDRESS_MAP_NAME;

/**
 * @author Artem Konovalov
 *         creation date  4/19/17.
 * @since 1.0
 */
public class OverviewEntryProcessor implements
        Serializable,
        EntryProcessor<String, Person>,
        EntryBackupProcessor<String, Person>, HazelcastInstanceAware {

    private transient HazelcastInstance hazelcastInstance;

    @Override
    public Object process(Map.Entry<String, Person> entry) {

        Person person = entry.getValue();

        IMap<AddressKey, Address> addressMap = hazelcastInstance.getMap(ADDRESS_MAP_NAME);

        Predicate predicate = new PredicateBuilder()
                .getEntryObject()
                .key()
                .get("personName").equal(person.getName());


        Set<AddressKey> addressKeySet = addressMap.localKeySet(predicate);
        Set<Address> addresses = new HashSet<>(addressKeySet.size());
        for (AddressKey key : addressKeySet)
            addresses.add(addressMap.get(key));

        return new Overview(person, addresses);
    }

    @Override
    public EntryBackupProcessor<String, Person> getBackupProcessor() {
        return this;
    }

    @Override
    public void processBackup(Map.Entry<String, Person> entry) {
        process(entry);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}

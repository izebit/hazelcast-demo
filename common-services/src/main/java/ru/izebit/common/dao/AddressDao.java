package ru.izebit.common.dao;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.izebit.common.model.Address;
import ru.izebit.common.model.AddressKey;
import ru.izebit.common.other.TableNames;

import java.util.Collection;

/**
 * @author Artem Konovalov
 *         creation date  4/19/17.
 * @since 1.0
 */
@Component
public class AddressDao {
    private final IMap<AddressKey, Address> addressMap;

    public AddressDao(@Autowired HazelcastInstance hazelcastInstance) {
        this.addressMap = hazelcastInstance.getMap(TableNames.ADDRESS_MAP_NAME);
    }

    public Collection<AddressKey> findForPersonWith(String name) {
        Predicate predicate = Predicates.equal("personName", name);
        return addressMap.keySet(predicate);
    }

    public void put(String peopleName, Address address) {
        AddressKey addressKey = new AddressKey(peopleName, address.getId());
        addressMap.put(addressKey, address);
    }
}

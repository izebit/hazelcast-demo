package ru.izebit.common.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static ru.izebit.common.other.HazelcastEntityNames.FOO_MAP_NAME;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 6/13/17.
 * @since 1.0
 */
@Service
public class FooService {
    private final IMap<Long, Object> map;

    public FooService(@Autowired HazelcastInstance instance) {
        this.map = instance.getMap(FOO_MAP_NAME);
    }


    public void save(Long key, Object obj) {
        map.put(key, obj);
    }

    public Object get(long key) {
        return map.get(key);
    }

    public Collection<Object> getAll() {
        return map.values();
    }

    public void removeAll() {
        map.clear();
    }
}

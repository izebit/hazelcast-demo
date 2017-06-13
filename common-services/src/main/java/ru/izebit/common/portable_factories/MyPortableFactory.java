package ru.izebit.common.portable_factories;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import ru.izebit.common.model.Foo;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 6/13/17.
 * @since 1.0
 */
public class MyPortableFactory implements PortableFactory {

    @Override
    public Portable create(int classId) {
        if (Foo.ID == classId)
            return new Foo();
        else
            return null;
    }
}

package ru.izebit.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import org.junit.Test;
import ru.izebit.common.model.Foo;
import ru.izebit.common.portable_factories.MyPortableFactory;
import ru.izebit.common.service.FooService;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 7/12/17.
 * @since 1.0
 */
public class SerializationTest {


    @Test
    public void test() throws Exception {
        HazelcastInstance server = Hazelcast.newHazelcastInstance();


        FooService firstService = new FooService(getHazelcastClient(0));
        firstService.removeAll();

        Foo obj1 = new Foo();
        obj1.setId(System.currentTimeMillis());
        obj1.setBar("first value");
        firstService.save(obj1);

        Foo obj2 = getInstanceWithObject(System.currentTimeMillis(), "second value", "value");
        FooService secondService = new FooService(getHazelcastClient(1));
        secondService.save(obj2);


        Foo obj3 = getInstanceWithObject(System.currentTimeMillis(), "third value", null);
        secondService.save(obj3);

        Foo obj4 = getInstanceWithPrimitive(System.currentTimeMillis(), "fourth value", 0);
        FooService thirdService = new FooService(getHazelcastClient(3));
        secondService.save(obj4);

        assertEquals(obj4, firstService.get(obj4.getId()));
        assertEquals(obj4, secondService.get(obj4.getId()));
        assertEquals(obj4, thirdService.get(obj4.getId()));

        assertEquals(obj3, firstService.get(obj3.getId()));
        assertEquals(obj3, secondService.get(obj3.getId()));
        assertEquals(obj3, thirdService.get(obj3.getId()));

        assertEquals(obj2, firstService.get(obj2.getId()));
        assertEquals(obj2, secondService.get(obj2.getId()));
        assertEquals(obj2, thirdService.get(obj2.getId()));

        assertEquals(obj1, firstService.get(obj1.getId()));
        assertEquals(obj1, secondService.get(obj1.getId()));
        assertEquals(obj1, thirdService.get(obj1.getId()));
    }

    private static Foo getInstanceWithObject(long id, String bar, String troloParam) {
        return new Foo() {
            private String trolo;

            {
                this.setBar(bar);
                this.setId(id);
                this.trolo = troloParam;
            }

            @Override
            public void writePortable(PortableWriter writer) throws IOException {
                super.writePortable(writer);
                writer.writeUTF("value", trolo);
            }

            @Override
            public void readPortable(PortableReader reader) throws IOException {
                super.readPortable(reader);
                trolo = reader.readUTF("value");
            }
        };
    }

    private static Foo getInstanceWithPrimitive(long id, String bar, long value) {
        return new Foo() {
            private long value;

            {
                this.setBar(bar);
                this.setId(id);
                this.value = value;
            }

            @Override
            public void writePortable(PortableWriter writer) throws IOException {
                super.writePortable(writer);
                writer.writeLong("value", value);
            }

            @Override
            public void readPortable(PortableReader reader) throws IOException {
                super.readPortable(reader);
                value = reader.readLong("value");
            }
        };
    }

    private static HazelcastInstance getHazelcastClient(int portableVersion) {
        ClientConfig config = new ClientConfig();
        config
                .getSerializationConfig()
                .setPortableVersion(portableVersion)
                .addPortableFactory(1, new MyPortableFactory());

        return HazelcastClient.newHazelcastClient(config);
    }
}

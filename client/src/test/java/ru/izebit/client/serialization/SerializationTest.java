package ru.izebit.client.serialization;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import ru.izebit.common.service.FooService;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.junit.Assert.assertTrue;
import static ru.izebit.client.serialization.DynamicUtils.getStringFrom;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 7/12/17.
 * @since 1.0
 */
public class SerializationTest {


    @Test
    public void test() throws Exception {
        HazelcastInstance server = Hazelcast.newHazelcastInstance();

        ClassLoader firstClassLoader = DynamicUtils.getClassLoader(SourceCode.CLASS_NAME, SourceCode.FIRST_CLASS);
        Class<?> firstClass = firstClassLoader.loadClass(SourceCode.CLASS_NAME);

        FooService firstService = new FooService(getHazelcastClient(0, getPortableFactory(firstClassLoader, SourceCode.CLASS_NAME)));
        firstService.removeAll();

        Object obj1 = getRootObject(System.currentTimeMillis(), "first value", firstClass);
        firstService.save(getIdFrom(obj1), obj1);


        ClassLoader secondClassLoader = DynamicUtils.getClassLoader(SourceCode.CLASS_NAME, SourceCode.SECOND_CLASS);
        Class<?> secondClass = secondClassLoader.loadClass(SourceCode.CLASS_NAME);
        Object obj2 = getInstanceWithObject(System.currentTimeMillis(), "second value", "value", secondClass);
        FooService secondService = new FooService(getHazelcastClient(1, getPortableFactory(secondClassLoader, SourceCode.CLASS_NAME)));
        secondService.save(getIdFrom(obj2), obj2);


        Object obj3 = getInstanceWithObject(System.currentTimeMillis(), "third value", null, secondClass);
        secondService.save(getIdFrom(obj3), obj3);


        ClassLoader thirdClassLoader = DynamicUtils.getClassLoader(SourceCode.CLASS_NAME, SourceCode.THIRD_CLASS);
        Class<?> thirdClass = thirdClassLoader.loadClass(SourceCode.CLASS_NAME);
        Object obj4 = getInstanceWithPrimitive(System.currentTimeMillis(), "fourth value", 42, thirdClass);
        FooService thirdService = new FooService(getHazelcastClient(2, getPortableFactory(thirdClassLoader, SourceCode.CLASS_NAME)));
        thirdService.save(getIdFrom(obj4), obj4);

        assertObjectEquals(obj4, firstService.get(getIdFrom(obj4)));
        assertObjectEquals(obj4, secondService.get(getIdFrom(obj4)));
        assertObjectEquals(obj4, thirdService.get(getIdFrom(obj4)));

        assertObjectEquals(obj3, firstService.get(getIdFrom(obj3)));
        assertObjectEquals(obj3, secondService.get(getIdFrom(obj3)));
        assertObjectEquals(obj3, thirdService.get(getIdFrom(obj3)));

        assertObjectEquals(obj2, firstService.get(getIdFrom(obj2)));
        assertObjectEquals(obj2, secondService.get(getIdFrom(obj2)));
        assertObjectEquals(obj2, thirdService.get(getIdFrom(obj2)));

        assertObjectEquals(obj1, firstService.get(getIdFrom(obj1)));
        assertObjectEquals(obj1, secondService.get(getIdFrom(obj1)));
        assertObjectEquals(obj1, thirdService.get(getIdFrom(obj1)));


        for (Object obj : Arrays.asList(obj1, obj2, obj3, obj4)) {
            System.out.println("--------------");
            for (FooService service : Arrays.asList(firstService, secondService, thirdService))
                System.out.println(getStringFrom(service.get(getIdFrom(obj))));
        }
    }

    private static Long getIdFrom(Object obj) throws Exception {
        return (Long) readDeclaredField(obj, "id", true);
    }

    private static Object getRootObject(long id, String bar, Class<?> type) throws Exception {
        Object instance = type.newInstance();
        FieldUtils.writeDeclaredField(instance, "bar", bar, true);
        FieldUtils.writeDeclaredField(instance, "id", id, true);
        return instance;
    }

    private static Object getInstanceWithObject(long id, String bar, String trolo, Class<?> type) throws Exception {
        Object instance = getRootObject(id, bar, type);
        FieldUtils.writeDeclaredField(instance, "trolo", trolo, true);
        return instance;
    }

    private static Object getInstanceWithPrimitive(long id, String bar, long value, Class<?> type) throws Exception {
        Object instance = getRootObject(id, bar, type);
        FieldUtils.writeDeclaredField(instance, "value", value, true);
        return instance;
    }

    private static PortableFactory getPortableFactory(ClassLoader classLoader, String className) throws Exception {
        Class<?> type = classLoader.loadClass(className);
        int versionId = (Integer) FieldUtils.readStaticField(type, "ID", true);

        return new PortableFactory() {
            @Override
            @SneakyThrows
            public Portable create(int classId) {
                if (versionId == classId)
                    return (Portable) type.newInstance();
                else
                    return null;
            }
        };
    }

    private static HazelcastInstance getHazelcastClient(int portableVersion, PortableFactory portableFactory) {
        ClientConfig config = new ClientConfig();
        config
                .getSerializationConfig()
                .setPortableVersion(portableVersion)
                .addPortableFactory(1, portableFactory);

        return HazelcastClient.newHazelcastClient(config);
    }

    private static void assertObjectEquals(Object o1, Object o2) throws Exception {
        boolean result = Objects.equals(readDeclaredField(o1, "bar", true), readDeclaredField(o2, "bar", true)) &&
                Objects.equals(readDeclaredField(o1, "id", true), readDeclaredField(o2, "id", true));

        assertTrue(result);
    }


    private static class SourceCode {
        private static final String CLASS_NAME = "ru.izebit.Foo";

        private static final String IMPORTS = "" +
                "package ru.izebit; " +
                "import com.hazelcast.nio.serialization.Portable; " +
                "import com.hazelcast.nio.serialization.PortableReader; " +
                "import com.hazelcast.nio.serialization.PortableWriter; " +
                "import lombok.Data; " +
                "import java.io.IOException; " +
                "import java.util.Objects; ";

        private static final String FIRST_CLASS = "" +
                IMPORTS +
                "@Data  " +
                "public class Foo implements Portable { " +
                "    public static final int ID = 1;  " +
                "    private long id; " +
                "    private String bar; " +

                "    @Override " +
                "    public int getFactoryId() { " +
                "        return 1; " +
                "    } " +
                "    @Override " +
                "    public int getClassId() { " +
                "        return ID; " +
                "    } " +
                "    @Override " +
                "    public void writePortable(PortableWriter writer) throws IOException { " +
                "        writer.writeLong(\"id\", id); " +
                "        writer.writeUTF(\"bar\", bar); " +
                "    } " +
                "    @Override " +
                "    public void readPortable(PortableReader reader) throws IOException { " +
                "        id = reader.readLong(\"id\"); " +
                "        bar = reader.readUTF(\"bar\"); " +
                "    } " +
                "}";
        private static final String SECOND_CLASS = "" +
                IMPORTS +
                "@Data  " +
                "public class Foo implements Portable { " +
                "    public static final int ID = 1;  " +
                "    private long id; " +
                "    private String bar; " +
                "    private String trolo; " +

                "    @Override " +
                "    public int getFactoryId() { " +
                "        return 1; " +
                "    } " +
                "    @Override " +
                "    public int getClassId() { " +
                "        return ID; " +
                "    } " +
                "    @Override " +
                "    public void writePortable(PortableWriter writer) throws IOException { " +
                "        writer.writeLong(\"id\", id); " +
                "        writer.writeUTF(\"bar\", bar); " +
                "        writer.writeUTF(\"trolo\", trolo); " +
                "    } " +
                "    @Override " +
                "    public void readPortable(PortableReader reader) throws IOException { " +
                "        id = reader.readLong(\"id\"); " +
                "        bar = reader.readUTF(\"bar\"); " +
                "        trolo = reader.readUTF(\"trolo\"); " +
                "    } " +
                "}";

        private static final String THIRD_CLASS = "" +
                IMPORTS +
                "@Data  " +
                "public class Foo implements Portable { " +
                "    public static final int ID = 1;  " +
                "    private long id; " +
                "    private String bar; " +
                "    private long value; " +

                "    @Override " +
                "    public int getFactoryId() { " +
                "        return 1; " +
                "    } " +
                "    @Override " +
                "    public int getClassId() { " +
                "        return ID; " +
                "    } " +
                "    @Override " +
                "    public void writePortable(PortableWriter writer) throws IOException { " +
                "        writer.writeLong(\"id\", id); " +
                "        writer.writeUTF(\"bar\", bar); " +
                "        writer.writeLong(\"value\", value); " +
                "    } " +
                "    @Override " +
                "    public void readPortable(PortableReader reader) throws IOException { " +
                "        id = reader.readLong(\"id\"); " +
                "        bar = reader.readUTF(\"bar\"); " +
                "        value = reader.readLong(\"value\"); " +
                "    } " +
                "}";
    }
}

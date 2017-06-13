package ru.izebit.common.model;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import lombok.Data;

import java.io.IOException;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 6/13/17.
 * @since 1.0
 */
@Data
public class Foo implements Portable {
    public static final int ID = 1;

    private String foo;
    private String bar;


    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("foo", foo);
        writer.writeUTF("bar", bar);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        foo = reader.readUTF("foo");
        bar = reader.readUTF("bar");
    }
}

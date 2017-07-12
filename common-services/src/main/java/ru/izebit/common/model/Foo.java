package ru.izebit.common.model;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import lombok.Data;

import java.io.IOException;
import java.util.Objects;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 6/13/17.
 * @since 1.0
 */
@Data
public class Foo implements Portable, Comparable<Foo> {
    public static final int ID = 1;

    private long id;
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
        writer.writeLong("id", id);
        writer.writeUTF("bar", bar);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        id = reader.readLong("id");
        bar = reader.readUTF("bar");
    }


    @Override
    public int compareTo(Foo o) {
        return Long.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Foo)) return false;

        Foo foo = (Foo) o;
        return Objects.equals(foo.bar, this.bar) &&
                Objects.equals(foo.id, this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bar);
    }
}

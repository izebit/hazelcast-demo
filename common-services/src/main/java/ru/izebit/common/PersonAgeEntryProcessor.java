package ru.izebit.common;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Artem Konovalov
 *         creation date  4/16/17.
 * @since 1.0
 */
public class PersonAgeEntryProcessor implements Serializable,
        EntryProcessor<String, Person>,
        EntryBackupProcessor<String, Person> {

    private final int age;

    public PersonAgeEntryProcessor(int age) {
        this.age = age;
    }


    @Override
    public void processBackup(Map.Entry<String, Person> entry) {
        process(entry);
    }

    @Override
    public Object process(Map.Entry<String, Person> entry) {
        Person person = entry.getValue();
        person.setAge(age);
        entry.setValue(person);

        return Boolean.TRUE;
    }

    @Override
    public EntryBackupProcessor<String, Person> getBackupProcessor() {
        return this;
    }

}

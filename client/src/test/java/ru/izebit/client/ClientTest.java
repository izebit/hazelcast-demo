package ru.izebit.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.izebit.common.Person;
import ru.izebit.common.PersonAgeEntryProcessor;
import ru.izebit.common.PersonDao;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationLauncher.class)
public class ClientTest {
    @Autowired
    private PersonDao personDao;

    @Before
    public void tearDown() {
        personDao.removeAll();
    }

    @Test
    public void successful_get() {
        Person originalPerson = new Person("artem", "konovalov", 26);
        personDao.put(originalPerson);

        Person storedPerson = personDao.getByName(originalPerson.getName());

        assertEquals(storedPerson, originalPerson);
    }

    @Test
    public void successful_entry_processing() throws InterruptedException {
        Person person = new Person("artem", "konovalov", 26);
        personDao.put(person);

        int age = 30;

        PersonAgeEntryProcessor processor = new PersonAgeEntryProcessor(age);

        boolean result = personDao.processWithName(person.getName(), processor);
        assertTrue(result);
        TimeUnit.SECONDS.sleep(1);

        Person storedPerson = personDao.getByName(person.getName());
        assertEquals(age, storedPerson.getAge());
    }

    @Test
    public void successful_sql_query() {
        Person person = new Person("artem", "konovalov", 26);
        personDao.put(person);

        Collection<Person> result = personDao.findByName(person.getName());
        assertThat(result, hasSize(1));
        assertThat(result, contains(person));
    }

    @Test
    public void successful_predicate_query() {
        Person person = new Person("artem", "konovalov", 26);
        personDao.put(person);

        Collection<Person> result = personDao.findBySurnameAndAgeLessThen(person.getSurname(), 30);
        assertThat(result, hasSize(1));
        assertThat(result, contains(person));


        result = personDao.findBySurnameAndAgeLessThen(person.getSurname(), person.getAge());
        assertThat(result, hasSize(0));
    }
}

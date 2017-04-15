package ru.izebit.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.izebit.common.Person;
import ru.izebit.common.PersonDao;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void successfull_get() {
        Person originalPerson = new Person("artem", "konovalov", 26);
        personDao.put(originalPerson);

        Person storedPerson = personDao.getByName(originalPerson.getName());

        assertEquals(storedPerson, originalPerson);
    }
}

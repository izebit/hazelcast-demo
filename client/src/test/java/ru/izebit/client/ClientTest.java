package ru.izebit.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.izebit.common.model.Address;
import ru.izebit.common.model.Message;
import ru.izebit.common.model.Overview;
import ru.izebit.common.model.Person;
import ru.izebit.common.processors.OverviewEntryProcessor;
import ru.izebit.common.processors.PersonAgeEntryProcessor;
import ru.izebit.common.service.AddressService;
import ru.izebit.common.service.MessageService;
import ru.izebit.common.service.PersonService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
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
    private PersonService personService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private MessageService messageService;

    @Before
    public void tearDown() {
        personService.removeAll();
        addressService.removeAll();
        messageService.removeAll();
    }

    @Test
    public void successful_get() {
        Person originalPerson = new Person("artem", "konovalov", 26);
        personService.put(originalPerson);

        Person storedPerson = personService.getByName(originalPerson.getName());

        assertEquals(storedPerson, originalPerson);
    }

    @Test
    public void successful_change_age_entry_processing() throws InterruptedException {
        Person person = new Person("artem", "konovalov", 26);
        personService.put(person);

        int age = 30;

        PersonAgeEntryProcessor processor = new PersonAgeEntryProcessor(age);

        boolean result = personService.processWithName(person.getName(), processor);
        assertTrue(result);
        TimeUnit.SECONDS.sleep(1);

        Person storedPerson = personService.getByName(person.getName());
        assertEquals(age, storedPerson.getAge());
    }


    @Test
    public void successful_overview_entry_processing() throws InterruptedException {
        Person person = new Person("artem", "konovalov", 26);
        personService.put(person);


        Address firstAddress = new Address("saratov", "fedorovskay");
        addressService.put(person.getName(), firstAddress);

        Address secondAddress = new Address("saratov", "shelkovichnay");
        addressService.put(person.getName(), secondAddress);

        OverviewEntryProcessor processor = new OverviewEntryProcessor();
        Overview overview = personService.processing(person.getName(), processor);

        assertNotNull(overview);
        assertEquals(person.getName(), overview.getPerson().getName());
        assertThat(overview.getAddresses(), hasSize(2));
        assertTrue(overview.getAddresses().contains(firstAddress));
        assertTrue(overview.getAddresses().contains(secondAddress));
    }

    @Test
    public void successful_sql_query() {
        Person person = new Person("artem", "konovalov", 26);
        personService.put(person);

        Collection<Person> result = personService.findByName(person.getName());
        assertThat(result, hasSize(1));
        assertThat(result, contains(person));
    }

    @Test
    public void successful_predicate_query() {
        Person person = new Person("artem", "konovalov", 26);
        personService.put(person);

        Collection<Person> result = personService.findBySurnameAndAgeLessThen(person.getSurname(), 30);
        assertThat(result, hasSize(1));
        assertThat(result, contains(person));


        result = personService.findBySurnameAndAgeLessThen(person.getSurname(), person.getAge());
        assertThat(result, hasSize(0));
    }

    @Test
    public void successful_messaging_by_queue() {
        int count = 10;
        List<Message> messages = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            messages.add(
                    Message
                            .builder()
                            .to("to" + i)
                            .from("from" + i)
                            .msg("message:" + i)
                            .build());

        messages.forEach(messageService::send);


        for (int i = 0; i < count; i++) {
            Message message = messageService.receive();
            assertEquals(messages.get(i).getFrom(), message.getFrom());
            assertEquals(messages.get(i).getTo(), message.getTo());
            assertEquals(messages.get(i).getMsg(), message.getMsg());
        }

        assertNull(messageService.receive());
    }

    @Test
    public void successful_lock_for_key() {
        Person person = new Person("artem", "konovalov", 10);
        personService.put(person);

        int result = person.getAge();
        result += IntStream
                .range(0, 1_000)
                .parallel()
                .peek(delta -> personService.changeAge(person.getName(), delta))
                .sum();

        Person updatedPerson = personService.getByName(person.getName());
        assertEquals(result, updatedPerson.getAge());
    }
}

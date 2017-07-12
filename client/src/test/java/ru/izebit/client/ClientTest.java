package ru.izebit.client;

import com.hazelcast.query.Predicates;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Timed;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
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

    @Test
    @Timed(millis = 10_000L)
    public void successful_topic() throws Exception {
        int count = 1_000;
        final AtomicInteger summaryCount = new AtomicInteger();
        List<String> news = new ArrayList<>();
        Set<String> messages = Collections.newSetFromMap(new ConcurrentHashMap<>());

        messageService.addCallBackForNews(msg -> {
            summaryCount.incrementAndGet();
            messages.remove(msg);
        });

        IntStream
                .range(0, count)
                .mapToObj(number -> "msg:" + number)
                .forEach(msg -> {
                    messages.add(msg);
                    news.add(msg);
                });

        for (int i = 0; i < count / 2; i++)
            messageService.publish(news.get(i));

        TimeUnit.SECONDS.sleep(1);

        assertEquals(count / 2, messages.size());
        assertEquals(count / 2, summaryCount.get());
        messageService.addCallBackForNews(msg -> summaryCount.incrementAndGet());


        for (int i = count / 2; i < news.size(); i++)
            messageService.publish(news.get(i));

        TimeUnit.SECONDS.sleep(1);

        assertEquals(summaryCount.get(), count + count / 2);
        assertTrue(messages.isEmpty());
    }

    @Test
    public void successful_multimap() throws Exception {
        String firstName = "ivan";
        String secondName = "petr";
        String thirdName = "artem";

        personService.addFriends(thirdName, emptyList());
        personService.addFriends(firstName, asList(secondName, thirdName));


        assertThat(personService.getFriendsFor(thirdName), empty());
        assertThat(personService.getFriendsFor(firstName), hasSize(2));
        assertThat(personService.getFriendsFor(firstName), contains(secondName, thirdName));
    }

    @Test
    public void successful_add_listener_with_predicate() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        personService.addCallBackForAdd(
                (key, person) -> counter.incrementAndGet(),
                Predicates.equal("name", "artem"));


        personService.put(new Person("artem", "izebit", 26));
        personService.put(new Person("john", "noname", 35));

        TimeUnit.SECONDS.sleep(1);
        assertThat(counter.get(), is(1));

        counter.set(0);
        personService.addCallBackForRemove(
                (key, person) -> counter.incrementAndGet(),
                Predicates.equal("name", "artem"));
        personService.removeAll();

        TimeUnit.SECONDS.sleep(1);
        assertThat(counter.get(), is(0));
    }
}

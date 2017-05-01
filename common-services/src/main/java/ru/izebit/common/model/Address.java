package ru.izebit.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Artem Konovalov
 *         creation date  4/19/17.
 * @since 1.0
 */
@Data
public class Address implements Serializable {
    private final Integer id;
    private final String city;
    private final String street;

    public Address(String city, String street) {
        this.id = Objects.hash(city, street);
        this.city = city;
        this.street = street;
    }
}

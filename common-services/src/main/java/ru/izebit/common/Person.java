package ru.izebit.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 232898298392839881L;


    private final String name;
    private String surname;
    private int age;
}

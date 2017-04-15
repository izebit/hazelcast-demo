package ru.izebit.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Artem Konovalov
 *         creation date  4/15/17.
 * @since 1.0
 */
@Data
public class Person implements Serializable {
    private static final long serialVersionUID = 2328938298392839882L;


    private final String name;
    private final String surname;
    private final int age;
}

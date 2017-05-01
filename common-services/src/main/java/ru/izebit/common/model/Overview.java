package ru.izebit.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Artem Konovalov
 *         creation date  4/19/17.
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class Overview implements Serializable {
    private final Person person;
    private final Collection<Address> addresses;
}

package ru.izebit.common.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Artem Konovalov
 *         creation date  5/1/17.
 * @since 1.0
 */
@Data
@Builder
public class Message implements Serializable {
    private final String to;
    private final String from;
    private final String msg;
}

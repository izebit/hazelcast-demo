package ru.izebit.common.model;

import com.hazelcast.core.PartitionAware;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Artem Konovalov
 *         creation date  4/19/17.
 * @since 1.0
 */
@Data
public class AddressKey implements Serializable, PartitionAware<String> {
    private final String personName;
    private final Integer addressId;

    @Override
    public String getPartitionKey() {
        return personName;
    }
}

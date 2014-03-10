package org.springframework.integration.dsl.support;

import java.util.Collection;

/**
 * @author Artem Bilan
 */
public interface GenericSplitter<T> {

	Collection<?> split(T target);

}

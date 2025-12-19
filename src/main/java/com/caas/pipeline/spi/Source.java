package com.caas.pipeline.spi;

/**
 * Produces items for a pipeline in a pull-based manner.
 * <p>
 * Returning {@code null} signals that no more items are available.
 * Implementations should be lightweight and stateless where possible, or
 * encapsulate their own lifecycle externally.
 *
 * @param <T> item type produced by the source
 */
@FunctionalInterface
public interface Source<T> {

    /**
     * Retrieves the next item from the source.
     *
     * @return next item, or {@code null} when the source is exhausted
     * @throws Exception if the source cannot provide the next item
     */
    T next() throws Exception;
}

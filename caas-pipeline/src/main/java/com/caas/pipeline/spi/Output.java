package com.caas.pipeline.spi;

/**
 * Consumes items emitted by a pipeline.
 * <p>
 * Implementations are responsible for handling persistence, delivery, or any
 * terminal side effects. Thread-safety requirements should be documented by
 * each implementation.
 *
 * @param <T> item type consumed by the output
 */
@FunctionalInterface
public interface Output<T> {

    /**
     * Accepts an item produced by the pipeline.
     *
     * @param item item to consume
     * @throws Exception if the item cannot be accepted
     */
    void emit(T item) throws Exception;
}

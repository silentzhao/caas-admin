package com.caas.pipeline.spi;

/**
 * Transforms one item into another within a pipeline stage.
 * <p>
 * Implementations should avoid side effects beyond transformation logic;
 * stateful processors should encapsulate their state explicitly.
 *
 * @param <I> input type
 * @param <O> output type
 */
@FunctionalInterface
public interface Processor<I, O> {

    /**
     * Processes a single input item and returns the output item.
     *
     * @param input input item
     * @return transformed output item
     * @throws Exception if processing fails
     */
    O process(I input) throws Exception;
}

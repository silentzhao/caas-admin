package com.caas.pipeline.spi;

/**
 * 消费流水线输出的数据。
 * <p>
 * 实现负责持久化、投递或其他终端副作用；线程安全要求由实现方说明。
 *
 * @param <T> Output 消费的数据类型
 */
@FunctionalInterface
public interface Output<T> {

    /**
     * 接收流水线产生的数据项。
     *
     * @param item 要消费的数据项
     * @throws Exception 无法接收时抛出
     */
    void emit(T item) throws Exception;
}

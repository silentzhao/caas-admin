package com.caas.pipeline.spi;

/**
 * 以拉取模式为流水线提供数据。
 * <p>
 * 返回 {@code null} 表示数据已耗尽。
 * 实现应尽量轻量、无状态，或将生命周期管理封装在外部。
 *
 * @param <T> Source 产生的数据类型
 */
@FunctionalInterface
public interface Source<T> {

    /**
     * 获取下一个数据项。
     *
     * @return 下一个数据项；若已耗尽则返回 {@code null}
     * @throws Exception 无法提供下一项时抛出
     */
    T next() throws Exception;
}

package com.caas.pipeline.spi;

/**
 * 在流水线阶段中将输入项转换为输出项。
 * <p>
 * 实现应避免转换逻辑之外的副作用；如需状态，请显式封装。
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 */
@FunctionalInterface
public interface Processor<I, O> {

    /**
     * 处理单个输入并返回输出。
     *
     * @param input 输入项
     * @return 转换后的输出项
     * @throws Exception 处理失败时抛出
     */
    O process(I input) throws Exception;
}

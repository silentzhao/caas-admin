package com.caas.pipeline;

import com.caas.pipeline.spi.Output;
import com.caas.pipeline.spi.Processor;
import com.caas.pipeline.spi.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 简单的同步流水线引擎：从 Source 拉取数据，经由 Processor 串联处理后输出到 Output。
 */
public final class Pipeline<I, O> {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final Source<I> source;
    private final List<Processor<?, ?>> processors;
    private final Output<O> output;
    private final int batchSize;

    public Pipeline(Source<I> source, Output<O> output, List<Processor<?, ?>> processors) {
        this(source, output, processors, DEFAULT_BATCH_SIZE);
    }

    public Pipeline(Source<I> source, Output<O> output, List<Processor<?, ?>> processors, int batchSize) {
        this.source = Objects.requireNonNull(source, "source");
        this.output = Objects.requireNonNull(output, "output");
        this.processors = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(processors, "processors")));
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        this.batchSize = batchSize;
    }

    /**
     * 执行流水线，直到 Source 耗尽为止。
     */
    public void run() throws Exception {
        while (true) {
            // 1) 从 Source 拉取一批数据。
            List<I> batch = fetchBatch(source, batchSize);
            if (batch.isEmpty()) {
                return;
            }

            // 2) 逐条经过 Processor 链处理。
            List<O> processed = processBatch(batch, processors);

            // 3) 输出处理后的结果。
            for (O item : processed) {
                output.emit(item);
            }
        }
    }

    /**
     * 从 Source 拉取最多 {@code batchSize} 条数据到列表中。
     */
    public static <I> List<I> fetchBatch(Source<I> source, int batchSize) throws Exception {
        List<I> items = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            I next = source.next();
            if (next == null) {
                break;
            }
            items.add(next);
        }
        return items;
    }

    /**
     * 通过 Processor 链处理一批数据。
     */
    public static <I, O> List<O> processBatch(List<I> items, List<Processor<?, ?>> processors) throws Exception {
        List<O> results = new ArrayList<>(items.size());
        for (I item : items) {
            Object current = item;
            for (Processor<?, ?> processor : processors) {
                current = applyProcessor(processor, current);
            }
            @SuppressWarnings("unchecked")
            O output = (O) current;
            results.add(output);
        }
        return results;
    }

    private static Object applyProcessor(Processor<?, ?> processor, Object input) throws Exception {
        @SuppressWarnings("unchecked")
        Processor<Object, Object> typed = (Processor<Object, Object>) processor;
        return typed.process(input);
    }
}

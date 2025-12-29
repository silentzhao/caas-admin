package com.caas.pipeline.example;

import com.caas.pipeline.Pipeline;
import com.caas.pipeline.spi.Output;
import com.caas.pipeline.spi.Processor;
import com.caas.pipeline.spi.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 最小示例：演示如何组合 Source、Processor 链与 Output。
 */
public final class PipelineExample {

    public static void main(String[] args) throws Exception {
        Source<String> source = new ListSource<>(Arrays.asList("alice", "bob", "carol"));

        List<Processor<?, ?>> processors = List.of(
                (Processor<String, String>) String::trim,
                (Processor<String, String>) String::toUpperCase,
                (Processor<String, Integer>) String::length
        );

        Output<Integer> output = item -> System.out.println("length=" + item);

        Pipeline<String, Integer> pipeline = new Pipeline<>(source, output, processors);
        pipeline.run();
    }

    private static final class ListSource<T> implements Source<T> {
        private final List<T> items;
        private int index = 0;

        private ListSource(List<T> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public T next() {
            if (index >= items.size()) {
                return null;
            }
            return items.get(index++);
        }
    }
}

package com.caas.pipeline;

import com.caas.pipeline.spi.Output;
import com.caas.pipeline.spi.Processor;
import com.caas.pipeline.spi.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 最小自测：可作为普通 Java 程序直接运行。
 */
public final class PipelineSelfTest {

    public static void main(String[] args) throws Exception {
        Source<Integer> source = new ListSource<>(Arrays.asList(1, 2, 3));

        List<Processor<?, ?>> processors = List.of(
                (Processor<Integer, Integer>) value -> value + 1,
                (Processor<Integer, String>) String::valueOf
        );

        List<String> outputItems = new ArrayList<>();
        Output<String> output = outputItems::add;

        Pipeline<Integer, String> pipeline = new Pipeline<>(source, output, processors);
        pipeline.run();

        List<String> expected = Arrays.asList("2", "3", "4");
        if (!expected.equals(outputItems)) {
            throw new IllegalStateException("Unexpected output: " + outputItems);
        }
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

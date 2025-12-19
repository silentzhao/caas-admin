package com.caas.llm;

import com.caas.pipeline.spi.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于模板方法模式的大模型处理器抽象类。
 * 子类只需负责构建 Prompt 与解析返回内容。
 */
public abstract class LlmProcessor<I, O> implements Processor<I, O> {

    private final LlmClient llmClient;
    private final Double temperature;
    private final Integer maxTokens;

    protected LlmProcessor(LlmClient llmClient, Double temperature, Integer maxTokens) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    protected LlmProcessor(LlmClient llmClient) {
        this(llmClient, null, null);
    }

    @Override
    public final O process(I input) throws Exception {
        // 1) 构建可选的 Prompt 方案（便于后续做 A/B 测试）。
        List<PromptVariant> variants = buildPromptVariants(input);
        PromptVariant selected = selectPromptVariant(variants, input);

        // 2) 基于选中的 Prompt 构建请求并调用 LLM。
        LlmRequest request = buildRequest(input, selected);
        LlmResponse response = llmClient.generate(request);

        // 3) 解析响应，返回业务结果。
        return parseResponse(response, input, selected);
    }

    /**
     * 构建 Prompt 方案集合，默认只返回单个方案。
     */
    protected List<PromptVariant> buildPromptVariants(I input) {
        return List.of(buildPromptVariant(input));
    }

    /**
     * 构建单个 Prompt 方案。
     */
    protected abstract PromptVariant buildPromptVariant(I input);

    /**
     * 选择一个 Prompt 方案，默认取第一个。
     */
    protected PromptVariant selectPromptVariant(List<PromptVariant> variants, I input) {
        Objects.requireNonNull(variants, "variants");
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("Prompt 方案列表不能为空");
        }
        return variants.get(0);
    }

    /**
     * 构建 LLM 请求对象，子类可通过重写进行扩展。
     */
    protected LlmRequest buildRequest(I input, PromptVariant variant) {
        LlmRequest request = new LlmRequest();
        request.setSystemPrompt(variant.getSystemPrompt());
        request.setUserPrompt(variant.getUserPrompt());
        request.setTemperature(temperature);
        request.setMaxTokens(maxTokens);
        request.setAttributes(buildAttributes(input, variant));
        return request;
    }

    /**
     * 组装扩展属性，默认写入 Prompt 方案标识。
     */
    protected Map<String, Object> buildAttributes(I input, PromptVariant variant) {
        if (variant.getId() == null || variant.getId().isEmpty()) {
            return null;
        }
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("promptVariantId", variant.getId());
        return attributes;
    }

    /**
     * 解析 LLM 响应并返回业务结果。
     */
    protected abstract O parseResponse(LlmResponse response, I input, PromptVariant variant) throws Exception;

    /**
     * Prompt 方案，支持 systemPrompt 与 userPrompt。
     */
    protected static class PromptVariant {

        private final String id;
        private final String systemPrompt;
        private final String userPrompt;

        public PromptVariant(String id, String systemPrompt, String userPrompt) {
            this.id = id;
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
        }

        public String getId() {
            return id;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public String getUserPrompt() {
            return userPrompt;
        }
    }
}

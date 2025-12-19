package com.caas.llm.processor;

import com.caas.domain.content.ArticleDraft;
import com.caas.domain.content.HotTopic;
import com.caas.llm.LlmClient;
import com.caas.llm.LlmProcessor;
import com.caas.llm.LlmResponse;

import java.time.LocalDateTime;

/**
 * 热点解读处理器：将 HotTopic 生成公众号/知乎风格的 Markdown 解读稿。
 */
public class TopicExplainProcessor extends LlmProcessor<HotTopic, ArticleDraft> {

    public TopicExplainProcessor(LlmClient llmClient, Double temperature, Integer maxTokens) {
        super(llmClient, temperature, maxTokens);
    }

    public TopicExplainProcessor(LlmClient llmClient) {
        super(llmClient);
    }

    @Override
    protected PromptVariant buildPromptVariant(HotTopic input) {
        String systemPrompt = """
                你是资深内容编辑，擅长公众号/知乎风格的热点解读。
                输出必须是 Markdown，结构清晰，语言克制、专业、可读。
                """;

        String userPrompt = """
                请基于以下热点信息撰写解读文章，输出 Markdown：
                - 热点标题：%s
                - 热点描述：%s
                - 分类：%s
                - 来源平台：%s
                - 来源链接：%s
                - 语言：%s
                - 地区：%s
                - 热度分数：%s
                - 讨论量：%s
                - 情感倾向：%s
                - 关键词：%s

                写作要求：
                1) 标题使用一级标题（#）。
                2) 正文分段清晰，包含背景、核心信息、影响分析、观点小结。
                3) 适当使用项目符号或小标题，但不要过度营销。
                4) 如信息不足，允许合理补充常识性背景，但需保持谨慎措辞。
                """.formatted(
                safe(input.getTitle()),
                safe(input.getDescription()),
                safe(input.getCategory()),
                safe(input.getSourcePlatform()),
                safe(input.getSourceUrl()),
                safe(input.getLanguage()),
                safe(input.getRegion()),
                safe(input.getPopularityScore()),
                safe(input.getMentionCount()),
                safe(input.getSentiment()),
                safe(input.getKeywords())
        );

        return new PromptVariant("topic-explain-v1", systemPrompt, userPrompt);
    }

    @Override
    protected ArticleDraft parseResponse(LlmResponse response, HotTopic input, PromptVariant variant) {
        ArticleDraft draft = new ArticleDraft();
        draft.setHotTopicId(input.getId());
        draft.setTitle(buildTitle(input));
        draft.setSummary(input.getDescription());
        draft.setBody(response.getContent());
        draft.setAuthorId("llm");
        draft.setEditorId("llm");
        draft.setStatus("draft");
        draft.setWordCount(countWords(response.getContent()));
        draft.setLanguage(input.getLanguage());
        draft.setTags(input.getKeywords());
        draft.setVersion(1);
        draft.setCreatedAt(LocalDateTime.now());
        draft.setUpdatedAt(LocalDateTime.now());
        return draft;
    }

    private String buildTitle(HotTopic input) {
        String title = input.getTitle();
        if (title == null || title.isBlank()) {
            return "热点解读";
        }
        return title + "解读";
    }

    private Integer countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String trimmed = content.trim();
        return trimmed.length();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

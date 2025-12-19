package com.caas.llm.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caas.domain.content.ArticleDraft;
import com.caas.domain.content.VideoScript;
import com.caas.llm.LlmClient;
import com.caas.llm.LlmProcessor;
import com.caas.llm.LlmResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章转视频脚本处理器：将 ArticleDraft 生成 JSON 结构脚本并解析为 VideoScript。
 */
public class ArticleToVideoScriptProcessor extends LlmProcessor<ArticleDraft, VideoScript> {

    public ArticleToVideoScriptProcessor(LlmClient llmClient, Double temperature, Integer maxTokens) {
        super(llmClient, temperature, maxTokens);
    }

    public ArticleToVideoScriptProcessor(LlmClient llmClient) {
        super(llmClient);
    }

    @Override
    protected PromptVariant buildPromptVariant(ArticleDraft input) {
        String systemPrompt = """
                你是视频脚本策划，擅长把文章改写成分段视频脚本。
                输出必须是 JSON，严格可解析，不要出现多余文本。
                """;

        String userPrompt = """
                请把以下文章改写为视频脚本，并输出 JSON：
                - 文章标题：%s
                - 文章摘要：%s
                - 文章正文（Markdown）：%s

                JSON 格式要求（字段名必须一致）：
                {
                  "title": "视频标题",
                  "style": "视频风格",
                  "target_duration_seconds": 120,
                  "language": "zh-CN",
                  "narration": "全片旁白文本",
                  "segments": [
                    {
                      "order_index": 1,
                      "type": "intro|main|outro",
                      "text": "本段口播文本",
                      "visual_notes": "画面/镜头提示",
                      "duration_seconds": 8,
                      "asset_urls": ["https://..."],
                      "start_offset_seconds": 0
                    }
                  ]
                }

                写作要求：
                1) 结构清晰，包含开场、主体、结尾。
                2) 文案简洁，适合口播。
                3) 时长分配合理，总时长接近目标时长。
                """.formatted(
                safe(input.getTitle()),
                safe(input.getSummary()),
                safe(input.getBody())
        );

        return new PromptVariant("article-to-video-v1", systemPrompt, userPrompt);
    }

    @Override
    protected VideoScript parseResponse(LlmResponse response, ArticleDraft input, PromptVariant variant) {
        JSONObject root = JSON.parseObject(response.getContent());

        VideoScript script = new VideoScript();
        script.setHotTopicId(input.getHotTopicId());
        script.setTitle(defaultIfBlank(root.getString("title"), input.getTitle()));
        script.setStyle(root.getString("style"));
        script.setTargetDurationSeconds(intValue(root.get("target_duration_seconds")));
        script.setLanguage(defaultIfBlank(root.getString("language"), input.getLanguage()));
        script.setNarration(root.getString("narration"));
        script.setSegments(parseSegments(root.getJSONArray("segments")));
        script.setStatus("draft");
        script.setCreatedAt(LocalDateTime.now());
        script.setUpdatedAt(LocalDateTime.now());
        return script;
    }

    private List<VideoScript.Segment> parseSegments(JSONArray segments) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }
        List<VideoScript.Segment> results = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            JSONObject item = segments.getJSONObject(i);
            if (item == null) {
                continue;
            }
            VideoScript.Segment segment = new VideoScript.Segment();
            segment.setOrderIndex(intValue(item.get("order_index")));
            segment.setType(item.getString("type"));
            segment.setText(item.getString("text"));
            segment.setVisualNotes(item.getString("visual_notes"));
            segment.setDurationSeconds(intValue(item.get("duration_seconds")));
            segment.setAssetUrls(stringListValue(item.get("asset_urls")));
            segment.setStartOffsetSeconds(intValue(item.get("start_offset_seconds")));
            results.add(segment);
        }
        return results;
    }

    private Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private List<String> stringListValue(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> result = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                result.add(String.valueOf(array.get(i)));
            }
            return result;
        }
        return List.of(String.valueOf(value));
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

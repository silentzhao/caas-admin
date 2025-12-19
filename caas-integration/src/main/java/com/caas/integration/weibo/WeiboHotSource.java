package com.caas.integration.weibo;

import com.caas.domain.content.HotTopic;
import com.caas.pipeline.spi.Source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * 微博热榜 Source 示例实现，通过 HTTP API 拉取热点并转为 HotTopic。
 */
public class WeiboHotSource implements Source<HotTopic> {

    private final HttpClient httpClient;
    private final URI apiEndpoint;
    private final String apiToken;
    private final Deque<HotTopic> buffer = new ArrayDeque<>();

    public WeiboHotSource(HttpClient httpClient, URI apiEndpoint, String apiToken) {
        this.httpClient = httpClient;
        this.apiEndpoint = apiEndpoint;
        this.apiToken = apiToken;
    }

    @Override
    public HotTopic next() throws Exception {
        if (buffer.isEmpty()) {
            List<HotTopic> fetched = fetchHotTopics();
            if (fetched.isEmpty()) {
                return null;
            }
            buffer.addAll(fetched);
        }
        return buffer.pollFirst();
    }

    private List<HotTopic> fetchHotTopics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiEndpoint)
                .header("Authorization", "Bearer " + apiToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("微博热榜接口调用失败，状态码=" + response.statusCode());
        }

        return parseHotTopics(response.body());
    }

    private List<HotTopic> parseHotTopics(String jsonBody) throws IOException {
        // 伪代码示意：假设接口返回 JSON 结构如下：
        // {
        //   "data": [
        //     {
        //       "topic_id": "123",
        //       "title": "热搜标题",
        //       "desc": "简要描述",
        //       "category": "社会",
        //       "source_url": "https://weibo.com/...",
        //       "hot_score": 98765.4,
        //       "mention_count": 12000,
        //       "sentiment": "positive",
        //       "tags": ["tag1", "tag2"],
        //       "first_seen_at": "2024-10-01T08:00:00",
        //       "updated_at": "2024-10-01T09:00:00"
        //     }
        //   ]
        // }

        // 这里不引入具体 JSON 解析库，仅展示字段映射关系。
        // 实际工程中可使用 Jackson/Gson/JSON-B 等完成反序列化。

        JSONArray items = decodeJsonArray(jsonBody, "data");
        if (items.isEmpty()) {
            return List.of();
        }
        List<HotTopic> topics = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null) {
                continue;
            }
            HotTopic topic = new HotTopic();
            topic.setId(item.getString("topic_id"));
            topic.setTitle(item.getString("title"));
            topic.setDescription(item.getString("desc"));
            topic.setCategory(item.getString("category"));
            topic.setSourcePlatform("weibo");
            topic.setSourceUrl(item.getString("source_url"));
            topic.setPopularityScore(decimalValue(item.get("hot_score")));
            topic.setMentionCount(intValue(item.get("mention_count")));
            topic.setSentiment(item.getString("sentiment"));
            topic.setKeywords(stringListValue(item.get("tags")));
            topic.setFirstSeenAt(dateTimeValue(item.getString("first_seen_at")));
            topic.setLastUpdatedAt(dateTimeValue(item.getString("updated_at")));
            topic.setStatus(defaultIfBlank(item.getString("status"), "active"));
            topic.setLanguage(defaultIfBlank(item.getString("language"), "zh-CN"));
            topic.setRegion(item.getString("region"));
            topic.setCreatedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());
            if (isInvalid(topic)) {
                continue;
            }
            topics.add(topic);
        }
        return topics;
    }

    private JSONArray decodeJsonArray(String jsonBody, String field) throws IOException {
        if (jsonBody == null || jsonBody.isEmpty()) {
            return new JSONArray();
        }
        try {
            JSONObject root = JSON.parseObject(jsonBody);
            JSONArray data = root.getJSONArray(field);
            return data == null ? new JSONArray() : data;
        } catch (Exception ex) {
            throw new IOException("解析微博热榜 JSON 失败", ex);
        }
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
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
            return null;
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> result = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                result.add(String.valueOf(array.get(i)));
            }
            return result;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> result = new ArrayList<>(list.size());
            for (Object item : list) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return new ArrayList<>(List.of(String.valueOf(value)));
    }

    private LocalDateTime dateTimeValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private boolean isInvalid(HotTopic topic) {
        return isBlank(topic.getId()) || isBlank(topic.getTitle());
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

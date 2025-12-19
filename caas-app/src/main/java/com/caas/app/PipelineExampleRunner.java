package com.caas.app;

import com.caas.domain.content.ArticleDraft;
import com.caas.domain.content.HotTopic;
import com.caas.domain.content.VideoScript;
import com.caas.integration.weibo.WeiboHotSource;
import com.caas.llm.LlmClient;
import com.caas.llm.LlmRequest;
import com.caas.llm.LlmResponse;
import com.caas.llm.processor.ArticleToVideoScriptProcessor;
import com.caas.llm.processor.TopicExplainProcessor;
import com.caas.pipeline.Pipeline;
import com.caas.pipeline.spi.Processor;
import com.caas.storage.output.FileSystemOutput;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 最小可运行示例：拉取热点 -> 生成文章 -> 生成视频脚本 -> 写入本地文件。
 */
public class PipelineExampleRunner {

    public static void main(String[] args) throws Exception {
        HttpServer server = startMockWeiboServer();
        URI endpoint = URI.create("http://localhost:" + server.getAddress().getPort() + "/weibo/hot");

        HttpClient httpClient = HttpClient.newHttpClient();
        WeiboHotSource source = new WeiboHotSource(httpClient, endpoint, "mock-token");

        LlmClient llmClient = new MockLlmClient();
        TopicExplainProcessor explainProcessor = new TopicExplainProcessor(llmClient, 0.7, 1200);
        ArticleToVideoScriptProcessor videoProcessor = new ArticleToVideoScriptProcessor(llmClient, 0.7, 1000);

        Processor<ArticleDraft, FileSystemOutput.ContentItem> packProcessor = article -> {
            VideoScript script = videoProcessor.process(article);
            return new FileSystemOutput.ContentItem(article, script);
        };

        FileSystemOutput output = new FileSystemOutput(Path.of("output"));

        List<Processor<?, ?>> processors = List.of(explainProcessor, packProcessor);
        Pipeline<HotTopic, FileSystemOutput.ContentItem> pipeline = new Pipeline<>(source, output, processors);

        pipeline.run();

        server.stop(0);
    }

    private static HttpServer startMockWeiboServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/weibo/hot", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String json = """
                    {
                      "data": [
                        {
                          "topic_id": "t-1001",
                          "title": "AI 助手在办公场景持续升温",
                          "desc": "越来越多企业尝试将 AI 助手融入日常办公流程。",
                          "category": "科技",
                          "source_url": "https://weibo.com/example",
                          "hot_score": 87654.3,
                          "mention_count": 15678,
                          "sentiment": "positive",
                          "tags": ["AI", "办公", "效率"],
                          "first_seen_at": "2024-10-01T08:00:00",
                          "updated_at": "2024-10-01T09:00:00",
                          "language": "zh-CN",
                          "region": "CN",
                          "status": "active"
                        }
                      ]
                    }
                    """;
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        return server;
    }

    /**
     * MVP 阶段的本地伪 LLM 客户端，用于保证示例可运行。
     */
    private static final class MockLlmClient implements LlmClient {

        @Override
        public LlmResponse generate(LlmRequest request) {
            String userPrompt = request.getUserPrompt();
            LlmResponse response = new LlmResponse();
            response.setRequestId(request.getRequestId());
            if (userPrompt != null && userPrompt.contains("JSON 格式要求")) {
                response.setContent(buildVideoScriptJson());
            } else {
                response.setContent(buildMarkdown());
            }
            return response;
        }

        private String buildMarkdown() {
            return """
                    # AI 助手在办公场景持续升温解读

                    ## 背景
                    AI 助手正在从实验走向落地，越来越多企业开始在办公场景中尝试。

                    ## 核心信息
                    - 主要场景集中在写作、总结与知识检索
                    - 试点部门更关注效率提升与成本控制

                    ## 影响分析
                    短期内会带来流程优化与岗位能力升级，中期需要关注数据合规与组织协同。

                    ## 观点小结
                    AI 助手的价值正在被验证，但需要以业务闭环和治理体系为前提。
                    """;
        }

        private String buildVideoScriptJson() {
            return """
                    {
                      "title": "AI 助手办公趋势解读",
                      "style": "知识解读",
                      "target_duration_seconds": 120,
                      "language": "zh-CN",
                      "narration": "AI 助手正逐步进入办公主流程，效率提升与治理规范并行。",
                      "segments": [
                        {
                          "order_index": 1,
                          "type": "intro",
                          "text": "为什么 AI 助手正在改变办公方式？",
                          "visual_notes": "快速剪影展示办公场景",
                          "duration_seconds": 8,
                          "asset_urls": [],
                          "start_offset_seconds": 0
                        },
                        {
                          "order_index": 2,
                          "type": "main",
                          "text": "从写作到总结，再到知识检索，AI 助手覆盖核心流程。",
                          "visual_notes": "信息卡片+流程示意",
                          "duration_seconds": 20,
                          "asset_urls": [],
                          "start_offset_seconds": 8
                        },
                        {
                          "order_index": 3,
                          "type": "outro",
                          "text": "关键是把 AI 融入业务闭环，并建立合规治理。",
                          "visual_notes": "结尾强调治理与价值",
                          "duration_seconds": 10,
                          "asset_urls": [],
                          "start_offset_seconds": 28
                        }
                      ]
                    }
                    """;
        }
    }
}

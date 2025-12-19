package com.caas.llm;

/**
 * 大模型客户端抽象，负责将请求转换为模型调用并返回结果。
 */
public interface LlmClient {

    /**
     * 执行一次模型调用。
     *
     * @param request 请求对象
     * @return 响应对象
     * @throws Exception 调用失败时抛出
     */
    LlmResponse generate(LlmRequest request) throws Exception;
}

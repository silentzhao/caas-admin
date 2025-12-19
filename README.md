# caas-admin

内容自动化工厂后端服务（Java 21、Maven 多模块）。

## 模块说明
- `caas-app`：应用入口与示例运行（`PipelineExampleRunner`）。
- `caas-core`：基础能力模块（预留公共工具与基础组件）。
- `caas-domain`：业务领域模型（热点、文章、视频脚本、内容包）。
- `caas-pipeline`：流水线 SPI 与同步 Pipeline 引擎。
- `caas-llm`：大模型解耦抽象层与处理器（Prompt 构建、解析）。
- `caas-integration`：外部数据源接入（如微博热榜）。
- `caas-storage`：结果输出与归档（本地文件系统输出）。

## 基础架构
- **数据流**：Source(热点) -> Processor(文章生成) -> Processor(视频脚本生成) -> Output(落盘)。
- **解耦策略**：LLM 通过 `LlmClient` 抽象与具体模型隔离。
- **扩展方式**：新增 Source/Processor/Output 即可组合新管线。

## 快速运行
本地最小示例：
```bash
mvn -pl caas-app -am exec:java -Dexec.mainClass=com.caas.app.PipelineExampleRunner
```
运行后输出目录：`output/yyyy-MM-dd/`。

## 相关文档
- `docs/服务与部署指南.md`

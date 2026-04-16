# ADR-0001: Camunda 7 在 Spring Boot 4 过渡期采用 Engine Only 策略

- 状态：Accepted
- 日期：2026-04-16

## 背景

当前平台已在核心业务中稳定使用 Camunda 7，但社区版已停止继续演进。
与此同时，平台基础技术栈需要逐步升级到 Spring Boot 4，以满足长期维护、
依赖治理和统一基线要求。

在这一背景下，存在三个直接矛盾：

- 现有业务对 Camunda 7 执行引擎依赖较深
- 短期内迁移到 Camunda 8 成本过高
- 若继续使用 Camunda 7，则需要自行承担 Spring Boot 4 兼容与后续维护责任

当前阶段的目标并不是建设完整的新流程平台，而是完成最小兼容落地：

- Spring Boot 4 可启动
- BPMN 可部署
- 核心流程可执行

## 决策

在 Spring Boot 4 过渡阶段，Camunda 7 仅作为嵌入式流程执行引擎使用。

生产环境采用：

`Engine Only + 自研 UI + 自研 IAM`

正式采用的 Camunda 组件为：

- `engine`
- `spring-boot-starter`
- `rest`，仅在明确需要时启用

不作为正式产品能力交付的组件为：

- `webapps`
- `tasklist`
- `cockpit`
- `admin`

非生产环境可按需保留 `cockpit` 作为排障工具，但不对业务用户开放，也不作为正式产品依赖。

## 决策说明

选择该策略的原因如下：

- 保持嵌入式架构，避免短期内引入分布式流程引擎迁移成本
- 控制兼容性改造范围，优先聚焦 starter 与运行时集成层
- 避免将 Camunda Webapps 适配为 SaaS 产品 UI，降低无效改造
- 保持未来迁移到 Camunda 8 或替代引擎的可行性

需要特别说明：

- Camunda 内置 identity、tenant、authorization 能力是存在的
- 但生产环境不采用其作为平台主 IAM、主租户模型、主权限模型
- 平台仍以自研 IAM、租户模型、权限模型为唯一主来源

## 结果与影响

该决策带来的直接影响包括：

- 平台必须自行提供任务中心、审批 UI、流程监控与基础运维能力
- 若启用 `rest`，应优先由平台服务封装后再对外暴露
- 不将 Camunda `starter-security` 作为 Engine Only 形态下的默认生产安全方案
- 需要明确 history、incident、retry、归档与运维权限等配套策略

## 后续约束

为避免过渡方案长期固化，新增业务代码应尽量避免在大量业务模块中直接扩散对 Camunda 原始 API 的依赖。

推荐通过平台自有的流程服务抽象访问工作流能力，以保留后续迁移空间。

## 参考文档

- [Engine Only 使用策略](../camunda-7-spring-boot-4-engine-only-strategy.md)

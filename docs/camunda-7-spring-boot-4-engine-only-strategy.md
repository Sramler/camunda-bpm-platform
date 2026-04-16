# Camunda 7 on Spring Boot 4: Engine Only 使用策略

## 1. 文档目的

本文用于明确 Camunda 7 在当前 “Spring Boot 4 最小兼容” 阶段的使用策略。

当前阶段的核心目标是：

- 保持 Camunda 7 作为嵌入式流程执行引擎使用
- 完成 Spring Boot 4 启动所需的最小兼容改造
- 保证核心流程能力可用
- 不将范围扩展到 Webapps 全量适配
- 为未来迁移到 Camunda 8 或替代方案保留空间

本文是产品和架构层面的决策说明，不是源码兼容 patch 清单。

## 2. 最终决策

生产环境采用：

`Engine Only + 自研 UI + 自研 IAM`

正式纳入的 Camunda 组件为：

- `engine`
- `spring-boot-starter`
- `rest`，仅在明确需要时启用

不作为正式产品能力交付的组件为：

- `webapps`
- `tasklist`
- `cockpit`
- `admin`

非生产环境中，可按需保留 `cockpit` 作为临时排障工具，但不得对业务用户开放，也不得成为正式产品依赖。

## 3. 适用范围

本决策服务于当前 Spring Boot 4 最小兼容目标。

本阶段仅关注：

- Spring Boot 4 启动成功
- BPMN 可部署
- 核心流程逻辑可执行，例如 `ServiceTask`

本阶段不包括：

- 将 Camunda Webapps 适配成正式 SaaS 产品 UI
- 将 Camunda Admin 作为平台 IAM 管理台
- 追求 Camunda 平台 UI 的完整功能对齐
- 大范围改造引擎核心逻辑

## 4. 职责边界

在本策略下，Camunda 的定位是：

嵌入式流程执行引擎

平台自身负责：

- 流程设计器
- 流程发布入口
- 任务中心
- 审批 UI
- 身份认证
- 租户模型
- 权限模型
- 流程监控视图
- 运维和恢复能力

基本原则是：

Camunda 负责执行。
平台负责产品化。

## 5. 为什么生产不采用 Webapps / Admin

`Tasklist`、`Cockpit`、`Admin` 都是 Camunda 的真实能力，但它们不适合作为当前 SaaS 平台的正式产品层。

主要原因包括：

- UI 技术栈与平台不统一
- 认证和会话模型与平台 IAM 不统一
- Camunda tenant 不等于 SaaS tenant
- 二次开发成本高，长期可控性弱
- 产品入口、权限边界和用户体验无法统一

因此，正确表述应为：

Camunda 具备这些能力，但生产环境不采用它们作为平台交付模型。

不应表述为：

Camunda 不具备这些能力。

## 6. Identity / Admin / Tenant 的准确规则

Camunda 7 内建以下概念：

- user
- group
- tenant
- authorization

Webapps / Admin 也可以管理这些对象。

但在本策略下：

- 不采用 Camunda 内置 identity 作为生产平台 IAM
- 不采用 Camunda tenant 作为平台主租户模型
- 不采用 Camunda authorization 作为平台主权限模型

平台仍然是以下能力的唯一主来源：

- 用户身份
- 租户隔离
- 角色和属性授权
- 会话与令牌生命周期

Camunda 的 identity / tenant 能力可以在引擎边界内存在，但不能成为正式产品主模型。

## 7. 组件选型规则

### 7.1 生产基线

生产环境使用尽可能小的组件集合：

- `engine`
- `spring-boot-starter`
- `rest`，仅在确有集成需求时启用

### 7.2 Webapps

生产环境不得依赖：

- `camunda-bpm-spring-boot-starter-webapp`
- `tasklist`
- `cockpit`
- `admin`

若不需要 Webapps，应在运行时排除相关依赖，或通过配置显式关闭。

### 7.3 REST

`rest` 是可选组件，不是必选组件。

仅在以下场景启用：

- 外部系统需要通过 HTTP 与流程能力交互
- 平台需要受控的引擎集成面
- 部分运维或查询能力需要 API 化

即便启用，也推荐遵循以下原则：

除非安全边界完全受控，否则不要将原生 Camunda REST 直接暴露给产品前端或业务客户端。

推荐方式是：

由平台服务对所需引擎能力做封装、代理或收口后再对外提供。

## 8. 安全集成规则

在 `Engine Only` 策略下，不将 Camunda `starter-security` 作为默认生产方案。

原因是：

它的设计中心更偏向 Camunda 自带 `/app`、`/api` 路径的安全集成，更适合 Webapps 场景，而不是平台自研 UI 场景。

因此：

- 生产认证由平台安全体系负责
- 生产授权由平台安全体系负责
- 租户隔离由平台安全体系负责
- Camunda 自带 Web 安全集成不得成为正式产品主安全模型

## 9. 平台必须自行实现的能力

如果不采用 Webapps，平台必须提供替代能力。

至少需要覆盖：

- 流程设计与发布
- 待办和已办任务中心
- 审批表单与审批动作
- 流程实例查询
- 历史与轨迹查询
- 当前节点定位
- incident 管理
- 失败 job 重试处理
- 异常流程恢复
- 审计与运维留痕

关键原则是：

替代 Cockpit 不是只做查询页面。

还必须具备基本的运维级处理能力。

## 10. 运维与数据前提

如果平台要自行替代 Webapps / Cockpit，则必须同步明确以下运维问题：

- history 级别策略
- 历史数据保留策略
- 清理与归档策略
- incident 处理流程
- retry 与 dead-job 处理流程
- 运维权限边界

需要强调的是：

不用 Cockpit，不代表不需要监控、历史和运维能力。

它只代表这些责任转移到平台自身承担。

## 11. 非生产环境策略

在开发、测试和排障环境中，可临时保留 `cockpit` 用于：

- 查看流程实例
- 排查 incident
- 分析 job executor 状态
- 辅助兼容性验证

限制条件：

- 不对业务用户开放
- 不作为正式产品功能
- 不作为生产依赖前提

## 12. 源码层面的澄清

本策略是运行时和产品层面的决策。

这并不意味着源码仓库必须立即删除所有 Webapp 相关模块。

在源码层面，Webapp 模块仍可能因为以下原因继续存在：

- 历史兼容
- starter 模块结构
- 构建 profile
- 回归测试拓扑

因此，正确的实施理解应为：

生产架构不采用 Webapps 作为产品能力。

不应误读为：

只有在源码树中彻底移除所有 Webapp 代码后，该策略才成立。

## 13. 迁移护栏

由于本方案是过渡方案，新平台代码应避免在大量业务模块中直接扩散对原始 Camunda API 的依赖。

推荐原则是：

业务模块优先通过平台自有的流程服务抽象访问工作流能力。

这样可以为未来保留迁移空间，包括：

- Camunda 8
- 替代工作流引擎
- 平台内部流程能力解耦或替换

## 14. 最终总结

最终架构立场是：

- 生产环境采用 `Engine Only + 自研 UI + 自研 IAM`
- `webapps`、`tasklist`、`cockpit`、`admin` 不作为正式生产交付组件
- `rest` 是可选组件，启用后也应受控暴露
- 产品 UI、IAM、租户、监控、运维能力均由平台侧承担

一句话总结：

在当前 Spring Boot 4 过渡阶段，Camunda 7 仅作为嵌入式流程执行引擎使用，而不是作为正式产品 UI 或 IAM 平台使用。

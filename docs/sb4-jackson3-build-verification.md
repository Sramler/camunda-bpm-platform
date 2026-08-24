# Camunda 7 SB4 / Jackson 3 构建收口记录

最后更新：2026-08-24

适用仓库：

- `/Users/bliu/code/camunda-bpm-platform`

适用分支：

- `codex/camunda7-sb4-fork`

当前不可变 fork 版本：

- `7.24.0-tiny-sb4-jackson3-01`

> `7.24.0-tiny-sb4-01` 已作为既有制品使用。本工作区的 Jackson 3
> 完整产物不得原地覆盖该版本。本轮使用新的不可变版本号
> `7.24.0-tiny-sb4-jackson3-01`。

## 1. 本轮收口目标

本轮主要处理三类问题：

- 收口 Spring Boot 4.1.0-SNAPSHOT / Spring 7.0.7-SNAPSHOT 经 `camunda-nexus` 触发的 Maven metadata / 401 噪音
- 收口 Jackson 3 迁移后阻断 `-DskipTests` 打包路径的 `spin/dataformat-json-jackson:testCompile` 问题
- 完成 REST Provider 与 Spin 历史语义的 Jackson 3 运行时验收

本轮不处理：

- Webapps 全量适配
- 全仓所有历史 Maven warning 清零
- 与当前最小运行目标无关的产品化能力

## 2. 已完成调整

### 2.1 Spring / Repository 噪音治理

- `parent/pom.xml`
  - `version.spring-boot` 从 `4.1.0-SNAPSHOT` 收敛到 `4.1.0-M4`
  - Spring 预览仓库保留在显式 profile 中
- `pom.xml`
  - 移除默认激活的 `camunda-nexus` 仓库配置

结果：

- 不再出现 `4.1.0-SNAPSHOT`
- 不再出现 `7.0.7-SNAPSHOT`
- 不再出现与这些快照元数据相关的 `401`

### 2.2 非运行时打包附件裁剪

为降低 `sources/tests` 分类附件解析带来的噪音，保留最小运行 fork 目标，做了以下裁剪：

- `engine/pom.xml`
  - 关闭 shaded `sources` 产物
  - 关闭 shaded `test` jar 产物
- `spin/dataformat-all/pom.xml`
  - 关闭 shaded `sources` 产物
- `connect/connectors-all/pom.xml`
  - 关闭 shaded `sources` 产物
- `engine-rest/assembly/pom.xml`
  - 移除 `sources/tests` classifier 依赖与对应装配执行
- `engine-rest/assembly-jakarta/pom.xml`
  - 移除 `sources/tests` classifier 依赖与对应装配执行

说明：

- 这是面向内部最小运行 fork 的有意识取舍
- 运行时主制品保留
- 辅助型源码 / 测试分类附件不再作为默认交付目标

### 2.3 Jackson 3 测试源码适配

已修复 `spin/dataformat-json-jackson` 下测试源码残留的 Jackson 2 包名引用：

- `spin/dataformat-json-jackson/src/test/java/org/camunda/spin/impl/json/jackson/format/JsonDeserializationValidationTest.java`
- `spin/dataformat-json-jackson/src/test/java/org/camunda/spin/json/tree/JsonTreeMapJsonToJavaTest.java`
- `spin/dataformat-json-jackson/src/test/java/org/camunda/spin/json/mapping/CustomerList.java`
- `spin/dataformat-json-jackson/src/test/java/org/camunda/spin/json/mapping/GenericCustomerList.java`

修复内容包括：

- `com.fasterxml.jackson.*` -> `tools.jackson.*`
- `TypeFactory.defaultInstance()` -> `TypeFactory.createDefaultInstance()`
- `JsonProcessingException` -> `JacksonException`

结果：

- `spin/dataformat-json-jackson:testCompile` 已恢复成功
- 之前只能依赖 `-Dmaven.test.skip=true` 的打包路径，现已可在普通 `-DskipTests` 下通过

## 3. 验证结果

### 3.1 Starter 编译链

命令：

```bash
mvn -pl spring-boot-starter/starter -am -DskipTests -Dmaven.repo.local=/usr/local/data/repo compile
```

结果：

- `BUILD SUCCESS`

日志文件：

- `target/codex-sb4-verification/starter-compile.log`
- `target/codex-sb4-verification/starter-compile-post-model-cleanup.log`

关键词检查：

- `camunda-nexus`
- `401`
- `4.1.0-SNAPSHOT`
- `7.0.7-SNAPSHOT`
- `spring-snapshots`
- `spring-milestones`

结果：

- 零命中

补充说明：

- 2026-08-24 已再次验证，不再出现以下 effective model 告警：
  - `${version}` deprecated，请改用 `${project.version}`
  - `maven-dependency-plugin` 重复声明

### 3.2 Spin Jackson 测试编译

命令：

```bash
mvn -pl spin/dataformat-json-jackson -DskipTests -Dmaven.repo.local=/usr/local/data/repo test-compile
```

结果：

- `BUILD SUCCESS`

说明：

- 本命令此前会在 `JsonDeserializationValidationTest` 上因 `com.fasterxml.jackson.databind.JavaType` 与 `tools.jackson.databind.JavaType` 不兼容而失败
- 当前已恢复

### 3.3 Java Client 打包链

命令：

```bash
mvn -pl clients/java/client -am -DskipTests -Dmaven.repo.local=/usr/local/data/repo package
```

结果：

- `BUILD SUCCESS`

日志文件：

- `target/codex-sb4-verification/java-client-package-skiptests.log`
- `target/codex-sb4-verification/java-client-package-maven-test-skip.log`

关键词检查：

- `camunda-nexus`
- `401`
- `4.1.0-SNAPSHOT`
- `7.0.7-SNAPSHOT`
- `spring-snapshots`
- `spring-milestones`
- `Could not get tests`
- `Could not get sources`

结果：

- 零命中

### 3.4 Spin Jackson 3 完整模块测试

命令：

```bash
mvn -pl spin/dataformat-json-jackson -am test -DskipITs
```

结果：

- 671 条测试通过，0 失败、0 错误、5 条按运行时条件跳过；
- 旧数字日期 payload 可读，Date 默认仍按历史数字时间戳写回；
- 未知字段保持 fail-fast；
- 结构节点文本访问继续包装为稳定的 Spin 异常；
- Java object 映射和 JSONPath 由完整模块测试覆盖。

## 4. 当前结论

截至 2026-08-24，本 fork 已达到以下状态：

- Spring Boot 4 快照元数据噪音已收口
- `camunda-nexus` 相关依赖解析噪音已不再出现在关键验证日志中
- `spin/dataformat-json-jackson` 的 Jackson 3 测试编译阻断已修复
- 常见消费者路径 `clients/java/client -am -DskipTests package` 已打通
- 早前遗留的两个 Maven model warning 已收口

因此，当前可以认为：

`7.24.0-tiny-sb4-jackson3-01` 在“内部最小运行 fork + Jackson 3 主线推进”目标下，已完成一轮有效构建收口。

## 5. 仍存在但暂不阻断的问题

以下问题仍可能继续出现，但当前不阻断本轮目标：

- 部分 assembly / shade 步骤存在常规重叠资源 warning
- 若未来扩大验证范围，仍应继续检查更多模块的 Jackson 3 残留

这些问题建议单独建卡处理，不与本轮“SB4 / Jackson 3 最小运行收口”混在一起。

## 6. 2026-08-24 REST / Boot 4.1.1 增量验证

本轮继续完成 REST starter 的 Jackson 3 运行时收口：

- Spring Boot 对齐 `4.1.1`，Jackson 对齐 `3.1.5`，Surefire 对齐 `3.5.6`；
- `spring-boot-starter-jersey` 排除 `spring-boot-jackson2` 与
  `jersey-media-json-jackson`；
- `CamundaJerseyResourceConfig` 停止注册 Jackson 2 `JacksonFeature`；
- REST core 继续注册 `tools.jackson.jakarta.rs` 官方 Jakarta REST Provider；
- 补齐 Boot 4 拆分后的 rest-test-client、rest-client、health 测试依赖；
- 增加 REST 变量 JSON 类型往返和非法 JSON 4xx 回归。

验证结果：

- REST starter 45 模块 reactor compile：通过；
- REST runtime 定向依赖树：未发现 `spring-boot-jackson2`、
  `jersey-media-json-jackson` 或未 shaded Jackson 2 实现；
- `SampleCamundaRestApplicationIT` 随机端口真实 HTTP 测试：6 条通过，
  0 失败、0 错误。

制品与消费者验证：

- 185 个 reactor POM 已统一使用新版本，未覆盖旧 `7.24.0-tiny-sb4-01`；
- REST 45 模块、BOM 链和 `spin/dataformat-all` 已安装到本地 Maven 仓库；
- tiny-platform 已切换到该版本，默认 Engine Only 与 `camunda-rest` 两套依赖树门禁均通过；
- tiny-platform `-Pcamunda-rest` 在真实 MySQL 上完成 engine 查询、REST 部署、流程启动、
  String/Integer/Boolean/Date 变量往返、非法 JSON 4xx 和级联清理；测试残留为 0。

发布边界：本地不可变制品与消费者验收已经完成；若需供其他环境使用，仍须按组织制品库流程
发布同一版本内容，禁止重新构建不同内容覆盖该版本。

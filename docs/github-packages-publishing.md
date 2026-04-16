# Camunda Fork GitHub Packages Publishing

最后更新：2026-04-16

适用仓库：

- `/Users/bliu/code/camunda-bpm-platform`

## 1. 目的

本说明用于落实 `CARD-C7-05B`：

把 Camunda 7 / Spring Boot 4 fork 产物发布到 GitHub Packages，
让 `tiny-platform` 和 GitHub Actions 不再依赖开发机本地 Maven 仓库。

## 2. 当前发布目标

本轮最小发布集为：

- `org.camunda.bpm:camunda-bom`
- `org.camunda.bpm:camunda-only-bom`
- `org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter`
- `org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest`
- `org.camunda.spin:camunda-spin-dataformat-all`
- 以及它们的最小传递依赖链

当前默认版本线：

- `7.24.0-tiny-sb4-01`

说明：

- `7.24.0-tiny-sb4-01-SNAPSHOT` 已完成开发阶段验证
- 当前主线消费与发布口径统一收敛到固定版 `7.24.0-tiny-sb4-01`

## 3. 关键实现

### 3.1 Maven 发布端点

根 POM 已增加：

- `distributionManagement`
- 仓库 id：`github`
- 发布地址：`https://maven.pkg.github.com/sramler/camunda-bpm-platform`

说明：

- GitHub Packages Maven 仓库要求使用小写 owner 名称
- 发布 workflow 使用 `actions/setup-java` 生成 `settings.xml`
- 认证凭据走 `GITHUB_ACTOR` + `GITHUB_TOKEN`

### 3.2 GitHub Actions 发布 workflow

已新增：

- `.github/workflows/publish-camunda-fork-github-packages.yml`

行为：

- 手动触发
- 校验版本名中包含 `-tiny-sb4-`
- 固定版发布前先本地安装 `spin/core`，补齐 `camunda-spin-core:tests`
- 再部署主最小 fork 产物集合，不做全量 distro 发布

原因：

- `spin/dataformat-json-jackson` 会拉取 `org.camunda.spin:camunda-spin-core:tests`
- 如果直接对主最小制品集执行 `-Dmaven.test.skip=true deploy`，该 `tests.jar` 不会被产出
- 如果只保留 `-DskipTests`，`spring-boot-starter/starter` 的 Boot 4 兼容测试源码又会在 `testCompile` 阶段失败
- 因此固定版发布口径必须是：
  - 先 `spin/core` 本地 `install`
  - 再对主最小制品集执行 `deploy`

## 4. 推荐发布顺序

1. 在 `camunda-bpm-platform` 确认当前版本号正确
2. 如需本地预演，先执行：
   - `mvn -B -pl spin/core -am -DskipTests -DskipITs install`
   - `mvn -B -pl bom/camunda-bom,bom/camunda-only-bom,spin/bom,commons/bom,connect/bom,spin/dataformat-all,spring-boot-starter/starter,spring-boot-starter/starter-rest -am -Dmaven.test.skip=true -DskipTests -DskipITs install`
3. 手动触发 `Publish Camunda fork to GitHub Packages`
4. 到 GitHub Packages 页面确认以下制品已可见：
   - `camunda-bom`
   - `camunda-only-bom`
   - `camunda-bpm-spring-boot-starter`
   - `camunda-bpm-spring-boot-starter-rest`
   - `camunda-spin-dataformat-all`
5. 到 package 设置页为 `tiny-platform` 仓库开启 `Manage Actions access`
6. 在 `tiny-platform` 运行一个后端 workflow 验证消费成功

## 5. 消费侧注意事项

消费仓库位于：

- `/Users/bliu/code/tiny-platform`

GitHub 官方文档给出的关键约束是：

- 发布同仓库 package 时，workflow 可直接使用 `GITHUB_TOKEN`
- 安装其他私有仓库的 package 时，推荐使用至少带 `read:packages` 的 classic PAT
- 如果目标 package 已显式授予 workflow 所在仓库的 Actions 访问权限，`GITHUB_TOKEN` 也可以用于读取

因此对 `tiny-platform` 的推荐顺序是：

1. 首选：
   - 为 consumer workflow 配置 `CAMUNDA_PACKAGES_TOKEN`（classic PAT，至少 `read:packages`）
2. 次选：
   - 在 `camunda-bpm-platform` package 设置页为 `tiny-platform` 开启 Actions 读取权限
   - consumer workflow 回退使用 `github.token`

## 6. 非目标

本说明当前不处理：

- Maven Central 发布
- `tiny-platform` 所有 workflow 的逐一回归结论

这些属于：

- `CARD-C7-05C`
- 额外的 CI 扩面回归收口

## 7. 一句话结论

当前发布主路径已经落地为：

`camunda-bpm-platform` 手动发布到 GitHub Packages  
`tiny-platform` 通过受控 profile + workflow 认证去消费

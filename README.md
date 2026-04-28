<div align="center">

# 🏞️ 沁湖驿站云服务平台
### QinhuOasis Service Hub

> 一站式智慧景区综合服务系统 · 双语国际化 · 高并发防超卖 · 对象存储全链路

---

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.3-C0392B?style=flat-square)
![MinIO](https://img.shields.io/badge/MinIO-8.5.9-C72E49?style=flat-square&logo=minio&logoColor=white)
![uniapp](https://img.shields.io/badge/uni--app-Vue3-42b883?style=flat-square&logo=vuedotjs&logoColor=white)
![Hutool](https://img.shields.io/badge/Hutool-5.8.26-informational?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Lines of Code](https://img.shields.io/badge/Java_LOC-5183-blueviolet?style=flat-square)
![Files](https://img.shields.io/badge/Source_Files-94-blue?style=flat-square)

</div>

---

## 📌 项目简介

**沁湖驿站云服务平台**是一套面向景区的全栈综合服务系统，覆盖餐厅美食、智慧停车、游客内容（UGC）、学生译员服务和投诉建议五大业务域。

后端采用 **Spring Boot 3.2.5 模块化单体架构**，以 Redis 承载高并发热点数据，以 MinIO 管理多类型媒体资产；前端基于 **uniapp + Vue3** 实现「一套代码、多端发布」。全系统从接口层到数据层均支持 **中英文双语（zh-CN / en-US）** 无缝切换。

---

## ✨ 技术亮点（Key Features）

### 🔴 Redis Lua 脚本保障高并发原子性

车位抢占场景是经典的「超卖」重灾区。本项目使用 **单条 Lua 脚本** 将「读库存 → 判断 → 扣减」合并为 Redis 的单个原子操作，彻底消除 TOCTOU 竞态条件：

```lua
-- parking_deduct_stock.lua
local stock = redis.call('GET', KEYS[1])
if stock == false then return -1 end   -- Key不存在（区域未初始化）
if tonumber(stock) <= 0 then return 0 end  -- 库存耗尽
redis.call('DECR', KEYS[1])
return 1                               -- 扣减成功
```

配合 Spring `@Transactional` 在数据库层以 **CAS 乐观锁**（`WHERE available_count > 0`）兜底，即使 Lua 成功而 DB 写失败，也会在 `catch` 块中 `INCR` 补回 Redis 库存，**双层防护、零超卖**。

---

### 🟡 Redis ZSet 驱动实时热度排行

餐厅热度排行使用 **Redis ZSet** 承载，Score 公式为：

```
sort_score = rating × 20 + LOG₁₀(review_count + 1) × 10
```

应用启动时由 `RedisDataInitializer`（`ApplicationRunner`）将 MySQL 全量数据预热到 ZSet。每次新增评价后异步 `ZADD` 覆盖更新，**查询排行 O(log N)，写入评价 O(log N)**，不影响主链路性能。

---

### 🟢 MinIO 分布式对象存储全链路

系统以 **4 个独立桶**隔离不同安全等级的资产：

| 桶名 | 访问策略 | 存储内容 |
|------|---------|---------|
| `qosh-ugc-images` | public-read | 游客攻略图、评论晒图 |
| `qosh-interpreter-certs` | 私密 | 译员资质证书（学号/成绩单）|
| `qosh-public-static` | public-read | 景区地图、官方攻略配图 |
| `qosh-sys-assets` | 私密 | 报表导出、日志备份 |

文件路径规则：`{bucket}/yyyyMMdd/{UUID}.{ext}`，天级分区防止单目录膨胀；`FileStorageService` 统一校验 MIME 类型（JPEG/PNG/WebP/GIF），杜绝恶意文件上传。

---

### 🔵 i18n 国际化全链路支持

国际化渗透到每一层，前端只需切换 `Accept-Language` Header：

```
请求层：Accept-Language: en-US  →  zh-CN（默认）
         ↓
拦截器：LocaleContextHolder.set(Locale)  ← 解析 Header 写入 ThreadLocal
         ↓
Service：LocaleContextHolder.get().getLanguage()
         → isEnglish() → 选择 titleEn / introductionEn
         ↓
VO 层：displayTitle / displayName / displayIntroduction（已解析字段）
         ↓
错误信息：I18nUtil.msg(ResultCode) → MessageSource.getMessage(key, locale)
```

数据库双语字段（`name` / `name_en`，`title` / `title_en`）+ 错误消息三文件（`messages.properties` / `messages_zh_CN.properties` / `messages_en_US.properties`）完整覆盖所有业务场景。

---

### ⚫ 阿里巴巴 Java 开发手册（嵩山版）严格落地

项目以 **P3C 规范**作为编码底线，核心约束如下：

| 规范条款 | 项目实践 |
|---------|---------|
| 禁止魔法值，使用常量类 | `PostType` / `OrderStatus` / `UserRole` 等 10+ 个 `final class` 常量 |
| 接口方法必须有 Javadoc | 所有 `Service` 接口、`Mapper` 接口方法均附带完整注释 |
| `@author` 标注到类 | 全量 94 个 Java 文件统一注明 `@author AiKiFan` |
| 事务注解 `rollbackFor` 明确指定 | 全部写操作使用 `@Transactional(rollbackFor = Exception.class)` |
| 日志使用 `@Slf4j`，禁用 `System.out` | Service 实现层统一使用 SLF4J，关键操作打印结构化日志 |
| 集合返回不允许 `null` | Mapper 返回列表时 MyBatis 自动返回空集合，VO 层无 NPE 风险 |

---

## 🗂️ 项目目录结构

```
qinhu-smart-tourism-cloud-platform/
│
├── QinhuOasisServiceHub-backend/          # Spring Boot 后端
│   ├── pom.xml                            # Maven 依赖（Java 17 / Spring Boot 3.2.5）
│   └── src/main/
│       ├── java/com/qinhu/oasis/
│       │   ├── OasisApplication.java       # 启动类（@MapperScan 覆盖所有模块）
│       │   │
│       │   ├── common/                    # ★ 公共基础设施层
│       │   │   ├── config/                # WebMvcConfig（跨域 + 拦截器注册）
│       │   │   ├── constant/              # 枚举常量（UserRole/OrderStatus/PostType…）
│       │   │   ├── exception/             # BizException + GlobalExceptionHandler
│       │   │   ├── i18n/                  # I18nUtil + LocaleContextHolder（ThreadLocal）
│       │   │   ├── init/                  # RedisDataInitializer（ApplicationRunner）
│       │   │   ├── result/                # Result<T> + PageResult<T> + ResultCode
│       │   │   ├── security/              # JwtUtil + AuthInterceptor + LoginUser（ThreadLocal）
│       │   │   └── service/               # FileStorageService（MinIO 统一上传）
│       │   │
│       │   ├── sys/                       # 用户认证域
│       │   │   ├── entity/SysUser.java
│       │   │   ├── dto/                   # RegisterReq / LoginVO / UserInfoVO
│       │   │   ├── mapper/SysUserMapper
│       │   │   ├── service/impl/SysUserServiceImpl  # BCrypt 加密，Snowflake 无状态 JWT
│       │   │   └── controller/            # AuthController + UserController
│       │   │
│       │   ├── restaurant/                # 美食点评域（Redis ZSet 热度排行）
│       │   │   ├── entity/Restaurant.java
│       │   │   ├── dto/                   # RestaurantListVO / DetailVO / RankListVO
│       │   │   ├── mapper/RestaurantMapper
│       │   │   ├── service/impl/          # ZSet 排行 + i18n displayName
│       │   │   └── controller/RestaurantController
│       │   │
│       │   ├── tourism/                   # 智慧旅游域（Lua 原子防超卖）
│       │   │   ├── entity/                # ParkingSpace + BizOrder
│       │   │   ├── dto/                   # ParkingSpaceVO / ParkingOrderReq / ParkingOrderVO
│       │   │   ├── mapper/                # ParkingSpaceMapper + BizOrderMapper
│       │   │   ├── service/impl/ParkingServiceImpl  # Lua 脚本 + CAS 乐观锁 + 补偿事务
│       │   │   └── controller/ParkingController
│       │   │
│       │   ├── ugc/                       # 用户内容域（攻略 / 评论 / 点赞 / 上传）
│       │   │   ├── entity/                # UgcPost + BizComment + UgcLike
│       │   │   ├── dto/                   # CreatePostReq / PostListVO / PostDetailVO…
│       │   │   ├── mapper/                # UgcPostMapper + BizCommentMapper + UgcLikeMapper
│       │   │   ├── service/impl/          # 点赞幂等 + @JsonRawValue 图片数组 + 浏览量++
│       │   │   └── controller/            # PostController + CommentController + FileController
│       │   │
│       │   ├── interpreter/               # 译员服务域（档案 / 接单 / 订单）
│       │   │   ├── entity/InterpreterProfile.java
│       │   │   ├── dto/                   # ApplyInterpreterReq / InterpreterVO / BookReq…
│       │   │   ├── mapper/                # InterpreterProfileMapper + InterpreterOrderMapper
│       │   │   ├── service/impl/          # 审核通过自动升级 role + 时薪自动计算
│       │   │   └── controller/            # InterpreterProfileController + OrderController
│       │   │
│       │   └── feedback/                  # 投诉建议域（匿名提交 / 管理员处理）
│       │       ├── entity/SysFeedback.java
│       │       ├── dto/                   # CreateFeedbackReq / FeedbackVO / ReplyReq
│       │       ├── mapper/SysFeedbackMapper
│       │       ├── service/impl/FeedbackServiceImpl
│       │       └── controller/FeedbackController
│       │
│       └── resources/
│           ├── application.yml            # 数据源 / Redis / MinIO / JWT 全量配置
│           ├── db/V1__init_schema.sql     # Flyway 迁移脚本（9 张表 + 初始数据）
│           ├── i18n/                      # messages.properties（zh/en 双语）
│           └── mapper/                    # MyBatis XML（按模块分目录，10 个 XML）
│               ├── sys/ restaurant/ tourism/
│               ├── ugc/ interpreter/ feedback/
│
├── QinhuOasisServiceHub-frontend/         # uniapp 前端（Vue3，开发中）
│
├── QinhuOasisServiceHub.sql               # 数据库一键初始化脚本（含测试数据）
├── FRONTEND_WIKI.md                       # API 接口全量文档（22 个端点 + 调用示例）
├── DEV_LOG.md                             # 开发日志（记录每个 Step 的关键决策）
└── README.md
```

---

## 🏛️ 架构设计

### 分层职责

```
┌─────────────────────────────────────────────────────┐
│  Controller 层   │  参数校验（@Valid）+ 登录鉴权        │
│                  │  统一返回 Result<T> 包装             │
├─────────────────────────────────────────────────────┤
│  Service 层      │  业务逻辑 + 事务边界                 │
│                  │  DTO ↔ Entity 手动映射              │
│                  │  i18n 字段解析（displayXxx 填充）    │
├─────────────────────────────────────────────────────┤
│  Mapper 层       │  MyBatis XML 纯 SQL，无注解污染      │
│                  │  map-underscore-to-camel 自动映射   │
├─────────────────────────────────────────────────────┤
│  Redis 层        │  ZSet 排行 / String 库存 / Lua 原子  │
│  MySQL 层        │  持久化 + CAS 乐观锁兜底             │
│  MinIO 层        │  对象存储（4 桶 × 权限隔离）          │
└─────────────────────────────────────────────────────┘
```

### DTO → Entity 转化逻辑

本项目不引入 MapStruct，遵循 **Alibaba P3C「显式优于魔法」** 原则，在 Service 层手动完成对象映射：

```java
// ✅ 标准写法（以 createPost 为例）
public PostDetailVO createPost(CreatePostReq req, Long userId) {
    // 1. Req → Entity（入参 DTO 转持久化实体）
    UgcPost post = new UgcPost();
    post.setUserId(userId);
    post.setTitle(req.getTitle());
    post.setImages(JSONUtil.toJsonStr(req.getImages())); // List → JSON String
    post.setStatus(req.getPostType() == PostType.DYNAMIC
            ? PostStatus.PUBLISHED : PostStatus.REVIEWING);

    // 2. 持久化
    ugcPostMapper.insert(post);

    // 3. Entity / DB 查询结果 → VO（出参 DTO，带 JOIN 字段）
    PostDetailVO vo = ugcPostMapper.selectDetailById(post.getId());
    vo.setDisplayTitle(resolveTitle(vo)); // i18n 填充
    return vo;
}
```

**映射规则汇总：**

| 场景 | 转化策略 |
|------|---------|
| 入参 Req → Entity | Service 层逐字段 `set`，包含类型转换（`List<String>` → JSON） |
| DB 结果 → VO | MyBatis `resultType` 直接映射，`map-underscore-to-camel` 自动处理命名 |
| 多表 JOIN | XML 中使用 `AS` 别名（`u.nickname AS author_nickname`），自动 camelCase |
| i18n 字段 | Service 查询后调用 `resolveXxx()` 方法，基于 `LocaleContextHolder` 填充 `displayXxx` |
| 图片 JSON | DB 存储为 `VARCHAR`，VO 响应时 `@JsonRawValue` 直出 JSON 数组，前端免 parse |

---

## 🗄️ 数据库设计

### 9 张核心表

```
sys_user              用户表（游客 / 学生译员 / 管理员三角色）
interpreter_profile   译员档案（审核状态机 + 时薪 + 证书 URL）
biz_order             统一订单表（多态设计：翻译服务 + 车位预约）
parking_space         停车区域（available_count = Redis 库存镜像）
biz_restaurant        餐厅表（sort_score = rating×20 + LOG₁₀(n+1)×10）
ugc_post              攻略/动态（双语标题 + JSON 图片数组）
biz_comment           评价/评论（多态 target_type，支持多级）
ugc_like              点赞记录（UNIQUE KEY 防重复，toggle 语义）
sys_feedback          投诉建议（支持匿名，管理员回复状态机）
```

### 关键设计决策

- **biz_order 多态**：`order_type=1` 为翻译订单，`order_type=2` 为车位订单，公共字段复用，专属字段允许 NULL，两套 Mapper（`BizOrderMapper` / `InterpreterOrderMapper`）按业务域分离查询。
- **软删除**：`deleted TINYINT(1)` 字段，所有查询加 `AND deleted = 0`，保留业务审计链路。
- **乐观锁**：`UPDATE parking_space SET available_count = available_count - 1 WHERE id = ? AND available_count > 0` 防止数据库层面负库存。

---

## 🚀 快速开始

### 环境要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | Spring Boot 3.x 要求 |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.x / 7.x | 缓存与队列 |
| MinIO | RELEASE.2023+ | 对象存储 |
| Node.js | 18+ | uniapp 前端开发 |
| HBuilderX | 最新版 | uniapp IDE |

---

### 后端启动

#### 第一步：初始化数据库

```bash
# 连接 MySQL，执行初始化脚本（含表结构 + 测试数据 + admin 账号）
mysql -u root -p < QinhuOasisServiceHub.sql
```

#### 第二步：启动 MinIO

```bash
# Windows（在 MinIO 安装目录执行）
cd D:\MinIO\bin
minio.exe server data --console-address ":9001"

# 访问控制台：http://localhost:9001
# 账号：root  密码：12345678
# 手动创建 4 个 Bucket：
#   qosh-ugc-images（设置 public-read 策略）
#   qosh-interpreter-certs
#   qosh-public-static（设置 public-read 策略）
#   qosh-sys-assets
```

#### 第三步：配置 application.yml

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qinhu_oasis?useUnicode=true&characterEncoding=utf8
    username: root
    password: 123456          # ← 修改为你的 MySQL 密码

  data:
    redis:
      host: localhost
      port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: root
  secret-key: "12345678"
```

> **局域网联调配置**：将上述 `localhost` 改为局域网 IP，例如 `10.220.119.171`；前端 `FRONTEND_WIKI.md` 中 BaseURL 也指向同一地址。

#### 第四步：编译启动

```bash
cd QinhuOasisServiceHub-backend

# 方式一：Maven 直接运行
mvn spring-boot:run

# 方式二：打包后运行（推荐局域网部署）
mvn clean package -DskipTests
java -jar target/qinhu-oasis-backend-1.0.0-SNAPSHOT.jar

# 验证启动成功（应返回 200 + 餐厅列表）
curl http://localhost:8080/api/restaurants
```

**初始管理员账号（已内置于 SQL）：**

```
用户名：admin
密码：Admin@123456
角色：管理员（role=2）
```

---

### 前端启动（uniapp）

#### 第一步：配置局域网 IP

打开 `QinhuOasisServiceHub-frontend/utils/request.js`，修改：

```javascript
// utils/request.js
const BASE_URL = 'http://10.220.119.171:8080/api'  // ← 替换为后端所在机器的局域网 IP
```

> 手机真机调试时，确保手机与电脑处于**同一 WiFi 网络**，且后端防火墙已放行 8080 端口。

#### 第二步：HBuilderX 运行

```
HBuilderX → 运行 → 运行到浏览器 / 运行到手机（微信开发者工具 / Android 真机）
```

---

## 🔐 安全设计

```
JWT（jjwt 0.12.5）
├── 算法：HS256，Secret ≥ 256 bits
├── Payload：userId + role（无敏感字段）
├── 有效期：7 天（604800 秒，可配置）
└── 刷新：过期后重新登录，无 Refresh Token（简化方案）

AuthInterceptor（HandlerInterceptor）
├── 解析 Authorization: Bearer {token}
├── 写入 LoginUser ThreadLocal（userId + role）
├── Token 缺失 → 匿名访问（由业务层控制权限）
├── Token 过期 → 抛 TOKEN_EXPIRED（1006）
└── afterCompletion → LoginUser.clear()（防内存泄漏）

密码存储：Hutool BCrypt（gensalt rounds=12）
```

---

## 📡 接口总览

完整 API 文档请参阅 👉 **[FRONTEND_WIKI.md](./FRONTEND_WIKI.md)**

| 模块 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 认证 | POST | `/api/auth/register` `/api/auth/login` | 无 |
| 用户 | GET | `/api/users/me` | 需登录 |
| 餐厅 | GET | `/api/restaurants` `/api/restaurants/rank` `/api/restaurants/{id}` | 无 |
| 车位 | GET/POST | `/api/parking/spaces` `/api/parking/orders` | 预约需登录 |
| 攻略 | GET/POST | `/api/posts` `/api/posts/{id}` `/api/posts/{id}/like` | 发布/点赞需登录 |
| 评论 | GET/POST | `/api/comments` | 发评论需登录 |
| 文件 | POST | `/api/files/upload` | 需登录 |
| 译员 | GET/POST | `/api/interpreters/**` `/api/interpreter/**` | 申请需登录 |
| 译员订单 | POST/GET | `/api/interpreter-orders/**` | 需登录 |
| 投诉建议 | POST | `/api/feedback` | 无（匿名支持）|
| 管理员 | GET/POST | `/api/admin/**` | 需 role=2 |

---

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| Java 源文件 | 94 个 |
| MyBatis XML | 10 个 |
| Java 代码行数 | 5,183 行 |
| 业务域数量 | 7 个（common + 6 业务模块）|
| REST API 端点 | 22 个 |
| 数据库表 | 9 张 |
| i18n 语言 | 2 种（zh-CN / en-US）|
| MinIO 存储桶 | 4 个 |

---

## 🛠️ 技术选型说明

| 选型 | 理由 |
|------|------|
| **Spring Boot 3.2.5** | Jakarta EE 10，原生支持 Java 17 特性（Records、Sealed Class 友好），长期维护 |
| **MyBatis XML 模式** | 复杂 JOIN 查询 SQL 可读性高，易于 DBA 审查与调优；拒绝 JPA 魔法查询导致的 N+1 |
| **Hutool BCrypt** | 无需引入 Spring Security，轻量实现密码安全存储；`gensalt()` 随机盐防彩虹表攻击 |
| **jjwt 0.12.5** | 业界标准 JWT 库，API 简洁；无状态 Token 天然适合移动端跨域认证 |
| **Redis ZSet** | 有序集合原语与排行榜语义完美契合，读写 O(log N)，支持 Top-K 实时更新 |
| **Redis Lua** | 脚本在单线程执行，是 Redis 层唯一保证原子性的正确方式，无需分布式锁开销 |
| **MinIO** | S3 兼容协议，私有部署零成本，桶级权限隔离满足证书隐私要求 |
| **uniapp + Vue3** | 一套代码同时支持小程序 + H5 + App，符合景区多端覆盖需求 |

---

## 📄 License

```
MIT License

Copyright (c) 2026 AiKiFan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**由 [Claude Sonnet 4.6](https://claude.ai) 辅助构建 · 严格遵循阿里巴巴 Java 开发手册（嵩山版）**

</div>

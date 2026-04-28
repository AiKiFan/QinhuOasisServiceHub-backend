# 沁湖驿站云服务平台 — 开发日志 (DEV_LOG)

> 项目名称：QinhuOasis Service Hub  
> 更新规则：**每完成一个大步骤后更新本文件**，记录要点、关键决策和待办事项。  
> 最后更新：2026-04-28（Step 3d + 3e 完成，后端全部完成）

---

## 项目概览

| 项目 | 说明 |
|------|------|
| 后端目录 | `QinhuOasisServiceHub-backend/` |
| 前端目录 | `QinhuOasisServiceHub-frontend/`（uniapp，尚未开始） |
| 技术栈 | Spring Boot 3.2.5 + MyBatis (XML) + MySQL 8.0 + Redis + Minio |
| 主包名 | `com.qinhu.oasis` |
| 端口 | 8080，Context Path: `/api` |
| 数据库 | `qinhu_oasis`（utf8mb4） |

---

## 完成进度总览

| 阶段 | 状态 | 完成日期 |
|------|------|----------|
| Step 1：数据库设计（DDL） | ✅ 已完成 | 2026-04-28 |
| Step 2：工程骨架与全局配置 | ✅ 已完成 | 2026-04-28 |
| Step 3a：餐厅排行 + 车位抢占核心业务 | ✅ 已完成 | 2026-04-28 |
| Step 3b：用户登录注册（sys 模块） | ✅ 已完成 | 2026-04-28 |
| Step 3c：UGC 攻略模块 | ✅ 已完成 | 2026-04-28 |
| Step 3d：译员接单模块 | ✅ 已完成 | 2026-04-28 |
| Step 3e：投诉建议模块 | ✅ 已完成 | 2026-04-28 |
| Step 4：前端 uniapp 开发 | ⏳ 待开始 | — |

---

## Step 1：数据库设计 ✅

**文件：**
- `QinhuOasisServiceHub.sql`（根目录，完整初始化脚本含测试数据）
- `QinhuOasisServiceHub-backend/src/main/resources/db/V1__init_schema.sql`（Flyway 迁移版本）

**9 张核心表：**

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `sys_user` | 用户表 | role: 0=游客 1=译员 2=管理员；locale: zh_CN/en_US |
| `interpreter_profile` | 译员档案 | status: 0=待审 1=通过 2=拒绝 3=暂停；cert_url(Minio) |
| `biz_order` | 统一订单表 | order_type: 1=翻译 2=车位；状态机 0→1→2→3/4/5→6 |
| `parking_space` | 车位区域 | available_count(MySQL镜像)；Redis key: `parking:stock:{id}` |
| `biz_restaurant` | 餐厅表 | sort_score = rating×20 + LOG10(review_count+1)×10；Redis ZSet |
| `ugc_post` | 攻略/动态 | post_type: 1=官方 2=游客攻略 3=游客动态 |
| `biz_comment` | 评价/评论 | target_type: 1=餐厅 2=攻略 3=译员订单 4=车位订单 |
| `ugc_like` | 点赞记录 | UNIQUE KEY 防重复 |
| `sys_feedback` | 投诉建议 | 状态: 0=待处理 1=处理中 2=已解决 3=已关闭 |

**初始数据：** 3 家餐厅（沁湖渔港/湖畔茶室/驿站快餐）、4 个停车区（A/B/残障/新能源）、admin 账户。

---

## Step 2：工程骨架与全局配置 ✅

**pom.xml 核心依赖（groupId: com.qinhu, artifactId: qinhu-oasis-backend, Java 17）：**
- Spring Boot 3.2.5、MyBatis 3.0.3、MySQL Connector、Redis（Lettuce）
- Minio 8.5.9、jjwt 0.12.5、hutool-all 5.8.26、Lombok、Validation

**application.yml 关键配置：**
- 数据源：`jdbc:mysql://localhost:3306/qinhu_oasis`，user=root，pwd=123456
- Redis：localhost:6379，database=0（无密码）
- Minio：http://localhost:9000，access-key=root，secret-key=12345678
- JWT：secret=`qinhu-oasis-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256`，expiration=604800s(7天)
- MyBatis：`map-underscore-to-camel-case: true`，mapper-locations: `classpath:mapper/**/*.xml`
- 第三方API占位：和风天气、高德地图、百度翻译（key均为占位符，需替换）

**已实现公共模块（`common/`）：**

| 文件 | 功能 |
|------|------|
| `common/result/Result.java` | 统一返回封装（code/message/data） |
| `common/result/ResultCode.java` | 错误码枚举（15个，含中英文i18n key） |
| `common/result/PageResult.java` | 分页结果包装 |
| `common/exception/BizException.java` | 业务异常（带ResultCode+i18n消息） |
| `common/exception/GlobalExceptionHandler.java` | 全局异常处理器 |
| `common/i18n/I18nInterceptor.java` | 读取 Accept-Language，写入 LocaleContextHolder |
| `common/i18n/LocaleContextHolder.java` | ThreadLocal Locale 持有器 |
| `common/i18n/I18nUtil.java` | 消息解析工具（支持参数替换） |
| `common/config/WebMvcConfig.java` | CORS + I18nInterceptor + AuthInterceptor 注册 |
| `common/config/RedisConfig.java` | RedisTemplate（JSON序列化）+ StringRedisTemplate |
| `common/config/MinioConfig.java` | MinioClient Bean |
| `common/security/JwtUtil.java` | JWT 生成/解析（jjwt 0.12.5） |
| `common/security/LoginUser.java` | ThreadLocal 登录用户上下文（userId/role） |
| `common/security/AuthInterceptor.java` | 解析 Bearer Token，设置 LoginUser；Token无效则抛异常 |
| `common/init/RedisDataInitializer.java` | ApplicationRunner，启动时初始化 Redis 数据 |

**i18n 消息文件：**
- `i18n/messages.properties`（默认中文）
- `i18n/messages_zh_CN.properties`
- `i18n/messages_en_US.properties`
- 覆盖错误码：通用/用户/译员/订单/车位/内容/文件 共 21 个 key

**注意：** 启动类 `QinhuOasisApplication.java` 含 `@MapperScan("com.qinhu.oasis.*.mapper")`，Mapper 接口无需加 `@Mapper`。

---

## Step 3a：核心业务实现（餐厅排行 + 车位抢占） ✅

### 餐厅热度排行（`restaurant/` 模块）

**API 端点（无需登录）：**
- `GET /api/restaurants?category=&page=1&size=10` — 列表（分类筛选+分页）
- `GET /api/restaurants/rank?top=10` — 热门排行（Redis ZSet驱动）
- `GET /api/restaurants/{id}` — 餐厅详情

**核心机制：**
- Redis ZSet Key：`restaurant:rank`，member=餐厅ID，score=sort_score
- 启动时 `RedisDataInitializer` 全量写入 ZSet
- `getTopRank()` 从 ZSet reverseRange 取 ID → 批量回查 DB → i18n displayName
- ZSet 为空时自动降级查 DB

**已创建文件：**
```
restaurant/entity/Restaurant.java
restaurant/mapper/RestaurantMapper.java
resources/mapper/restaurant/RestaurantMapper.xml
restaurant/dto/RestaurantListVO.java
restaurant/dto/RestaurantDetailVO.java    ← 继承 RestaurantListVO
restaurant/dto/RankListVO.java
restaurant/service/RestaurantService.java
restaurant/service/impl/RestaurantServiceImpl.java
restaurant/controller/RestaurantController.java
```

### 车位抢占防超卖（`tourism/` 模块）

**API 端点：**
- `GET /api/parking/spaces` — 所有停车区域 + Redis 实时库存（无需登录）
- `POST /api/parking/orders` — 预约车位（需 JWT 登录）
- `POST /api/parking/orders/{orderId}/cancel` — 取消预约（需 JWT 登录）

**防超卖 Lua 脚本：**
```lua
local stock = redis.call('GET', KEYS[1])
if stock == false then return -1 end      -- key不存在
if tonumber(stock) <= 0 then return 0 end -- 库存不足
redis.call('DECR', KEYS[1])
return 1                                  -- 扣减成功
```

**预约事务流程：**
1. Lua 脚本原子扣减 Redis（失败立即返回错误）
2. DB 写入订单（`biz_order`）
3. DB `UPDATE parking_space SET available_count-1 WHERE available_count>0`（CAS）
4. 若 DB 任意步骤失败：catch 块 INCR 补回 Redis → re-throw → `@Transactional` 回滚 DB

**已创建文件：**
```
tourism/entity/ParkingSpace.java
tourism/entity/BizOrder.java
tourism/mapper/ParkingSpaceMapper.java
tourism/mapper/BizOrderMapper.java
resources/mapper/tourism/ParkingSpaceMapper.xml
resources/mapper/tourism/BizOrderMapper.xml
tourism/dto/ParkingSpaceVO.java
tourism/dto/ParkingOrderReq.java
tourism/dto/ParkingOrderVO.java
tourism/service/ParkingService.java
tourism/service/impl/ParkingServiceImpl.java    ← 含 Snowflake 雪花算法生成订单号
tourism/controller/ParkingController.java
```

### 鉴权机制说明

- `AuthInterceptor` 对所有请求尝试解析 Bearer Token
- Token 缺失 → 匿名访问（LoginUser 不设置）
- Token 有效 → `LoginUser.set(userId, role)` 写入 ThreadLocal
- Token 过期/无效 → 抛 BizException(TOKEN_EXPIRED/TOKEN_INVALID)
- 需要登录的接口在 Controller 中检查 `LoginUser.getUserId() == null`

---

## Step 3b：用户系统（`sys/` 模块） ✅

**API 端点：**
- `POST /api/auth/register` — 用户注册（默认角色 TOURIST=0，无需登录）
- `POST /api/auth/login` — 用户名+密码登录，返回 JWT Token（无需登录）
- `GET /api/users/me` — 获取当前登录用户信息（需 JWT 登录）

**核心实现：**
- 密码加密：Hutool BCrypt（`cn.hutool.crypto.digest.BCrypt`，`hutool-all` 已含，无需额外依赖）
- 注册：校验用户名唯一 → BCrypt hashpw → INSERT，默认 role=0(TOURIST)、status=1(正常)、locale=zh_CN
- 登录：按用户名查询 → 校验 status=1 → BCrypt checkpw → JwtUtil.generateToken → updateLastLogin（记录 IP+时间）
- `/users/me`：从 `LoginUser.getUserId()` 取当前用户 ID → selectById → UserInfoVO（脱敏：不返回 password）
- IP 提取：兼容反向代理，优先 X-Forwarded-For，其次 X-Real-IP，最后 remoteAddr

**已创建文件：**
```
sys/entity/SysUser.java
sys/mapper/SysUserMapper.java
resources/mapper/sys/SysUserMapper.xml
sys/dto/RegisterReq.java      （@NotBlank/@Size/@Pattern 校验）
sys/dto/LoginReq.java
sys/dto/LoginVO.java           （token/userId/username/nickname/role/avatar/expiresIn）
sys/dto/UserInfoVO.java        （id/username/nickname/phone/email/avatar/role/locale/createTime）
sys/service/SysUserService.java
sys/service/impl/SysUserServiceImpl.java
sys/controller/AuthController.java
sys/controller/UserController.java
```

**注意：** `@MapperScan("com.qinhu.oasis.*.mapper")` 已覆盖 `sys.mapper` 包，无需额外配置。

---

## Step 3c：UGC 内容模块（`ugc/` 模块） ✅

**API 端点：**
- `GET /api/posts?type=&page=1&size=10` — 攻略/动态列表（无需登录，type: 1=官方 2=游客攻略 3=游客动态）
- `GET /api/posts/{id}` — 攻略详情（无需登录，自动 +1 浏览量）
- `POST /api/posts` — 发布攻略/动态（需登录；type=3 直接发布，type=2 进入审核）
- `POST /api/posts/{id}/like` — 点赞/取消点赞（需登录，同一接口切换，返回 `{"liked": true/false}`）
- `GET /api/comments?targetId=&targetType=&page=1&size=20` — 评论列表（无需登录）
- `POST /api/comments` — 发表评论/评价（需登录）
- `POST /api/files/upload` — 上传图片到 `qosh-ugc-images`（需登录，仅限 JPEG/PNG/WebP/GIF，最大 10MB）

**核心机制：**
- 图片列表存为 JSON 字符串，VO 响应时用 `@JsonRawValue` 直接输出 JSON 数组（前端无需二次解析）
- 点赞：DB UNIQUE KEY 防重复，`@Transactional` 保障 like 记录与计数原子性
- `PostListVO` 包含 `title`/`titleEn`/`displayTitle`，服务层根据 `LocaleContextHolder` 解析 i18n 展示标题
- 列表查询 JOIN `sys_user` 获取作者昵称/头像，避免 N+1
- `FileStorageService` 统一管理上传逻辑，路径格式：`{endpoint}/{bucket}/yyyyMMdd/{uuid}.{ext}`

**Minio Bucket 说明（需在控制台手动设置 public 读策略）：**
- `qosh-ugc-images` → 攻略图/评论图/用户头像（需 public-read 策略，否则图片 URL 无法直接访问）

**新增常量类：**
- `PostType`：OFFICIAL=1 / TOURIST=2 / DYNAMIC=3
- `PostStatus`：DRAFT=0 / PUBLISHED=1 / REVIEWING=2 / TAKEN_DOWN=3
- `CommentTargetType`：RESTAURANT=1 / POST=2 / INTERPRETER_ORDER=3 / PARKING_ORDER=4
- `LikeTargetType`：POST=1 / COMMENT=2

**已创建文件：**
```
ugc/entity/{UgcPost, BizComment, UgcLike}.java
ugc/mapper/{UgcPostMapper, BizCommentMapper, UgcLikeMapper}.java
resources/mapper/ugc/{UgcPostMapper, BizCommentMapper, UgcLikeMapper}.xml
ugc/dto/{CreatePostReq, PostListVO, PostDetailVO, CreateCommentReq, CommentVO, FileUploadVO}.java
ugc/service/{UgcPostService, BizCommentService}.java
ugc/service/impl/{UgcPostServiceImpl, BizCommentServiceImpl}.java
ugc/controller/{PostController, CommentController, FileController}.java
common/service/FileStorageService.java   ← 通用文件上传组件
common/constant/{PostType, PostStatus, CommentTargetType, LikeTargetType}.java
```

---

## Step 3d：译员服务模块（`interpreter/` 模块） ✅

**API 端点：**
- `POST /api/interpreter/cert-upload` — 上传译员资质证书到 `qosh-interpreter-certs`（需登录）
- `POST /api/interpreter/apply` — 申请成为译员（需登录；档案状态初始为 PENDING）
- `GET /api/interpreters` — 浏览已通过审核的译员列表（无需登录，按评分+接单数排序）
- `GET /api/interpreters/{id}` — 译员详情（无需登录）
- `POST /api/interpreter-orders` — 游客预约译员（需登录；指定档案 ID、服务类型、时间）
- `POST /api/interpreter-orders/{id}/accept` — 译员接单（需登录；只有被指定的译员可接）
- `POST /api/interpreter-orders/{id}/cancel` — 取消订单（需登录；游客或译员均可）
- `GET /api/interpreter-orders/mine` — 查看我的翻译订单（需登录）
- `GET /api/admin/interpreter-profiles` — 管理员查看所有译员档案（需 ADMIN 角色）
- `POST /api/admin/interpreter-profiles/{id}/review` — 管理员审核（?approve=true/false，需 ADMIN 角色）

**核心机制：**
- 审核通过 → 自动更新 `sys_user.role` 为 `STUDENT(1)`，赋予译员权限
- 订单金额自动计算：`hourlyRate × HOURS.between(startTime, endTime)`
- 译员接单与游客取消均有 `ORDER_STATUS_INVALID` 保护，避免无效状态流转
- `InterpreterOrderMapper` 独立于 `BizOrderMapper`（同一底层表 `biz_order`，通过 `order_type=1` 区分）

**新增常量类：**
- `InterpreterStatus`：PENDING=0 / APPROVED=1 / REJECTED=2 / SUSPENDED=3

**已创建文件：**
```
interpreter/entity/InterpreterProfile.java
interpreter/mapper/{InterpreterProfileMapper, InterpreterOrderMapper}.java
resources/mapper/interpreter/{InterpreterProfileMapper, InterpreterOrderMapper}.xml
interpreter/dto/{ApplyInterpreterReq, InterpreterVO, BookInterpreterReq, InterpreterOrderVO}.java
interpreter/service/InterpreterService.java
interpreter/service/impl/InterpreterServiceImpl.java
interpreter/controller/{InterpreterProfileController, InterpreterOrderController}.java
common/constant/InterpreterStatus.java
sys/mapper/SysUserMapper.java    ← 新增 updateRole(id, role) 方法
resources/mapper/sys/SysUserMapper.xml  ← 新增 updateRole SQL
```

---

## Step 3e：投诉建议模块（`feedback/` 模块） ✅

**API 端点：**
- `POST /api/feedback` — 提交投诉/建议（**支持匿名**；已登录则自动关联用户）
- `GET /api/admin/feedback?status=&feedbackType=` — 管理员分页查询（需 ADMIN 角色）
- `POST /api/admin/feedback/{id}/reply` — 管理员回复并更新状态（需 ADMIN 角色）

**核心机制：**
- 匿名支持：`userId` 来自 `LoginUser.getUserId()`，未登录时为 null，正常入库
- 图片列表存为 JSON 字符串（同 UGC 模块），VO 用 `@JsonRawValue` 输出
- 管理员回复同时更新 `status`（1=处理中/2=已解决/3=已关闭）和 `handler_id`

**新增常量类：**
- `FeedbackType`：COMPLAINT=1 / SUGGESTION=2 / INQUIRY=3 / OTHER=4
- `FeedbackStatus`：PENDING=0 / PROCESSING=1 / RESOLVED=2 / CLOSED=3

**已创建文件：**
```
feedback/entity/SysFeedback.java
feedback/mapper/SysFeedbackMapper.java
resources/mapper/feedback/SysFeedbackMapper.xml
feedback/dto/{CreateFeedbackReq, FeedbackVO, ReplyFeedbackReq}.java
feedback/service/FeedbackService.java
feedback/service/impl/FeedbackServiceImpl.java
feedback/controller/FeedbackController.java
common/constant/{FeedbackType, FeedbackStatus}.java
```

---

## 启动前检查清单

每次启动前需确认：
- [ ] MySQL 已启动，数据库 `qinhu_oasis` 已存在（执行过 `QinhuOasisServiceHub.sql`）
- [ ] Redis 已启动（localhost:6379，无密码）
- [ ] Minio 已启动（localhost:9000，access-key=root，secret-key=12345678），4 个 bucket 已创建：
  - `qosh-public-static`、`qosh-interpreter-certs`、`qosh-ugc-images`、`qosh-sys-assets`
  - `qosh-ugc-images` 需要设置 **public-read** 策略，否则图片 URL 无法直接访问
- [ ] 第三方 API key 已填写（和风天气/高德地图/百度翻译，不影响启动但影响对应功能）

---

## 关键设计决策记录

| 决策 | 选择 | 原因 |
|------|------|------|
| 架构 | 模块化单体（非微服务） | 项目规模适中，避免过度设计 |
| ORM | 原生 MyBatis XML | 明确 SQL，便于学习和调试 |
| 库存扣减 | Redis Lua 原子脚本 | 防止多线程竞争导致超卖 |
| JWT | jjwt 0.12.5，无 Spring Security | 轻量，满足需求 |
| 订单号 | hutool Snowflake 雪花算法 | 分布式唯一、有序、18位数字 |
| i18n | ThreadLocal Locale + MessageSource | 无侵入，拦截器自动设置 |
| 文件存储 | Minio | 私有化部署，存储证书/图片 |
| 排行榜 | Redis ZSet | O(log N) 插入，O(N) 范围查询 |

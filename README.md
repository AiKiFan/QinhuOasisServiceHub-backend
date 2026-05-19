# 沁湖驿站 · Qinhu Oasis Service Hub

<div align="center">

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat-square&logo=spring)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.3-F7B032?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis)
![MinIO](https://img.shields.io/badge/MinIO-8.5.9-E02F20?style=flat-square&logo=minio)
![JWT](https://img.shields.io/badge/JWT-0.12.5-000000?style=flat-square&logo=json-web-tokens)

**沁湖驿站云服务平台 · 后端服务**

*RESTful API · Spring Boot 3 · MyBatis + Redis + MinIO*

</div>

---

## 1. 项目简介

沁湖驿站后端是基于 **Spring Boot 3** 构建的 RESTful API 服务，为 H5 前端、微信小程序等多端提供统一的数据接口。

核心能力：

| 能力 | 说明 |
|------|------|
| **用户体系** | JWT 无状态认证，BCrypt 密码加密，支持游客/译员/管理员三级角色 |
| **景区导览** | 景点 CRUD + Redis ZSet 热度排行 |
| **美食排行** | 餐厅分类筛选 + Redis 热度分排序 |
| **译员预约** | 雪花算法订单号、24h 前置预约、角色升级联动 |
| **智慧停车** | Redis Lua 脚本原子扣库存、分布式锁防超卖、入场/离场结算 |
| **文件存储** | MinIO 预签名 URL + Bucket 隔离策略 |
| **国际化** | 请求头 Accept-Language 解析，i18n 消息统一返回 |
| **统一异常** | 业务异常（BizException）+ 全局异常处理器，返回结构化错误码 |

---

## 2. 核心技术栈

### 2.1 技术选型

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.2.5 |
| Java 版本 | OpenJDK | 17 |
| ORM | MyBatis Spring Boot Starter | 3.0.3 |
| 数据库 | MySQL Connector/J | 8.x |
| 缓存 | Spring Data Redis | 内置 |
| 对象存储 | MinIO Java SDK | 8.5.9 |
| 认证 | jjwt (JJWT) | 0.12.5 |
| 工具库 | Hutool | 5.8.26 |
| 构建 | Maven | 3.9+ |
| 校验 | Hibernate Validator | 内置 |

### 2.2 架构总览

```
三层架构 + 公共层（common）
┌──────────────────────────────────────────────┐
│                   Controller                  │
│   (Auth / Restaurant / Interpreter / Parking…)│
├──────────────────────────────────────────────┤
│                    Service                     │
│   (业务逻辑 + 事务管理 + 缓存读写)              │
├──────────────────────────────────────────────┤
│                    Mapper                      │
│   (MyBatis XML Mapper)                        │
├──────────────────────────────────────────────┤
│  MySQL  │  Redis  │  MinIO  │  第三方API     │
└──────────────────────────────────────────────┘
```

---

## 3. 项目目录结构

```
src/main/java/com/qinhu/oasis/
├── common/                          # 公共基础设施层
│   ├── config/
│   │   ├── MinioConfig.java        # MinIO 客户端 Bean 配置
│   │   ├── RedisConfig.java         # RedisTemplate（JSON序列化 + String）
│   │   └── WebMvcConfig.java        # CORS + 拦截器注册（I18n → Auth）
│   ├── constant/                    # 业务常量枚举
│   │   ├── CommentTargetType.java   # 评论对象类型（餐厅/攻略/译员订单/停车订单）
│   │   ├── FeedbackStatus.java      # 反馈状态（待处理/处理中/已解决/已关闭）
│   │   ├── FeedbackType.java        # 反馈类型（投诉/建议/咨询/其他）
│   │   ├── InterpreterStatus.java   # 译员状态（待审核/已通过/已拒绝）
│   │   ├── LikeTargetType.java      # 点赞对象类型（攻略/评论）
│   │   ├── OrderStatus.java         # 订单状态（待接单/已接单/进行中/已完成/已取消/退款中/已退款）
│   │   ├── OrderType.java           # 订单类型（翻译服务/停车预约）
│   │   ├── PostStatus.java          # 帖子状态（草稿/已发布/审核中/已下架）
│   │   ├── PostType.java            # 帖子类型（官方攻略/游客攻略/游客动态）
│   │   ├── SpaceStatus.java         # 停车区域状态（关闭/开放/维护中）
│   │   └── UserRole.java            # 用户角色（游客/学生译员/管理员）
│   ├── controller/
│   │   └── WeatherController.java   # 天气 API 代理（GET /api/weather/now）
│   ├── dto/
│   │   └── WeatherVO.java          # 天气数据视图对象
│   ├── exception/
│   │   ├── BizException.java        # 业务异常（携带 ResultCode + i18n 消息）
│   │   └── GlobalExceptionHandler.java  # 全局异常处理（统一响应格式）
│   ├── i18n/
│   │   ├── I18nInterceptor.java     # 语言解析拦截器（优先级：?lang= > Accept-Language > zh_CN）
│   │   ├── I18nUtil.java            # 消息解析工具（基于 MessageSource）
│   │   └── LocaleContextHolder.java # ThreadLocal<Locale> 持有器
│   ├── init/                        # 应用启动初始化
│   │   ├── AdminUserInitializer.java    # 管理员账号初始化（默认 admin/Admin@123456）
│   │   ├── MinioBucketInitializer.java  # MinIO Bucket 创建（4个桶）
│   │   └── RedisDataInitializer.java    # Redis 数据预热（车位库存 + 餐厅排行）
│   ├── result/
│   │   ├── PageResult.java          # 分页结果（total + list）
│   │   ├── Result.java              # 统一响应（code + message + data）
│   │   └── ResultCode.java          # 业务错误码枚举（100+错误码）
│   ├── security/
│   │   ├── AuthInterceptor.java     # JWT 鉴权拦截器（解析 Bearer Token）
│   │   ├── JwtUtil.java             # JWT 工具（HS256 签名/验签）
│   │   └── LoginUser.java           # ThreadLocal 用户上下文（userId + role）
│   └── service/
│       ├── FileStorageService.java  # 文件上传服务（支持 4 个桶，预签名 URL）
│       ├── WeatherService.java      # 天气服务接口
│       └── impl/
│           └── WeatherServiceImpl.java  # 和风天气 API 实现（Redis 30min 缓存）
│
├── sys/                             # 系统模块（用户/认证/收藏）
│   ├── controller/
│   │   ├── AuthController.java      # 登录 / 注册
│   │   ├── FavoriteController.java  # 收藏 CRUD
│   │   └── UserController.java      # 个人资料
│   ├── dto/                         # 请求/响应 DTO
│   ├── entity/
│   │   ├── SysUser.java             # 用户实体（BCrypt 密码、role、locale）
│   │   └── UserFavorite.java        # 收藏实体（软删除）
│   ├── mapper/                      # MyBatis Mapper
│   └── service/
│       ├── SysUserService.java
│       └── impl/
│           ├── SysUserServiceImpl.java   # JWT 生成 + BCrypt 加密
│           └── FavoriteServiceImpl.java  # 收藏软删除恢复机制
│
├── restaurant/                      # 餐厅模块
│   ├── controller/RestaurantController.java
│   ├── dto/
│   ├── entity/Restaurant.java
│   ├── mapper/RestaurantMapper.java
│   └── service/
│       └── impl/RestaurantServiceImpl.java  # Redis ZSet 排行同步
│
├── tourism/                        # 旅游模块（景点 + 停车）
│   ├── controller/
│   │   ├── ParkingController.java  # 停车 API
│   │   └── ScenicSpotController.java
│   ├── dto/
│   ├── entity/
│   │   ├── BizOrder.java           # 业务订单（雪花算法 orderNo）
│   │   ├── ParkingSpace.java       # 停车区域（availableCount 为 Redis 镜像）
│   │   ├── ParkingSpot.java        # 单个车位（状态：空闲/已占用/超时）
│   │   └── ScenicSpot.java
│   ├── mapper/
│   └── service/
│       └── impl/
│           ├── ParkingServiceImpl.java  # Redis Lua 原子扣库存 + 分布式锁
│           └── ScenicSpotServiceImpl.java
│
├── interpreter/                    # 译员模块
│   ├── controller/
│   │   ├── InterpreterOrderController.java  # 译员订单
│   │   └── InterpreterProfileController.java
│   ├── dto/
│   ├── entity/InterpreterProfile.java
│   ├── mapper/
│   └── service/
│       └── impl/InterpreterServiceImpl.java  # 角色升级 + 24h 前置校验
│
├── feedback/                       # 投诉建议模块
│   ├── controller/FeedbackController.java
│   ├── dto/
│   ├── entity/SysFeedback.java     # images 为 JSON 数组字符串
│   ├── mapper/SysFeedbackMapper.java
│   └── service/
│       └── impl/FeedbackServiceImpl.java  # 匿名提交 + 角色感知回复标签
│
└── ugc/                           # UGC 模块（游记/评论/点赞/文件上传）
    ├── controller/
    │   ├── CommentController.java
    │   ├── FileController.java     # 文件上传（需登录）
    │   └── PostController.java
    ├── dto/
    ├── entity/
    │   ├── BizComment.java        # 评论（支持晒图 JSON 数组）
    │   ├── UgcLike.java          # 点赞（联合主键去重）
    │   └── UgcPost.java          # 游记/动态
    ├── mapper/
    └── service/
        └── impl/
            ├── BizCommentServiceImpl.java  # 评分聚合计算
            └── UgcPostServiceImpl.java    # 游客攻略需审核，动态直接发布
```

---

## 4. 关键技术点解析

### 4.1 JWT 无状态认证 + ThreadLocal 用户上下文

JWT 存储用户 ID 和角色，AuthInterceptor 解析后存入 ThreadLocal，全程无 Session：

```java
// JwtUtil.java — 生成 Token（HS256）
public String generateToken(Long userId, Integer role) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
}

// AuthInterceptor.java — 解析并存入 ThreadLocal
LoginUser.set(userId, role);  // 请求结束时自动清理
```

### 4.2 Redis Lua 脚本实现车位原子扣减

避免并发超卖，使用 Redis Lua 脚本在服务端保证原子性：

```java
// ParkingServiceImpl.java
private static final String DECREMENT_STOCK_LUA =
    "if redis.call('exists', KEYS[1]) == 1 " +
    "and tonumber(redis.call('get', KEYS[1])) >= tonumber(ARGV[1]) " +
    "then redis.call('decrby', KEYS[1], ARGV[1]); return 1; " +
    "else return 0; end";

public boolean decrementStock(Long zoneId, int count) {
    String key = "parking:stock:" + zoneId;
    Object result = redisTemplate.execute(
        new DefaultRedisScript<>(DECREMENT_STOCK_LUA, Long.class),
        Collections.singletonList(key), count
    );
    return result != null && (Long) result == 1L;
}
```

### 4.3 统一异常处理 + i18n 消息国际化

所有业务异常携带国际化消息 Key，由 GlobalExceptionHandler 统一返回：

```java
// BizException.java
@Getter
public class BizException extends RuntimeException {
    private final ResultCode resultCode;
    private final String i18nMessage;  // e.g. "user.username.duplicate"
}

// GlobalExceptionHandler.java
@ExceptionHandler(BizException.class)
public Result<?> handleBiz(BizException e) {
    String msg = i18nUtil.msg(e.getI18nMessage());  // 根据当前 Locale 解析
    return Result.fail(e.getResultCode().getCode(), msg);
}

// ResultCode.java 示例
USER_USERNAME_DUPLICATE(1003, "user.username.duplicate")
```

### 4.4 MinIO 预签名 URL + 主机名自动修复

MinIO 签名时使用配置的 endpoint，生成的 URL 含 `localhost:9000`，手机直接访问失败。修复方案：

```java
// FileStorageService.java
public String uploadImage(MultipartFile file, String bucket) {
    String presignedUrl = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(bucket)
            .object(objectName)
            .expiry(7, TimeUnit.DAYS)
            .build()
    );
    // 替换 URL 主机名为当前配置的 endpoint 主机
    return replacePresignedUrlHost(presignedUrl);
}

private String replacePresignedUrlHost(String url) {
    java.net.URL original = new java.net.URL(url);
    java.net.URL target = new java.net.URL(endpoint);
    return String.format("%s://%s%s%s",
        original.getProtocol(),
        target.getHost() + (target.getPort() != -1 ? ":" + target.getPort() : ""),
        original.getPath(),
        original.getQuery() != null ? "?" + original.getQuery() : ""
    );
}
```

### 4.5 启动数据预热（ApplicationRunner）

服务启动时自动初始化 Redis 缓存，确保热点数据第一时间可用：

```java
// RedisDataInitializer.java
@Override
public void run(ApplicationArguments args) {
    parkingService.initStockToRedis();   // 车位库存镜像
    restaurantService.initRankToRedis();  // 餐厅排行 ZSet
}
```

---

## 5. 核心配置说明

### 5.1 application.yml 核心配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /api        # 所有接口统一前缀

spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 连接池大小
  data:
    redis:
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
  messages:
    basename: i18n/messages  # i18n 资源文件路径
    cache-duration: 3600s

minio:
  endpoint: http://localhost:9000  # MinIO 服务地址
  buckets:
    public-static: qosh-public-static      # 景区地图、官方攻略配图
    interpreter-certs: qosh-interpreter-certs  # 译员资质证书
    ugc-images: qosh-ugc-images              # 游客攻略图、评论晒图
    sys-assets: qosh-sys-assets              # 报表导出、日志备份

jwt:
  expiration: 604800      # Token 有效期：7 天

third-party:
  hefeng-weather:
    base-url: https://mt7dnah6du.re.qweatherapi.com/v7
  amap:
    base-url: https://restapi.amap.com/v3
  baidu-translate:
    base-url: https://fanyi-api.baidu.com/api/trans/vip/translate
```

### 5.2 MyBatis 配置

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml   # 所有模块的 XML Mapper
  configuration:
    map-underscore-to-camel-case: true          # 下划线 → 驼峰
    default-statement-timeout: 30               # SQL 超时 30s
  type-aliases-package: com.qinhu.oasis.**.entity  # 别名扫描
```

---

## 6. Redis 数据设计

| Key Pattern | 数据类型 | 说明 | TTL |
|-------------|----------|------|-----|
| `restaurant:rank` | ZSet | 餐厅热度排行（score = sortScore） | 永不过期 |
| `parking:stock:{zoneId}` | String | 停车区域可用数（MySQL 镜像） | 永不过期 |
| `parking:lock:{zoneId}` | String | 区域分布式锁（SETNX + 5s TTL） | 5s |
| `parking:lock:spot:{spotId}` | String | 车位分布式锁 | 5s |
| `weather:now:{lon},{lat}` | String | 天气数据缓存 | 30min |

---

## 7. API 一览

### 7.1 认证模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 公开 |
| POST | `/api/auth/login` | 用户登录 | 公开 |

### 7.2 用户模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/users/me` | 当前用户信息 | 登录 |
| PUT | `/api/users/me` | 更新个人资料 | 登录 |

### 7.3 餐厅模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/restaurants` | 餐厅列表（分页+分类） | 公开 |
| GET | `/api/restaurants/rank` | 热门排行 | 公开 |
| GET | `/api/restaurants/{id}` | 餐厅详情 | 公开 |
| GET | `/api/restaurants/admin/list` | 管理员列表 | 管理员 |
| POST | `/api/restaurants/admin/create` | 新增餐厅 | 管理员 |
| PUT | `/api/restaurants/admin/update` | 更新餐厅 | 管理员 |
| DELETE | `/api/restaurants/admin/{id}` | 删除餐厅 | 管理员 |

### 7.4 景点模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/scenic-spots` | 景点列表 | 公开 |
| GET | `/api/scenic-spots/{id}` | 景点详情 | 公开 |
| POST | `/api/scenic-spots/admin/create` | 新增景点 | 管理员 |
| PUT | `/api/scenic-spots/admin/update` | 更新景点 | 管理员 |
| DELETE | `/api/scenic-spots/admin/{id}` | 删除景点 | 管理员 |
| PUT | `/api/scenic-spots/admin/{id}/status` | 切换状态 | 管理员 |

### 7.5 译员模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/interpreters` | 译员列表 | 公开 |
| GET | `/api/interpreters/{id}` | 译员详情 | 公开 |
| POST | `/api/interpreter/apply` | 申请译员 | 登录 |
| POST | `/api/interpreter/cert-upload` | 上传资质证书 | 登录 |
| GET | `/api/admin/interpreter-profiles` | 管理员所有申请 | 管理员 |
| POST | `/api/admin/interpreter-profiles/{id}/review` | 审核译员 | 管理员 |

### 7.6 译员订单模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/interpreter-orders` | 预约译员 | 登录 |
| GET | `/api/interpreter-orders/mine` | 我的订单 | 登录 |
| GET | `/api/interpreter-orders/received` | 收到的订单 | 译员 |
| POST | `/api/interpreter-orders/{id}/accept` | 译员接单 | 译员 |
| POST | `/api/interpreter-orders/{id}/complete` | 完成服务 | 译员 |
| POST | `/api/interpreter-orders/{id}/cancel` | 取消订单 | 双方 |

### 7.7 停车模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/parking/spaces` | 停车区域列表 | 公开 |
| POST | `/api/parking/orders` | 预约停车（选规格） | 登录 |
| GET | `/api/parking/zones/{zoneId}/spots` | 车位实时状态 | 登录 |
| POST | `/api/parking/spots/{spotId}/book` | 选位预约（原子锁） | 登录 |
| POST | `/api/parking/spots/{spotId}/settle` | 结算离场 | 登录 |

### 7.8 UGC 模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/posts` | 游记列表 | 公开 |
| GET | `/api/posts/{id}` | 游记详情（+1浏览量） | 公开 |
| POST | `/api/posts` | 发布游记 | 登录 |
| POST | `/api/posts/{id}/like` | 点赞/取消点赞 | 登录 |
| GET | `/api/comments` | 评论列表 | 公开 |
| POST | `/api/comments` | 发表评论 | 登录 |
| POST | `/api/files/upload` | 上传图片 | 登录 |

### 7.9 投诉建议模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/feedback` | 提交反馈（支持匿名） | 公开 |
| GET | `/api/feedback/me` | 我的反馈 | 登录 |
| GET | `/api/admin/feedback` | 管理员列表 | 管理员 |
| GET | `/api/admin/feedback/{id}` | 管理员详情 | 管理员 |
| POST | `/api/admin/feedback/{id}/reply` | 管理员回复 | 管理员 |

### 7.10 收藏 & 天气

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/favorites` | 添加收藏 | 登录 |
| DELETE | `/api/favorites/{type}/{id}` | 取消收藏 | 登录 |
| GET | `/api/favorites` | 所有收藏 | 登录 |
| GET | `/api/weather/now` | 实时天气（和风API） | 公开 |

---

## 8. 统一响应格式

```json
// 成功
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}

// 失败
{
  "code": 1003,
  "message": "用户名已被占用",
  "data": null
}

// 分页
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 42,
    "list": [ ... ]
  }
}
```

---

## 9. 环境准备与快速启动

### 9.1 环境要求

| 依赖 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.9+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| MinIO | 最新版（默认 `localhost:9000`） |

### 9.2 配置步骤

**Step 1：创建数据库**

```sql
CREATE DATABASE qinhu_oasis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Step 2：配置 application-local.yml（或覆盖 application.yml）**

```yaml
# src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qinhu_oasis?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
```

**Step 3：启动 MinIO 并创建桶**

MinIO 启动后访问 `http://localhost:9001`，使用 `minioadmin/minioadmin` 登录，手动创建以下 4 个桶（或由 `MinioBucketInitializer` 自动创建）：

- `qosh-public-static`（public-read）
- `qosh-interpreter-certs`（public-read）
- `qosh-ugc-images`（public-read）
- `qosh-sys-assets`（private）

### 9.3 编译与启动

```bash
cd QinhuOasisServiceHub-backend

# 编译（跳过测试）
mvn clean package -DskipTests

# 运行
java -jar target/qinhu-oasis-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

> 服务启动时自动执行以下初始化：
> 1. `AdminUserInitializer` — 创建默认管理员 admin/Admin@123456
> 2. `MinioBucketInitializer` — 自动创建 4 个 MinIO Bucket
> 3. `RedisDataInitializer` — 预热车位库存和餐厅排行数据

### 9.4 验证服务

```bash
# 健康检查
curl http://localhost:8080/api/restaurants/rank

# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'

# 带 Token 请求
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer {your_token}"
```

---

## 10. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | Admin@123456 |

> 管理员账号在首次启动时由 `AdminUserInitializer` 自动创建，密码加密存储。

---

## 11. 项目亮点速览

- ✅ **Spring Boot 3 + JDK 17** —— 最新 LTS 技术栈
- ✅ **JWT 无状态认证** —— ThreadLocal 用户上下文，全链路无 Session
- ✅ **Redis Lua 原子操作** —— 车位超卖问题从根源杜绝
- ✅ **MinIO 预签名 URL** —— 临时访问令牌，存储地址永不暴露
- ✅ **请求级 i18n** —— Accept-Language 自动解析，返回对应语言错误信息
- ✅ **MyBatis 软删除** —— 所有数据表支持软删除，数据安全可恢复
- ✅ **统一异常处理** —— 100+ 业务错误码，结构化返回，前端零处理
- ✅ **启动数据预热** —— ApplicationRunner 保证热点数据在首次请求时已就绪
- ✅ **多 Bucket 隔离策略** —— 公开图片 / 证书 / UGC / 私有资产分类管理
- ✅ **雪花算法订单号** —— 不依赖数据库自增，高并发下唯一性有保障

---

*Made with ❤️ by AiKiFan · 沁湖驿站智慧旅游云服务平台 · 2026*

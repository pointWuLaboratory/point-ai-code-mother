# 需求

## 核心业务流程
**建立完整的应用生命周期管理体系**

用户在页面输入提示词后，系统会创建一个应用记录，然后跳转到对话页面与 AI 交互生成网站。
生成完成后，用户可以预览效果，满意后进行部署，让网站真正对外提供服务。

- 数据存储
- 权限控制
- 文件管理
- 网站部署

模仿
https://nocode.cn/

## 库表设计

``` sql
-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

```
- priority 优先级，约定 99为精选应用，这样可以展示高质量的应用，避免用户看到大量测试内容。
- 添加索引：给deployKey、appName、userId 三个经常用于查询的字段添加索引，以提高查询效率。


## 业务流程设计

由于应用的生成过程和 AI 对话是绑定的，提供一个 ChatToGenCode 的应用生成接口，调用之前开发的 AI 代码生成门面完成任务，流式返回给前端。
前端不需要区分用户是否是第一次和该应用对话，始终调用这个接口即可。需要怎么做都交给后端来判断。


## 接口测试

```bash
# 1. 用户登录
curl -X POST "http://localhost:8123/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "point",
    "userPassword": "qwertyuiop"
  }' \
  -c cookies.txt

# 2. 调用生成代码接口（流式）
curl -G "http://localhost:8123/api/app/chat/gen/code" \
  --data-urlencode "appId=403744888758390784" \
  --data-urlencode "message=做个人博客，代码不超过 20 行" \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  -b cookies.txt \
  --no-buffer

```

## 应用部署
生成的网站 需要用户直接查看
把网站的文件同步到一个web服务器上。
 
- 使用serve工具
- 通过 spring boot 接口
- 使用 nginx 映射
    - 修改 nginx 配置文件， http块中添加 serve 块，配置 root 为项目部署根目录
```bash
# 静态资源服务器 - 80 端口
server {
    listen       80;
    server_name  localhost;
    charset      utf-8;
    charset_types text/css application/javascript text/plain text/xml application/json;
    # 项目部署根目录
    root         /Users/yupi/Code/yu-ai-code-mother/tmp/code_deploy;
    
    # 处理所有请求
    location ~ ^/([^/]+)/(.*)$ {
        try_files /$1/$2 /$1/index.html =404;
    }
}

```

启动 nginx 服务，监听 80 端口
```bash
brew services start nginx
```
# 需求

## 核心业务流程
建立完整的应用生命周期管理体系

用户在页面输入提示词后，系统会创建一个应用记录，然后跳转到对话页面与 AI 交互生成网站。
生成完成后，用户可以预览效果，满意后进行部署，让网站真正对外提供服务。

- 数据存储
- 权限控制
- 文件管理
- 网站部署

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

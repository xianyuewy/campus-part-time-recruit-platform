# 🤖 校园兼职招聘平台 | AI 赋能版
> 前后端分离全栈项目，面向校园场景的兼职招聘管理系统，集成AI智能客服、AI岗位审核、实时通讯等特色功能

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![SpringBoot](https://img.shields.io/badge/SpringBoot-2.x-green.svg)]()
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)]()
[![AI](https://img.shields.io/badge/AI-Integrated-orange.svg)]()

## 📌 项目简介
本系统基于 **Spring Boot + Vue** 前后端分离架构开发，搭建**学生、企业、管理员**三端协同的校园兼职招聘平台。
覆盖岗位发布、简历投递、资质审核、在线沟通、信用评价等全业务流程，并创新接入AI能力，实现智能客服答疑、岗位内容合规自动审核，大幅降低人工运营成本，提升系统智能化水平。

## ✨ 核心功能亮点
1. **多角色权限体系**
   基于 Spring Security + JWT 实现身份认证与接口鉴权，严格区分学生、企业、管理员三大角色权限。
2. **AI 特色模块（核心亮点）**
   - 🗨️ **AI 智能客服**：全局悬浮客服窗口，对接AI对话接口，解答用户常见问题，分流人工咨询压力；
   - 🔍 **AI 岗位智能审核**：自动检测企业发布岗位的违规内容、敏感词汇，替代人工初审，提升审核效率。
3. **即时通讯**
   基于 WebSocket 实现岗位投递后的双方实时聊天，支持会话身份校验与消息主动推送。
4. **基础业务模块**
   岗位管理、简历投递、面试邀约、信用评价、文件上传、数据统计等完整业务功能。
5. **性能优化**
   引入 Redis 做热点数据缓存，解决缓存穿透、雪崩等问题，优化数据库查询效率。

## 🎬 项目演示
视频文件体积较大（451MB），建议下载至本地观看：
[完整功能演示视频]([https://github.com/xianyuewy/campus-part-time-recruit-platform/releases/download/%E7%B3%BB%E7%BB%9F%E6%BC%94%E7%A4%BA%E8%A7%86%E9%A2%91/Tencent.Video.2026.06.16.-.12.39.28.02.mp4))


## 🖥️ 技术栈
### 后端
- 核心框架：Spring Boot、Spring MVC、Spring Security
- 持久层：MyBatis-Plus、MySQL
- 中间件：Redis、WebSocket
- 工具：SpringDoc（接口文档）、JWT（令牌认证）

### 前端
- 核心框架：Vue 3、JavaScript
- UI 组件库：Element Plus
- 其他：CSS、拖拽交互组件、聊天弹窗组件

## 🚀 环境准备 & 快速启动
### 前置要求
- JDK 17
- Maven 3.6+
- MySQL 5.7 / 8.0
- Redis 5.0+
- Node.js 14+（前端运行环境）

### 后端启动步骤
1. 克隆项目到本地
```bash
git clone https://github.com/xianyuewy/campus-part-time-recruit-platform.git
cd campus-part-time-recruit-platform/col_stu_ptj_sys


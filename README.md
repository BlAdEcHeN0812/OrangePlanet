# 🍊 OrangePlanet - 智能校园个人助手

**OrangePlanet** 是一款专为大学生（特别是浙大学生，根据代码逻辑推断）设计的极简风校园个人助手。它集成了课表管理、在线学习资源、邮件处理和待办事项功能，旨在提供一个统一、流畅的校园生活数字化工作台。

---

## ✨ 核心功能

* **📅 智能课表**
* 动态显示学期课表，支持学年和学期筛选。
* 卡片式设计，不同课程自动分配配色，视觉清晰。
* 点击查看课程详细信息（地点、教师、学分、成绩等）。


* **📚 数字化书桌 (My Desk)**
* 同步在线学习平台（浙大「学在浙大」）的课程。
* **资源下载**：一键获取课件与教学资源。
* **点名监控**：实时查看数字点名、雷达点名状态。


* **✉️ 聚合邮箱**
* 专门针对校园邮箱优化，通过专用密码快速登录。
* 支持邮件列表预览及 HTML 格式内容展示。


* **📝 极简待办 (Todo List)**
* 快速记录学习任务与生活琐事。
* 支持编辑、删除及状态更新，数据持久化存储。


* **🌙 极简交互设计**
* 带有灵动气泡动画的登录背景。
* 基于 HarmonyOS Sans 字体，提供舒适的阅读体验。
* 侧边栏折叠布局，适配多种屏幕尺寸。



---

## 🛠️ 技术栈

* **前端框架**: [Vue 3](https://vuejs.org/) (Composition API)
* **UI 组件库**: [Element Plus](https://element-plus.org/)
* **样式/动画**: CSS3 + 关键帧动画 (Bubble Physics)
* **图标库**: Element Plus Icons
* **后端交互**: RESTful API (对接 `/api` 路径)

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/OrangePlanet.git
cd OrangePlanet

```

### 2. 环境准备

本项目前端采用 CDN 方式引入库文件，无需复杂的编译过程。

* **前端**：直接通过 Web 服务器（如 Nginx, Live Server）托管根目录即可。
* **后端**：需准备对应的 API 服务（项目代码中预留了 `/api/courses`, `/api/emails`, `/api/learning/*` 等接口）。

### 3. 配置 API 接口

在 `renderer.js` 中修改 `API_BASE` 常量：

```javascript
const API_BASE = 'http://你的服务器地址/api';

```

---

## 📂 文件结构

```text
├── index.html      # 主结构文件，包含登录页与应用主体
├── style.css       # 全局样式，包含气泡背景动画与极简主题定义
├── renderer.js     # 前端逻辑核心，处理数据请求、状态管理与交互
├── images/         # 存放 logo.jpg 等静态资源
└── fonts/          # 存放 HarmonyOS Sans 等自定义字体

```

---

## 🎨 界面设计理念

* **色彩**：以活力橙 (`#FF9F43`) 为主色调，象征年轻与朝气。
* **质感**：采用毛玻璃特效、柔和阴影（Soft Shadow）和渐变色卡片。
* **动效**：登录页使用 `breathe` 呼吸灯动画和 `rise` 气泡上升动效，缓解用户等待焦虑。

---


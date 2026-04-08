# 🛡️ SuBind - 全功能物品绑定与资产保护系统

![Paper](https://img.shields.io/badge/API-Paper_1.21-blue.svg?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)
![Author](https://img.shields.io/badge/Author-SuXuan__Dev-purple.svg?style=flat-square)

**SuBind** 是一款为高版本 Minecraft (1.21+) 打造的高级物品绑定与资产保护插件。它不仅提供了高度自定义的 GUI
绑定操作，更内置了强大的防丢、防爆、跨时空自动返还以及双核心离线邮件推送功能。

---

## 💎 获取成品插件 (Download & Support)

本项目采用 **核心代码完全开源** 的模式，供大家学习与交流。

如果您希望直接获取**已编译好的开箱即用版本 (.jar)**，并获得后续的**永久更新**与**一对一技术支持**，请前往我的 MineBBS
发布页进行购买支持。您的赞助是我持续开发和维护的最大动力！

👉 **[点击这里前往 MineBBS 购买 SuBind 成品](这里替换为你的MineBBS帖子链接)** 👈

---

## ✨ 核心特性 (Features)

* 🛡️ **绝对安全防护体系**：
    * **防误丢**：3秒内连按两次 `Q` 键才能丢弃绑定物品（可配置）。
    * **防损毁**：绑定物品免疫岩浆、爆炸、仙人掌破坏及自然消失（永不刷没）。
    * **防窃取**：非玩家实体（如僵尸、狐狸）无法拾取绑定物品。
* ♻️ **智能防霸占与自动归还**：他人拾取你的绑定物品后，将在指定时间后自动从其背包扣除并飞回原主人背包。
* ✉️ **双核心离线邮件推送**：
    * 无缝兼容 `SweetMail` 插件。
    * 若未安装外部邮件插件，将自动启用自带的 `mails.yml` 离线存储系统，玩家上线输入指令即可领取防丢物品。
* 💰 **深度自定义消耗**：支持 `Vault金币 + 特定材质/特定CMD模型` 双重消耗判断。
* 🌍 **区域自动绑定**：支持在特定世界（如资源区、副本区）拾取物品时强制自动绑定。

---

## ⚙️ 运行环境

* **服务端核心**：Paper 1.21 及以上衍生核心。
* **Java 版本**：Java 21
* **前置依赖**：`Vault` 及任意经济核心。
* **软依赖(可选)**：`SweetMail`

---

## 📜 命令与权限 (Commands & Permissions)

| 指令             | 权限节点           | 描述               |
|:---------------|:---------------|:-----------------|
| `/bind`        | `rebind.use`   | 打开物品绑定/解绑 GUI 面板 |
| `/bind claim`  | `rebind.claim` | 领取内置离线邮件系统中的物品   |
| `/bind help`   | `rebind.help`  | 显示插件帮助菜单         |
| `/bind reload` | `rebind.admin` | 重载所有配置文件及语言包     |
| `/bind name`   | `rebind.name`  | 开发者工具：查看手中物品精确译名 |

---

## 🛠️ 自行编译 (Build from Source)

如果您具备开发经验，可以自行克隆本仓库并编译：

```bash
git clone https://github.com/CBer-SuXuan/SuBind.git
cd SuBind
mvn clean package
```

> **注意**：自行编译版本仅供学习使用，我们不为自行编译的版本提供技术答疑与售后支持。如需商业使用与完整服务，请支持正版。

------

## 🤝 开发者定制服务 (Custom Development)

找不到完全符合服务器需求的插件？现有插件总差那么一点意思？

我是 **SuXuan_Dev**，提供专业的 Minecraft 插件私人定制服务！

如果您需要：

1. 本插件的专属二次开发（接入特殊经济、等级等）。
2. 从零量身打造全新的 Bukkit/Paper 插件（RPG玩法、定制GUI、跨服数据等）。

欢迎联系我定制！**代码规范优异，拒绝卡服与内存泄漏！**

- 🐧 **企鹅联系方式**: `1755719577` (添加时请备注：插件定制)

------

*Copyright © 2026 SuXuan_Dev. All rights reserved.*
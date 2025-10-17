<img width="1024" height="1024" alt="image" src="https://github.com/user-attachments/assets/97e075b9-f8f3-41ac-902f-6973599cb214" />

# 🤖 Multy-Bot

**Multy-Bot** is a modular, multi-platform automation system currently focused on **Discord**.  
It provides advanced moderation, configuration, and automation features, with a clear architecture designed for future integration with other platforms (X/Twitter, Instagram, etc.).

---

## 🚀 Features

### 🛡️ Moderation
- `/ban`, `/kick`, `/timeout`, `/warn` – full moderation toolkit with audit logs.  
- `/cases` – list all moderation cases in the server.  
- Auto-moderation with rules:
  - **badwords**, **links/invites**, **caps**, **mentions**
  - Actions: *delete*, *warn*, *timeout (min)*  
  - Supports **role**, **channel**, and **user exemptions**.

### 👋 Greeting System
- Automatic welcome and goodbye messages with configurable templates.
- Optional image banners and embed styling.
- Commands:
  - `/welcome set-channel <#channel>`
  - `/welcome set-message <text>`
  - `/welcome test`

### ⚙️ Configuration
- `/config set log-channel:<#channel>`
- `/config set locale:<en|es>`
- `/config show` – displays the current server configuration.

### 🧩 Auto-Roles
- Automatically assign one or more roles when users join.
- Commands:
  - `/joinroles add role:<ROLE>`
  - `/joinroles remove role:<ROLE>`
  - `/joinroles list`

### 🌐 Internationalization (i18n)
- Two localization bundles:  
  - `messages_en.properties`  
  - `messages_es.properties`
- Locale selection is stored per guild.
- Fallback to English if the key is missing.

---

## 🧱 Architecture

**Multy-Bot** is built with [Quarkus 3](https://quarkus.io/) and designed around a **hexagonal / layered architecture**:
[
multy-bot/

├── core/ # Command framework, events, context

├── features/ # Modular features (moderation, automod, greet, etc.)

├── infra/ # Logging, persistence, utilities, i18n

├── integration/ # Adapters (Discord, future integrations)

├── resources/ # i18n bundles, config, etc.

└── main/ # Entry point (Quarkus app)


Each module (feature) is self-contained, with its own commands, listeners, and configuration models.  
The architecture allows adding or disabling features independently.

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-------------|
| Language | Java 21 |
| Framework | [Quarkus 3.x](https://quarkus.io/) |
| Discord SDK | [JDA 5.x](https://github.com/discord-jda/JDA) |
| Database | MongoDB (Panache ORM) |
| Packaging | Maven |
| Logging | Quarkus logging + custom LogService |
| i18n | ResourceBundle (messages_en/es) |

---

## 🧰 Development

### 1️⃣ Prerequisites
- Java 21+
- Maven 3.9+
- (Optional) MongoDB or Atlas cluster  
  *(can be skipped for local testing with `-Dbot.gateway.enabled=false`)*

### 2️⃣ Local Run
```bash
# Run in dev mode (no Discord gateway)
mvn -DskipTests -Dbot.gateway.enabled=false quarkus:dev




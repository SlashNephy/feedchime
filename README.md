# feedchime

🔔 Simple RSS feed notifier which supports Discord Webhook

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/feedchime/Docker)](https://hub.docker.com/r/slashnephy/feedchime)
[![Docker Image Size (tag)](https://img.shields.io/docker/image-size/slashnephy/feedchime/latest)](https://hub.docker.com/r/slashnephy/feedchime)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/feedchime)](https://hub.docker.com/r/slashnephy/feedchime)
[![license](https://img.shields.io/github/license/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/pulls)

[![screenshot.png](https://raw.githubusercontent.com/SlashNephy/feedchime/master/docs/screenshot.png)](https://github.com/SlashNephy/feedchime)

## Requirements

- Java 11 or later

## Get Started

`config.yml`

```yaml
# フィードの取得間隔 (秒)
# 10 未満の値はエラーになります
interval: 3600
# 一度のチェックで通知する最大数
limit: 1
# ログレベル (OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
logLevel: 'TRACE'

# チェックするフィード定義のリスト
feeds:
    # フィード URL
  - url: 'https://www.publickey1.jp/atom.xml'
    # Discord Webhook URL
    discordWebhookUrl: 'https://discord.com/api/webhooks/xxx/xxx'
```

### Docker

There are some image tags.

- `slashnephy/feedchime:latest`  
  Automatically published every push to `master` branch.
- `slashnephy/feedchime:dev`  
  Automatically published every push to `dev` branch.
- `slashnephy/feedchime:<version>`  
  Coresponding to release tags on GitHub.

`docker-compose.yml`

```yaml
version: '3.8'

services:
  feedchime:
    container_name: feedchime
    image: slashnephy/feedchime:latest
    restart: always
    volumes:
      - ./config.yml:/app/config.yml:ro
      - data:/app/data

volumes:
  data:
    driver: local
```

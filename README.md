# feedchime

🔔 Simple RSS feed notifier which supports Discord Webhook

[![Kotlin](https://img.shields.io/badge/Kotlin-1.6-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/feedchime/Docker)](https://hub.docker.com/r/slashnephy/feedchime)
[![license](https://img.shields.io/github/license/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/feedchime)](https://github.com/SlashNephy/feedchime/pulls)

[![screenshot.png](https://raw.githubusercontent.com/SlashNephy/feedchime/master/docs/screenshot.png)](https://github.com/SlashNephy/feedchime)

## Requirements

- Java 17 or later

## Get Started

`config.yml`

```yaml
# フィードの取得間隔 (秒)
# 10 未満の値はエラーになります
interval: 3600
# 一度のチェックで通知する最大数
limit: 1
# User-Agent
# userAgent: xxx
# ログレベル (OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
logLevel: 'TRACE'

# 通知チャンネル定義のリスト
channels:
    #  Discord Webhook URL
  - discordWebhookUrl: 'https://discord.com/api/webhooks/xxx/xxx'
    # チェックするフィード定義のリスト
    feeds:
        # フィード URL
      - url: 'https://www.publickey1.jp/atom.xml'
        # Webhook のユーザ名
        name: 'PublicKey'
        # Webhook のアバター URL
        avatarUrl: 'https://www.publickey1.jp/favicon.ico'
        # フィルター定義
        filter:
          # 含めるタイトル (部分一致)
          titles:
            - 'Docker'
          # 無視するタイトル (部分一致)
          ignoreTitles:
            - '[PR]'
```

### Docker

There are some image tags.

- `ghcr.io/slashnephy/feedchime:latest`  
  Automatically published every push to `master` branch.
- `ghcr.io/slashnephy/feedchime:dev`  
  Automatically published every push to `dev` branch.
- `ghcr.io/slashnephy/feedchime:<version>`  
  Coresponding to release tags on GitHub.

`docker-compose.yml`

```yaml
version: '3.8'

services:
  feedchime:
    container_name: feedchime
    image: ghcr.io/slashnephy/feedchime:latest
    restart: always
    volumes:
      - ./config.yml:/app/config.yml:ro
      - data:/app/data

volumes:
  data:
    driver: local
```

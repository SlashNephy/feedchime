# feedchime
🔔 Simple RSS feed notifier which supports Discord Webhook

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

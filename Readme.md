# Macro Dashboard

## Personal macro trading dashboard, with :
- COT positioning, who is buying or selling on the market, for main assets
- Economic calendar, last news with their previsions, for current week
- Central banks interest rates, of countries for main Forex assets

### This is a Docker project, launch with :
```
docker compose up -d
```

## Spring Boot backend + Angular frontend

### Currently serving 3 main backend services :
- COT data of main assets of the market, who is buying or selling - from /api/cot-data
- Interest Rates of countries for main Forex assets - from /api/interest-rates
- Economical news with their planning - from /api/news-calendar

### Frontend under construction

## Sources :
1. CFTC, using Commitments reports Of Traders
2. BIS, using actual interest rates of Central Banks
3. Finnhub API, using their economic calendar

## Configuration :

### API Keys

This application requires external API keys for some services.

**Finnhub Calendar API** (required for economic calendar)

1. Create a free account on [Finnhub.io](https://www.finnhub.io)
2. Generate your API key from your profile
3. Add it to `src/main/resources/application.properties` :

```properties
finnhub.api.key=YOUR_API_KEY_HERE
```

Note: The free tier is limited to 60 request every hour. This application refreshes every 5 minutes by default.

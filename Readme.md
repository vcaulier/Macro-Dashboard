# Macro Dashboard

## Personal macro trading dashboard, with :
- COT positioning, who is buying or selling on the market, for main assets
- Economic calendar, last news with their previsions, for current week
- Central banks interest rates, of countries for main Forex assets

### This is a Docker project, launch with :
```
docker compose up -d
```
And you need API keys for external data sources (see Configuration below)

## Spring Boot backend + Angular frontend

### Currently serving 3 main backend services :
- COT data of main assets of the market, who is buying or selling - from /api/cot-data
- Interest Rates of countries for main Forex assets - from /api/interest-rates
- Economical news with their planning - from /api/news-calendar

### Angular Frontend showing Interest Rates evolution and COT positionning

## Sources :
1. CFTC, using Commitments reports Of Traders
2. BIS, using actual interest rates of Central Banks
3. Finnhub API, using their economic calendar

## Configuration :

### API Keys

This application requires external API keys for some services.

0. Copy the example environment file to .env :

```bash
   cp .env.example .env
```


**CFTC Government API** (required to see who is buying or selling on the market)

Used to fetch COT (Commitments of Traders) data from the US government's CFTC reporting platform.
Without this key, the COT positioning charts will not work.

1. Create a free account at [publicreporting.cftc.gov/signup](https://publicreporting.cftc.gov/signup)
2. Once logged in, go to [publicreporting.cftc.gov/profile/edit/developer_settings](https://publicreporting.cftc.gov/profile/edit/developer_settings)
3. Generate a new API key — you will receive a **Client ID** and a **Client Secret**
4. Set them in your `.env`:

```env
CFTC_API_KEY=your_client_id_here
CFTC_SECRET=your_client_secret_here
```

> The CFTC publishes COT reports every Friday at 3:30pm EST, covering positions as of the previous Tuesday.
> Data is therefore always 3 to 6 days behind the current date — this is expected behaviour, not a bug.


**Finnhub Calendar API** (required for economic calendar and to get interest rates evolution)

1. Create a free account on [Finnhub.io](https://www.finnhub.io)
2. Generate your API key from your profile
3. Edit `.env` and set your key
4. Launch the application:

```bash
   docker compose up -d
```

Notes:
- Finnhub free tier is limited to 60 request every minute. This application refreshes every 5 minutes by default.

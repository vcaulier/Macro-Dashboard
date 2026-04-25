# Macro Dashboard

Personal macro trading dashboard, with :
- COT positioning
- Economic calendar
- Forex sentiment

This is a Docker project, launch with :
```
docker compose up -d
```

Spring Boot backend + Angular frontend.

Currently serving 3 main backend services :
- COT data of main assets of the market, who is buying or selling - from /api/cot-data
- Interest Rates of countries for main Forex assets - from /api/interest-rates
- Economical news with their planning (currently building)

Frontend under construction

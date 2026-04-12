FROM node:21 AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist/macro-dashboard/browser /usr/share/nginx/html
EXPOSE 80

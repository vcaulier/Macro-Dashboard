export const ASSETS = ['EUR','GBP','JPY','AUD','CAD','NZD','USD', 'CHF','GOLD','SILVER','USOIL'] as const;
export type Asset = typeof ASSETS[number];

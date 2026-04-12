package com.vcaulier.macrodashboard.model;

public enum CotAsset {
    EUR("EURO FX", "forex"),
    GBP("BRITISH POUND", "forex"),
    JPY("JAPANESE YEN", "forex"),
    AUD("AUSTRALIAN DOLLAR", "forex"),
    CAD("CANADIAN DOLLAR", "forex"),
    USD("USD INDEX", "forex"),
    NZD("NZ DOLLAR", "forex"),
    CHF("SWISS FRANC", "forex"),
    GOLD("GOLD", "commodity"),
    SILVER("SILVER", "commodity"),
    USOIL("WTI-PHYSICAL", "commodity");

    private final String cftcCode;
    private final String category;

    CotAsset(String cftcCode, String category) {
        this.cftcCode = cftcCode;
        this.category = category;
    }

    public static CotAsset fromMarketName(String marketName) {
        for (CotAsset asset : values()) {
            if (marketName != null && marketName.equals(asset.cftcCode)) {
                return asset;
            }
        }
        return null;
    }

    public String getCategory() {
        return this.category;
    }

    public String getCftcCode() {
        return this.cftcCode;
    }
}

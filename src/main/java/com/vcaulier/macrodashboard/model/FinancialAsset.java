package com.vcaulier.macrodashboard.model;

/**
 * A descriptive enum of all financial assets this Spring-Boot application will provide
 */
public enum FinancialAsset {
    EUR("EURO FX", "XM", "forex"),
    GBP("BRITISH POUND", "GB", "forex"),
    JPY("JAPANESE YEN", "JP", "forex"),
    AUD("AUSTRALIAN DOLLAR", "AU", "forex"),
    CAD("CANADIAN DOLLAR", "CA", "forex"),
    USD("USD INDEX", "US", "forex"),
    NZD("NZ DOLLAR", "NZ", "forex"),
    CHF("SWISS FRANC", "CH", "forex"),
    GOLD("GOLD", null, "commodity"),
    SILVER("SILVER", null, "commodity"),
    USOIL("WTI-PHYSICAL", null, "commodity");

    private final String cftcCode;
    private final String countryCode;
    private final String category;

    /**
     * @param cftcCode Code to select this asset in CFTC JSON
     * @param countryCode Code to select linked country in interest rates API 
     * @param category Linked category of this asset
     * 
     * @return Financial asset with all necessary utility data for this application
     */
    FinancialAsset(String cftcCode, String countryCode, String category) {
        this.cftcCode = cftcCode;
        this.countryCode = countryCode;
        this.category = category;
    }

    /**
     * @param marketName CFTC Code of excepted asset
     * 
     * @return Linked financial asset of market name/code
     */
    public static FinancialAsset fromMarketName(String marketName) {
        for (FinancialAsset asset : values()) {
            if (marketName != null && marketName.equals(asset.cftcCode)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * @param countryCode Country code of expected asset
     * 
     * @return Linked financial asset of this country code
     */
    public static FinancialAsset fromCountryCode(String countryCode) {
        for (FinancialAsset asset : values()) {
            if (countryCode != null && countryCode.equals(asset.countryCode)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * @return Market category of this financial asset, is it linked to Forex, or Commodities
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * @return CFTC JSON code of this financial asset
     */
    public String getCftcCode() {
        return this.cftcCode;
    }

    /**
     * @return Linked country code of this asset
     */
    public String getCountryCode() {
        return this.countryCode;
    }
}

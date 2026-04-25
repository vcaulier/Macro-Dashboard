package com.vcaulier.macrodashboard.model;

import java.util.Set;

/**
 * A descriptive enum of all financial assets this Spring-Boot application will provide
 */
public enum FinancialAsset {
    EUR("EURO FX", "XM", 
        Set.of("EU", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "IE", "PT", "GR"), "forex"),
    GBP("BRITISH POUND", "GB", Set.of("GB"), "forex"),
    JPY("JAPANESE YEN", "JP", Set.of("JP"), "forex"),
    AUD("AUSTRALIAN DOLLAR", "AU", Set.of("AU"), "forex"),
    CAD("CANADIAN DOLLAR", "CA", Set.of("CA"), "forex"),
    USD("USD INDEX", "US", Set.of("US"), "forex"),
    NZD("NZ DOLLAR", "NZ", Set.of("NZ"), "forex"),
    CHF("SWISS FRANC", "CH", Set.of("CH"), "forex"),
    GOLD("GOLD", null, null, "commodity"),
    SILVER("SILVER", null, null, "commodity"),
    USOIL("WTI-PHYSICAL", null, null, "commodity");

    private final String cftcCode;
    private final String bisCountryCode;
    private final Set<String> newsCountryCodes;
    private final String category;

    /**
     * @param cftcCode Code to select this asset in CFTC JSON
     * @param bisCountryCode Code to select linked country in interest rates API 
     * @param newsCountryCodes Codes to select linked countries in calendar news 
     * @param category Linked category of this asset
     * 
     * @return Financial asset with all necessary utility data for this application
     */
    FinancialAsset(String cftcCode, String bisCountryCode, Set<String> newsCountryCodes, String category) {
        this.cftcCode = cftcCode;
        this.bisCountryCode = bisCountryCode;
        this.newsCountryCodes = newsCountryCodes;
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
     * @param bisCountryCode BIS country code of expected asset
     * 
     * @return Linked financial asset of this country code
     */
    public static FinancialAsset fromBisCountryCode(String bisCountryCode) {
        for (FinancialAsset asset : values()) {
            if (bisCountryCode != null && bisCountryCode.equals(asset.bisCountryCode)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * @param newsCountryCode News country code of expected asset
     * 
     * @return Linked financial asset of this country code
     */
    public static FinancialAsset fromNewsCountryCode(String newsCountryCode) {
        for (FinancialAsset asset : values()) {
            if (newsCountryCode != null && asset.getNewsCountryCode() != null 
                    && asset.getNewsCountryCode().contains(newsCountryCode)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * @param assetName Enum name of expected asset, asset short name
     * 
     * @return Linked financial asset of this short name
     */
    public static FinancialAsset fromAssetName(String assetName) {
        for (FinancialAsset asset : values()) {
            if (assetName != null && assetName.equals(asset.name())) {
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
     * @return Linked BIS country code of this asset
     */
    public String getBisCountryCode() {
        return this.bisCountryCode;
    }

    /**
     * @return Linked news country codes of this asset
     */
    public Set<String> getNewsCountryCode() {
        return this.newsCountryCodes;
    }
}

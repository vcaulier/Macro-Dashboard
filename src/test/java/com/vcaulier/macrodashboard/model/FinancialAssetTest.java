package com.vcaulier.macrodashboard.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FinancialAsset enum, mapping logic and categorization
 */
public class FinancialAssetTest {

    @Test
    public void testFromMarketNameWithValidCftcCodes() {
        // Test all Forex assets
        assertEquals(FinancialAsset.EUR, FinancialAsset.fromMarketName("EURO FX"));
        assertEquals(FinancialAsset.GBP, FinancialAsset.fromMarketName("BRITISH POUND"));
        assertEquals(FinancialAsset.JPY, FinancialAsset.fromMarketName("JAPANESE YEN"));
        assertEquals(FinancialAsset.AUD, FinancialAsset.fromMarketName("AUSTRALIAN DOLLAR"));
        assertEquals(FinancialAsset.CAD, FinancialAsset.fromMarketName("CANADIAN DOLLAR"));
        assertEquals(FinancialAsset.USD, FinancialAsset.fromMarketName("USD INDEX"));
        assertEquals(FinancialAsset.NZD, FinancialAsset.fromMarketName("NZ DOLLAR"));
        assertEquals(FinancialAsset.CHF, FinancialAsset.fromMarketName("SWISS FRANC"));

        // Test commodity assets
        assertEquals(FinancialAsset.GOLD, FinancialAsset.fromMarketName("GOLD"));
        assertEquals(FinancialAsset.SILVER, FinancialAsset.fromMarketName("SILVER"));
        assertEquals(FinancialAsset.USOIL, FinancialAsset.fromMarketName("WTI-PHYSICAL"));
    }

    @Test
    public void testFromUnknownMarketName() {
        assertNull(FinancialAsset.fromMarketName("UNKNOWN_ASSET"));
        assertNull(FinancialAsset.fromMarketName("Bitcoin"));
        assertNull(FinancialAsset.fromMarketName(""));
    }

    @Test
    public void testFromNullMarketName() {
        assertNull(FinancialAsset.fromMarketName(null));
    }

    @Test
    public void testFromValidCountryCode() {
        // Test forex assets with country codes
        assertEquals(FinancialAsset.EUR, FinancialAsset.fromBisCountryCode("XM"));
        assertEquals(FinancialAsset.GBP, FinancialAsset.fromBisCountryCode("GB"));
        assertEquals(FinancialAsset.JPY, FinancialAsset.fromBisCountryCode("JP"));
        assertEquals(FinancialAsset.AUD, FinancialAsset.fromBisCountryCode("AU"));
        assertEquals(FinancialAsset.CAD, FinancialAsset.fromBisCountryCode("CA"));
        assertEquals(FinancialAsset.USD, FinancialAsset.fromBisCountryCode("US"));
        assertEquals(FinancialAsset.NZD, FinancialAsset.fromBisCountryCode("NZ"));
        assertEquals(FinancialAsset.CHF, FinancialAsset.fromBisCountryCode("CH"));
    }

    @Test
    public void testFromUnknownCountryCode() {
        assertNull(FinancialAsset.fromBisCountryCode("XX"));
        assertNull(FinancialAsset.fromBisCountryCode("FR"));
        assertNull(FinancialAsset.fromBisCountryCode(""));
    }

    @Test
    public void testFromNullCountryCode() {
        assertNull(FinancialAsset.fromBisCountryCode(null));
    }

    @Test
    public void testGetForexCategory() {
        assertEquals("forex", FinancialAsset.EUR.getCategory());
        assertEquals("forex", FinancialAsset.GBP.getCategory());
        assertEquals("forex", FinancialAsset.JPY.getCategory());
        assertEquals("forex", FinancialAsset.AUD.getCategory());
        assertEquals("forex", FinancialAsset.CAD.getCategory());
        assertEquals("forex", FinancialAsset.USD.getCategory());
        assertEquals("forex", FinancialAsset.NZD.getCategory());
        assertEquals("forex", FinancialAsset.CHF.getCategory());
    }

    @Test
    public void testGetCommodityCategory() {
        assertEquals("commodity", FinancialAsset.GOLD.getCategory());
        assertEquals("commodity", FinancialAsset.SILVER.getCategory());
        assertEquals("commodity", FinancialAsset.USOIL.getCategory());
    }

    @Test
    public void testGetCftcCode() {
        assertEquals("EURO FX", FinancialAsset.EUR.getCftcCode());
        assertEquals("GOLD", FinancialAsset.GOLD.getCftcCode());
        assertEquals("WTI-PHYSICAL", FinancialAsset.USOIL.getCftcCode());
    }

    @Test
    public void testGetCountryCode() {
        assertEquals("XM", FinancialAsset.EUR.getBisCountryCode());
        assertEquals("US", FinancialAsset.USD.getBisCountryCode());
        assertNull(FinancialAsset.GOLD.getBisCountryCode());
        assertNull(FinancialAsset.SILVER.getBisCountryCode());
        assertNull(FinancialAsset.USOIL.getBisCountryCode());
    }
}

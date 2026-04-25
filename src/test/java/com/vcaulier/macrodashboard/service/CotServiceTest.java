package com.vcaulier.macrodashboard.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vcaulier.macrodashboard.model.CotRecord;
import com.vcaulier.macrodashboard.model.FinancialAsset;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Unit tests for CotService - testing JSON parsing and data transformation logic
 */
public class CotServiceTest {

    private CotService cotService;
    private ObjectMapper objectMapper;
    private Method parseDateMethod;
    private Method parseLongMethod;
    private Method parseAssetMethod;
    private Method parseRowMethod;

    @BeforeEach
    public void setUp() throws Exception {
        cotService = new CotService();
        objectMapper = new ObjectMapper();

        // Get private methods via reflection for unit testing
        parseDateMethod = CotService.class.getDeclaredMethod("parseDate", JsonNode.class, String.class);
        parseDateMethod.setAccessible(true);

        parseLongMethod = CotService.class.getDeclaredMethod("parseLong", JsonNode.class, String.class);
        parseLongMethod.setAccessible(true);

        parseAssetMethod = CotService.class.getDeclaredMethod("parseAsset", JsonNode.class, String.class);
        parseAssetMethod.setAccessible(true);

        parseRowMethod = CotService.class.getDeclaredMethod("parseRow", JsonNode.class);
        parseRowMethod.setAccessible(true);
    }

    @Test
    public void testParseDateFromValidFormat() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "2026-01-01T00:00:00.000"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        LocalDate result = (LocalDate) parseDateMethod.invoke(cotService, node, "report_date_as_yyyy_mm_dd");

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 1, 1), result);
    }

    @Test
    public void testParseDateFromNullField() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": null
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        LocalDate result = (LocalDate) parseDateMethod.invoke(cotService, node, "report_date_as_yyyy_mm_dd");

        assertNull(result);
    }

    @Test
    public void testParseDateFromMissingField() throws Exception {
        String json = "{}";
        JsonNode node = objectMapper.readTree(json);

        LocalDate result = (LocalDate) parseDateMethod.invoke(cotService, node, "report_date_as_yyyy_mm_dd");

        assertNull(result);
    }

    @Test
    public void testParseDateFromInvalidFormat() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "invalid_date"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        assertThrows(InvocationTargetException.class,
            () -> parseDateMethod.invoke(cotService, node, "report_date_as_yyyy_mm_dd")
        );
    }

    @Test
    public void testParseLongFromValidNumber() throws Exception {
        String json = """
            {
                "volume": "123456"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        long result = (long) parseLongMethod.invoke(cotService, node, "volume");

        assertEquals(123456L, result);
    }

    @Test
    public void testParseLongFromNullField() throws Exception {
        String json = """
            {
                "volume": null
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        long result = (long) parseLongMethod.invoke(cotService, node, "volume");

        assertEquals(0L, result);
    }

    @Test
    public void testParseLongFromMissingField() throws Exception {
        String json = "{}";
        JsonNode node = objectMapper.readTree(json);

        long result = (long) parseLongMethod.invoke(cotService, node, "volume");

        assertEquals(0L, result);
    }

    @Test
    public void testParseLongFromInvalidNumber() throws Exception {
        String json = """
            {
                "volume": "not_a_number"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        long result = (long) parseLongMethod.invoke(cotService, node, "volume");

        assertEquals(0L, result);
    }

    @Test
    public void testParseAssetFromValidCftcCode() throws Exception {
        String json = """
            {
                "contract_market_name": "EURO FX"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        FinancialAsset result = (FinancialAsset) parseAssetMethod.invoke(cotService, node, "contract_market_name");

        assertEquals(FinancialAsset.EUR, result);
    }

    @Test
    public void testParseAssetFromUnknownCftcCode() throws Exception {
        String json = """
            {
                "contract_market_name": "UNKNOWN_ASSET"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        FinancialAsset result = (FinancialAsset) parseAssetMethod.invoke(cotService, node, "contract_market_name");

        assertNull(result);
    }

    @Test
    public void testParseAssetFromNullField() throws Exception {
        String json = """
            {
                "contract_market_name": null
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        FinancialAsset result = (FinancialAsset) parseAssetMethod.invoke(cotService, node, "contract_market_name");

        assertNull(result);
    }

    @Test
    public void testParseRowFromCompleteData() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "2026-04-15T00:00:00.000",
                "contract_market_name": "EURO FX",
                "comm_positions_long_all": "250000",
                "comm_positions_short_all": "150000",
                "noncomm_positions_long_all": "180000",
                "noncomm_positions_short_all": "220000",
                "nonrept_positions_long_all": "50000",
                "nonrept_positions_short_all": "30000",
                "open_interest_all": "480000"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        CotRecord result = (CotRecord) parseRowMethod.invoke(cotService, node);

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 4, 15), result.getDate());
        assertEquals(FinancialAsset.EUR, result.getAsset());
        assertEquals("forex", result.getCategory());

        assertEquals(250000L, result.getHedgersLong());
        assertEquals(150000L, result.getHedgersShort());
        assertEquals(100000L, result.getHedgersNet()); // 250000 - 150000

        assertEquals(180000L, result.getInstitutionnalLong());
        assertEquals(220000L, result.getInstitutionnalShort());
        assertEquals(-40000L, result.getInstitutionnalNet()); // 180000 - 220000

        assertEquals(50000L, result.getRetailLong());
        assertEquals(30000L, result.getRetailShort());
        assertEquals(20000L, result.getRetailNet()); // 50000 - 30000

        assertEquals(480000L, result.getOpenInterest());
    }

    @Test
    public void testParseRowWithNullValues() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "2026-01-01T00:00:00.000",
                "contract_market_name": "GOLD",
                "comm_positions_long_all": null,
                "comm_positions_short_all": "150000",
                "noncomm_positions_long_all": "180000",
                "noncomm_positions_short_all": null,
                "nonrept_positions_long_all": "50000",
                "nonrept_positions_short_all": "30000",
                "open_interest_all": null
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        CotRecord result = (CotRecord) parseRowMethod.invoke(cotService, node);

        assertNotNull(result);
        assertEquals(0L, result.getHedgersLong()); // null -> 0L
        assertEquals(150000L, result.getHedgersShort());
        assertEquals(-150000L, result.getHedgersNet()); // 0 - 150000

        assertEquals(180000L, result.getInstitutionnalLong());
        assertEquals(0L, result.getInstitutionnalShort()); // null -> 0L
        assertEquals(180000L, result.getInstitutionnalNet()); // 180000 - 0

        assertEquals(0L, result.getOpenInterest()); // null -> 0L
    }

    @Test
    public void testNetCalculationsOfParseRow() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "2026-01-01T00:00:00.000",
                "contract_market_name": "USD INDEX",
                "comm_positions_long_all": "1000",
                "comm_positions_short_all": "2000",
                "noncomm_positions_long_all": "5000",
                "noncomm_positions_short_all": "3000",
                "nonrept_positions_long_all": "800",
                "nonrept_positions_short_all": "1200",
                "open_interest_all": "8000"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        CotRecord result = (CotRecord) parseRowMethod.invoke(cotService, node);

        assertEquals(-1000L, result.getHedgersNet()); // 1000 - 2000
        assertEquals(2000L, result.getInstitutionnalNet()); // 5000 - 3000
        assertEquals(-400L, result.getRetailNet()); // 800 - 1200
    }

    @Test
    public void testParseRowFromUnknownAsset() throws Exception {
        String json = """
            {
                "report_date_as_yyyy_mm_dd": "2026-01-01T00:00:00.000",
                "contract_market_name": "UNKNOWN_MARKET",
                "comm_positions_long_all": "100000",
                "comm_positions_short_all": "50000",
                "noncomm_positions_long_all": "80000",
                "noncomm_positions_short_all": "90000",
                "nonrept_positions_long_all": "30000",
                "nonrept_positions_short_all": "20000",
                "open_interest_all": "200000"
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        CotRecord result = (CotRecord) parseRowMethod.invoke(cotService, node);

        assertNull(result.getAsset());
        assertNull(result.getCategory());
    }
}

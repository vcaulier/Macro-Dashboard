package com.vcaulier.macrodashboard.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vcaulier.macrodashboard.model.CotRecord;
import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.service.CotService;
import com.vcaulier.macrodashboard.service.InterestRateService;

/**
 * Unit tests for MacroDashboardController
 */
@ExtendWith(MockitoExtension.class)
public class MacroDashboardControllerTest {

    @Mock
    private CotService cotService;

    @Mock
    private InterestRateService interestRateService;

    @InjectMocks
    private MacroDashboardController controller;

    @Test
    public void testGetCotData() {
        // Arrange
        LinkedList<CotRecord> mockRecords = new LinkedList<>();
        mockRecords.add(new CotRecord(
            LocalDate.of(2026, 4, 15),
            FinancialAsset.EUR,
            "forex",
            250000L, 150000L, 100000L,
            180000L, 220000L, -40000L,
            50000L, 30000L, 20000L,
            480000L
        ));
        mockRecords.add(new CotRecord(
            LocalDate.of(2026, 4, 15),
            FinancialAsset.GOLD,
            "commodity",
            120000L, 80000L, 40000L,
            90000L, 100000L, -10000L,
            20000L, 15000L, 5000L,
            230000L
        ));

        when(cotService.createCotRecords()).thenReturn(mockRecords);

        // Act
        LinkedList<CotRecord> result = controller.getCotData();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first record
        CotRecord firstRecord = result.get(0);
        assertEquals(LocalDate.of(2026, 4, 15), firstRecord.getDate());
        assertEquals(FinancialAsset.EUR, firstRecord.getAsset());
        assertEquals("forex", firstRecord.getCategory());
        assertEquals(250000L, firstRecord.getHedgersLong());
        assertEquals(150000L, firstRecord.getHedgersShort());
        assertEquals(100000L, firstRecord.getHedgersNet());
        assertEquals(180000L, firstRecord.getInstitutionnalLong());
        assertEquals(220000L, firstRecord.getInstitutionnalShort());
        assertEquals(-40000L, firstRecord.getInstitutionnalNet());
        assertEquals(50000L, firstRecord.getRetailLong());
        assertEquals(30000L, firstRecord.getRetailShort());
        assertEquals(20000L, firstRecord.getRetailNet());
        assertEquals(480000L, firstRecord.getOpenInterest());

        // Verify second record
        CotRecord secondRecord = result.get(1);
        assertEquals(LocalDate.of(2026, 4, 15), secondRecord.getDate());
        assertEquals(FinancialAsset.GOLD, secondRecord.getAsset());
        assertEquals("commodity", secondRecord.getCategory());
        assertEquals(120000L, secondRecord.getHedgersLong());
        assertEquals(80000L, secondRecord.getHedgersShort());
        assertEquals(40000L, secondRecord.getHedgersNet());
        assertEquals(90000L, secondRecord.getInstitutionnalLong());
        assertEquals(100000L, secondRecord.getInstitutionnalShort());
        assertEquals( -10000L, secondRecord.getInstitutionnalNet());
        assertEquals(20000L, secondRecord.getRetailLong());
        assertEquals(15000L, secondRecord.getRetailShort());
        assertEquals(5000L, secondRecord.getRetailNet());
        assertEquals(230000L, secondRecord.getOpenInterest());

        // Verify service was called
        verify(cotService, times(1)).createCotRecords();
    }

    @Test
    public void testGetCotDataFromEmptyList() {
        // Arrange
        when(cotService.createCotRecords()).thenReturn(new LinkedList<>());

        // Act
        LinkedList<CotRecord> result = controller.getCotData();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cotService, times(1)).createCotRecords();
    }

    @Test
    public void testGetInterestRates() {
        // Arrange
        LinkedHashMap<FinancialAsset, Double> mockRates = new LinkedHashMap<>();
        mockRates.put(FinancialAsset.USD, 5.5);
        mockRates.put(FinancialAsset.GBP, 3.0);
        mockRates.put(FinancialAsset.EUR, 2.25);
        mockRates.put(FinancialAsset.AUD, -0.75);

        when(interestRateService.getInterestRates()).thenReturn(mockRates);

        // Act
        LinkedHashMap<FinancialAsset, Double> result = controller.getInterestRates();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(5.5, result.get(FinancialAsset.USD));
        assertEquals(3.0, result.get(FinancialAsset.GBP));
        assertEquals(2.25, result.get(FinancialAsset.EUR));
        assertEquals(-0.75, result.get(FinancialAsset.AUD));

        verify(interestRateService, times(1)).getInterestRates();
    }

    @Test
    public void testGetInterestRatesFromEmptyMap() {
        // Arrange
        when(interestRateService.getInterestRates()).thenReturn(new LinkedHashMap<>());

        // Act
        LinkedHashMap<FinancialAsset, Double> result = controller.getInterestRates();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(interestRateService, times(1)).getInterestRates();
    }

    @Test
    public void testGetInterestRatesPreservesOrder() {
        // Arrange - LinkedHashMap preserves insertion order
        LinkedHashMap<FinancialAsset, Double> mockRates = new LinkedHashMap<>();
        mockRates.put(FinancialAsset.USD, 5.5);
        mockRates.put(FinancialAsset.GBP, 5.0);
        mockRates.put(FinancialAsset.EUR, 4.25);
        mockRates.put(FinancialAsset.JPY, 0.1);

        when(interestRateService.getInterestRates()).thenReturn(mockRates);

        // Act
        LinkedHashMap<FinancialAsset, Double> result = controller.getInterestRates();

        // Assert - verify order is preserved
        var keys = result.keySet().stream().toList();
        assertEquals(FinancialAsset.USD, keys.get(0));
        assertEquals(FinancialAsset.GBP, keys.get(1));
        assertEquals(FinancialAsset.EUR, keys.get(2));
        assertEquals(FinancialAsset.JPY, keys.get(3));
    }

}

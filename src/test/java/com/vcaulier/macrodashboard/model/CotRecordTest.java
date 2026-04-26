package com.vcaulier.macrodashboard.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CotRecord - An @Data class test
 */
public class CotRecordTest {

    @Test
    public void testNoArgsConstructor() {
        CotRecord record = new CotRecord();

        assertNull(record.getDate());
        assertNull(record.getAsset());
        assertNull(record.getCategory());
        assertEquals(0L, record.getHedgersLong());
        assertEquals(0L, record.getHedgersShort());
        assertEquals(0L, record.getHedgersNet());
        assertEquals(0L, record.getInstitutionnalLong());
        assertEquals(0L, record.getInstitutionnalShort());
        assertEquals(0L, record.getInstitutionnalNet());
        assertEquals(0L, record.getRetailLong());
        assertEquals(0L, record.getRetailShort());
        assertEquals(0L, record.getRetailNet());
        assertEquals(0L, record.getOpenInterest());
    }

    @Test
    public void testAllArgsConstructor() {
        LocalDate date = LocalDate.of(2026, 4, 15);

        CotRecord record = new CotRecord(
            date,
            FinancialAsset.EUR,
            "forex",
            250000L,  // hedgersLong
            150000L,  // hedgersShort
            100000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        assertEquals(date, record.getDate());
        assertEquals(FinancialAsset.EUR, record.getAsset());
        assertEquals("forex", record.getCategory());
        assertEquals(250000L, record.getHedgersLong());
        assertEquals(150000L, record.getHedgersShort());
        assertEquals(100000L, record.getHedgersNet());
        assertEquals(180000L, record.getInstitutionnalLong());
        assertEquals(220000L, record.getInstitutionnalShort());
        assertEquals(-40000L, record.getInstitutionnalNet());
        assertEquals(50000L, record.getRetailLong());
        assertEquals(30000L, record.getRetailShort());
        assertEquals(20000L, record.getRetailNet());
        assertEquals(480000L, record.getOpenInterest());
    }

    @Test
    public void testSettersAndGetters() {
        CotRecord record = new CotRecord();
        LocalDate date = LocalDate.of(2026, 4, 20);

        record.setDate(date);
        record.setAsset(FinancialAsset.GOLD);
        record.setCategory("commodity");
        record.setHedgersLong(300000L);
        record.setHedgersShort(250000L);
        record.setHedgersNet(50000L);
        record.setInstitutionnalLong(200000L);
        record.setInstitutionnalShort(210000L);
        record.setInstitutionnalNet(-10000L);
        record.setRetailLong(80000L);
        record.setRetailShort(70000L);
        record.setRetailNet(10000L);
        record.setOpenInterest(580000L);

        assertEquals(date, record.getDate());
        assertEquals(FinancialAsset.GOLD, record.getAsset());
        assertEquals("commodity", record.getCategory());
        assertEquals(300000L, record.getHedgersLong());
        assertEquals(250000L, record.getHedgersShort());
        assertEquals(50000L, record.getHedgersNet());
        assertEquals(200000L, record.getInstitutionnalLong());
        assertEquals(210000L, record.getInstitutionnalShort());
        assertEquals(-10000L, record.getInstitutionnalNet());
        assertEquals(80000L, record.getRetailLong());
        assertEquals(70000L, record.getRetailShort());
        assertEquals(10000L, record.getRetailNet());
        assertEquals(580000L, record.getOpenInterest());
    }

    @Test
    public void testEquals() {
        LocalDate date = LocalDate.of(2026, 4, 15);

        CotRecord record1 = new CotRecord(
            date,
            FinancialAsset.EUR,
            "forex",
            250000L,  // hedgersLong
            150000L,  // hedgersShort
            100000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        CotRecord record2 = new CotRecord(
            date,
            FinancialAsset.EUR,
            "forex",
            250000L,  // hedgersLong
            150000L,  // hedgersShort
            100000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        assertEquals(record1, record2);
    }

    @Test
    public void testNotEqualsByData() {
        LocalDate date = LocalDate.of(2026, 4, 15);

        CotRecord record1 = new CotRecord(
            date,
            FinancialAsset.EUR,
            "forex",
            250000L,  // hedgersLong
            150000L,  // hedgersShort
            100000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        CotRecord record2 = new CotRecord(
            date,
            FinancialAsset.USOIL,
            "commodities",
            50000L,  // hedgersLong
            10000L,  // hedgersShort
            40000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        assertNotEquals(record1, record2);
    }

    @Test
    public void testNotEqualsByClass() {
        LocalDate date = LocalDate.of(2026, 4, 15);

        CotRecord record = new CotRecord(
            date,
            FinancialAsset.EUR,
            "forex",
            250000L,  // hedgersLong
            150000L,  // hedgersShort
            100000L,  // hedgersNet
            180000L,  // institutionnalLong
            220000L,  // institutionnalShort
            -40000L,  // institutionnalNet
            50000L,   // retailLong
            30000L,   // retailShort
            20000L,   // retailNet
            480000L   // openInterest
        );

        assertNotEquals(record, "This is a test String");
        assertNotEquals(record, 123);
    }
}

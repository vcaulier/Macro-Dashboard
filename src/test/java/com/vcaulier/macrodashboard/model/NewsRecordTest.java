package com.vcaulier.macrodashboard.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NewsRecord - An @Data class test
 */
public class NewsRecordTest {


    @Test
    public void testNoArgsConstructor() {
        NewsRecord record = new NewsRecord();

        assertNull(record.getDateTime());
        assertNull(record.getAsset());
        assertNull(record.getCountryCode());
        assertNull(record.getPreviousValue());
        assertNull(record.getEstimatedValue());
        assertNull(record.getActualValue());
        assertNull(record.getEventName());
        assertNull(record.getImpact());
        assertNull(record.getUnit());
    }

    @Test
    public void testAllArgsConstructor() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 12, 15);

        NewsRecord record = new NewsRecord(
            dateTime,
            FinancialAsset.EUR,
            "FR",
            null,
            2.5,
            2.3,
            "This is a test !",
            "specific",
            "%"
        );

        assertEquals(dateTime, record.getDateTime());
        assertEquals(FinancialAsset.EUR, record.getAsset());
        assertEquals("FR", record.getCountryCode());
        assertNull(record.getPreviousValue());
        assertEquals(2.5, record.getEstimatedValue());
        assertEquals(2.3, record.getActualValue());
        assertEquals("This is a test !", record.getEventName());
        assertEquals("specific", record.getImpact());
        assertEquals("%", record.getUnit());
    }

    @Test
    public void testSettersAndGetters() {
        NewsRecord record = new NewsRecord();
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 12, 15);

        record.setDateTime(dateTime);
        record.setAsset(FinancialAsset.USD);
        record.setCountryCode("US");
        record.setEstimatedValue(-2.5);
        record.setActualValue(-2.3);
        record.setEventName("This is another test !");
        record.setImpact("specific");
        record.setUnit("");

        assertEquals(dateTime, record.getDateTime());
        assertEquals(FinancialAsset.USD, record.getAsset());
        assertEquals("US", record.getCountryCode());
        assertNull(record.getPreviousValue());
        assertEquals(-2.5, record.getEstimatedValue());
        assertEquals(-2.3, record.getActualValue());
        assertEquals("This is another test !", record.getEventName());
        assertEquals("specific", record.getImpact());
        assertEquals("", record.getUnit());
    }

    @Test
    public void testEquals() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 12, 15);

        NewsRecord record1 = new NewsRecord(
            dateTime,
            FinancialAsset.EUR,
            "FR",
            null,
            2.5,
            2.3,
            "This is a test !",
            "specific",
            "%"
        );

        NewsRecord record2 = new NewsRecord(
            dateTime,
            FinancialAsset.EUR,
            "FR",
            null,
            2.5,
            2.3,
            "This is a test !",
            "specific",
            "%"
        );

        assertEquals(record1, record2);
    }

    @Test
    public void testNotEqualsByData() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 12, 15);

        NewsRecord record1 = new NewsRecord(
            dateTime,
            FinancialAsset.EUR,
            "FR",
            null,
            2.5,
            2.3,
            "This is a test !",
            "specific",
            "%"
        );

        NewsRecord record2 = new NewsRecord(
            dateTime,
            FinancialAsset.USD,
            "FR",
            null,
            2.5,
            2.3,
            "This is another test !",
            "strange",
            ""
        );

        assertNotEquals(record1, record2);
    }

    @Test
    public void testNotEqualsByClass() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 15, 12, 15);

        NewsRecord record = new NewsRecord(
            dateTime,
            FinancialAsset.EUR,
            "FR",
            null,
            2.5,
            2.3,
            "This is a test !",
            "specific",
            "%"
        );

        assertNotEquals(record, "This is a test String");
        assertNotEquals(record, 123);
    }

}

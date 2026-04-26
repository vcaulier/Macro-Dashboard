package com.vcaulier.macrodashboard.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import org.springframework.web.client.RestTemplate;

import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.model.NewsRecord;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Unit tests for NewsCalendarService - testing JSON parsing and data transformation logic
 */
@ExtendWith(MockitoExtension.class)
public class NewsCalendarServiceTest {

    @InjectMocks
    private NewsCalendarService newsService;
    
    @Mock
    private RestTemplate restTemplate;
    
    private ObjectMapper objectMapper;
    private Method updateNewsRecords;
    private Method parseNewsRecord;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        Field restTemplateField = NewsCalendarService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(newsService, restTemplate);

        Field url = NewsCalendarService.class.getDeclaredField("NEWS_CALENDAR_BASE_URL");
        url.setAccessible(true);
        url.set(newsService, "testUrl");

        Field key = NewsCalendarService.class.getDeclaredField("FINNHUB_API_KEY");
        key.setAccessible(true);
        key.set(newsService, "test");

        updateNewsRecords = NewsCalendarService.class.getDeclaredMethod("updateNewsRecords");
        updateNewsRecords.setAccessible(true);

        parseNewsRecord = NewsCalendarService.class.getDeclaredMethod("parseNewsRecord", JsonNode.class);
        parseNewsRecord.setAccessible(true);
    }

    @Test
    public void testParseNewsFromValidFormat() throws Exception {
        String json = """
            {
                "time": "2026-01-01 12:15:00",
                "country": "US",
                "prev": -0.75,
                "estimate": null,
                "actual": null,
                "event": "This is a test !",
                "impact": "high",
                "unit": "%" 
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        NewsRecord result = (NewsRecord) parseNewsRecord.invoke(newsService, node);

        assertNotNull(result);

        assertEquals(LocalDateTime.of(2026, 01, 01, 12, 15, 00), result.getDateTime());
        assertEquals(FinancialAsset.USD, result.getAsset());
        assertEquals("US", result.getCountryCode());
        assertEquals(-0.75, result.getPreviousValue());
        assertNull(result.getEstimatedValue());
        assertNull(result.getActualValue());
        assertEquals("This is a test !", result.getEventName());
        assertEquals("high", result.getImpact());
        assertEquals("%", result.getUnit());
    }

    @Test
    public void testParseNewsFromInvalidDate() throws Exception {
        String json = """
            {
                "time": "ZZZ",
                "country": "FR",
                "prev": -0.75,
                "estimate": null,
                "actual": null,
                "event": "This is not valid !",
                "impact": "strange",
                "unit": "strange" 
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        NewsRecord result = (NewsRecord) parseNewsRecord.invoke(newsService, node);

        assertNull(result);
    }

    @Test
    public void testParseNewsFromInvalidAsset() throws Exception {
        String json = """
            {
                "time": "2026-01-01 12:15:00",
                "country": "ZZ",
                "prev": -0.75,
                "estimate": null,
                "actual": null,
                "event": "This is a test !",
                "impact": "medium",
                "unit": "" 
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        NewsRecord result = (NewsRecord) parseNewsRecord.invoke(newsService, node);

        assertNotNull(result);

        assertEquals(LocalDateTime.of(2026, 01, 01, 12, 15, 00), result.getDateTime());
        assertNull(result.getAsset());
        assertEquals("ZZ", result.getCountryCode());
        assertEquals(-0.75, result.getPreviousValue());
        assertNull(result.getEstimatedValue());
        assertNull(result.getActualValue());
        assertEquals("This is a test !", result.getEventName());
        assertEquals("medium", result.getImpact());
        assertEquals("", result.getUnit());
    }

    @Test
    public void testParseRowFromCompleteDataReturnsAllHighAndMediumNewsByDate() throws Exception {
        String json = """
            {
                "economicCalendar": [
                    {
                        "time": "3026-01-01 12:30:00",
                        "country": "US",
                        "prev": -0.75,
                        "estimate": null,
                        "actual": null,
                        "event": "This is a test !",
                        "impact": "high",
                        "unit": "%" 
                    },

                    {
                        "time": "3026-01-01 12:15:00",
                        "country": "FR",
                        "prev": 2.75,
                        "estimate": 2.3,
                        "actual": 2.1,
                        "event": "This is another test !",
                        "impact": "medium",
                        "unit": "" 
                    },

                    {
                        "time": "3026-01-01 12:25:00",
                        "country": "FR",
                        "prev": -2.75,
                        "estimate": -2.3,
                        "actual": -2.1,
                        "event": "This is something to remove !",
                        "impact": "low",
                        "unit": "" 
                    }
                ]
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject(anyString(), eq(JsonNode.class))).thenReturn(node);

        updateNewsRecords.invoke(newsService);

        LinkedList<NewsRecord> newsRecords = newsService.getNewsRecords();

        assertNotNull(newsRecords);
        assertEquals(2, newsRecords.size());
        NewsRecord record1 = newsRecords.get(0);
        NewsRecord record2 = newsRecords.get(1);

        assertNotNull(record1);

        assertEquals(LocalDateTime.of(3026, 01, 01, 12, 15, 00), record1.getDateTime());
        assertEquals(FinancialAsset.EUR, record1.getAsset());
        assertEquals("FR", record1.getCountryCode());
        assertEquals(2.75, record1.getPreviousValue());
        assertEquals(2.3, record1.getEstimatedValue());
        assertEquals(2.1, record1.getActualValue());
        assertEquals("This is another test !", record1.getEventName());
        assertEquals("medium", record1.getImpact());
        assertEquals("", record1.getUnit());

        assertNotNull(record2);

        assertEquals(LocalDateTime.of(3026, 01, 01, 12, 30, 00), record2.getDateTime());
        assertEquals(FinancialAsset.USD, record2.getAsset());
        assertEquals("US", record2.getCountryCode());
        assertEquals(-0.75, record2.getPreviousValue());
        assertNull(record2.getEstimatedValue());
        assertNull(record2.getActualValue());
        assertEquals("This is a test !", record2.getEventName());
        assertEquals("high", record2.getImpact());
        assertEquals("%", record2.getUnit());
    }

    @Test
    public void testParseRowFromCompleteDataFilterPastNews() throws Exception {
        String json = """
            {
                "economicCalendar": [
                    {
                        "time": "2026-01-01 12:30:00",
                        "country": "US",
                        "prev": -0.75,
                        "estimate": null,
                        "actual": null,
                        "event": "This is a test !",
                        "impact": "high",
                        "unit": "%" 
                    },

                    {
                        "time": "2026-01-01 12:15:00",
                        "country": "FR",
                        "prev": 2.75,
                        "estimate": 2.3,
                        "actual": 2.1,
                        "event": "This is another test !",
                        "impact": "medium",
                        "unit": "" 
                    },

                    {
                        "time": "2026-01-01 12:25:00",
                        "country": "FR",
                        "prev": -2.75,
                        "estimate": -2.3,
                        "actual": -2.1,
                        "event": "This is something to remove !",
                        "impact": "low",
                        "unit": "" 
                    }
                ]
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject(anyString(), eq(JsonNode.class))).thenReturn(node);

        updateNewsRecords.invoke(newsService);

        LinkedList<NewsRecord> newsRecords = newsService.getNewsRecords();

        assertNotNull(newsRecords);
        assertEquals(0, newsRecords.size());
    }
}

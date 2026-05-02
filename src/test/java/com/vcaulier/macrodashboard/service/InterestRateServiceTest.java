package com.vcaulier.macrodashboard.service;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.yaml.snakeyaml.util.Tuple;

import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.model.InterestRate;

/**
 * Tests unitaires pour la classe InterestRateService
 */
@ExtendWith(MockitoExtension.class)
public class InterestRateServiceTest {

    @InjectMocks
    private InterestRateService interestRateService;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    private Field INTEREST_RATES_BASE_URL;
    private Method createInterestRates;
    private Method parseRate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NewsCalendarService newsCalendarService;

    @BeforeEach
    public void setUp() throws Exception {
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();

        Field restTemplateField = InterestRateService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(interestRateService, restTemplate);

        Field newsCalendarServiceField = InterestRateService.class.getDeclaredField("newsCalendarService");
        newsCalendarServiceField.setAccessible(true);
        newsCalendarServiceField.set(interestRateService, newsCalendarService);

        parseRate = InterestRateService.class.getDeclaredMethod("parseRate", Element.class);
        parseRate.setAccessible(true);

        createInterestRates = InterestRateService.class.getDeclaredMethod("createInterestRates");
        createInterestRates.setAccessible(true);

        INTEREST_RATES_BASE_URL = InterestRateService.class.getDeclaredField("INTEREST_RATES_BASE_URL");
        INTEREST_RATES_BASE_URL.setAccessible(true);
    }

    @Test
    public void testParseRateOfSimpleUSElement() throws Exception {
        String xmlString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Series REF_AREA="US">
                <Obs OBS_VALUE="3.625"/>
                </Series>
                """;

        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        Element element = doc.getDocumentElement();


        @SuppressWarnings("unchecked")
        Tuple<FinancialAsset, Double> result = (Tuple<FinancialAsset, Double>) parseRate.invoke(interestRateService, element);

        assertNotNull(result);
        assertEquals(FinancialAsset.USD, result._1());
        assertEquals(3.625, result._2());
    }

    @Test
    public void testParseRateOfSimpleEURElement() throws Exception {
        String xmlString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Series REF_AREA="XM">
                <Obs OBS_VALUE="-0.75"/>
                </Series>
                """;

        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        Element element = doc.getDocumentElement();

        @SuppressWarnings("unchecked")
        Tuple<FinancialAsset, Double> result = (Tuple<FinancialAsset, Double>) parseRate.invoke(interestRateService, element);

        assertNotNull(result);
        assertEquals(FinancialAsset.EUR, result._1());
        assertEquals(-0.75, result._2());
    }

    @Test
    public void testParseRateFromUnknownCountryCode() throws Exception {
        String xmlString = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Series REF_AREA="ZZ">
            <Obs OBS_VALUE="5.0"/>
            </Series>
                """;

        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        Element element = doc.getDocumentElement();

        java.lang.reflect.Method method = InterestRateService.class.getDeclaredMethod("parseRate", Element.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Tuple<FinancialAsset, Double> result = (Tuple<FinancialAsset, Double>) method.invoke(interestRateService, element);

        assertNotNull(result);
        assertNull(result._1());
        assertEquals(5.0, result._2());
    }

    @Test
    public void testGetInterestRatesFromRealData() throws Exception {
        String xmlString = """
            <?xml version="1.0" encoding="UTF-8"?>
            <message:StructureSpecificData xmlns:message="http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message">
            <message:DataSet>
            <Series FREQ="M" REF_AREA="US">
            <Obs TIME_PERIOD="2026-02" OBS_VALUE="3.625" />
            </Series>
            <Series FREQ="M" REF_AREA="XM">
            <Obs TIME_PERIOD="2026-02" OBS_VALUE="2" />
            </Series>
            <Series FREQ="M" REF_AREA="JP">
            <Obs TIME_PERIOD="2026-02" OBS_VALUE="-0.75" />
            </Series>
            </message:DataSet>
            </message:StructureSpecificData>
            """;

        INTEREST_RATES_BASE_URL.set(interestRateService, "testUrl");

        when(restTemplate.getForObject(any(String.class), eq(String.class))).thenReturn(xmlString);

        createInterestRates.invoke(interestRateService);

        LinkedList<InterestRate> result = interestRateService.getInterestRates();

        assertTrue(result.stream().anyMatch(entry -> entry.getAsset().equals(FinancialAsset.USD)
                                            && 3.625 == entry.getInterestRate()));

        assertTrue(result.stream().anyMatch(entry -> entry.getAsset().equals(FinancialAsset.EUR)
                                            && 2.0 == entry.getInterestRate()));

        assertTrue(result.stream().anyMatch(entry -> entry.getAsset().equals(FinancialAsset.JPY)
                                            && -0.75 == entry.getInterestRate()));
    }
}

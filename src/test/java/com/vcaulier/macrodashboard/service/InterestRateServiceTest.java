package com.vcaulier.macrodashboard.service;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

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
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.yaml.snakeyaml.util.Tuple;

import com.vcaulier.macrodashboard.model.FinancialAsset;

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
    private Method updateInterestRates;
    private Method parseRate;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();

        Field restTemplateField = InterestRateService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(interestRateService, restTemplate);

        parseRate = InterestRateService.class.getDeclaredMethod("parseRate", Element.class);
        parseRate.setAccessible(true);

        updateInterestRates = InterestRateService.class.getDeclaredMethod("updateInterestRates");
        updateInterestRates.setAccessible(true);

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
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="3.625" />
            </Series>
            <Series FREQ="M" REF_AREA="XM">
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="2" />
            </Series>
            <Series FREQ="M" REF_AREA="JP">
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="-0.75" />
            </Series>
            </message:DataSet>
            </message:StructureSpecificData>
            """;

        INTEREST_RATES_BASE_URL.set(interestRateService, "testUrl");

        when(restTemplate.getForObject("testUrl", String.class)).thenReturn(xmlString);

        updateInterestRates.invoke(interestRateService);

        LinkedHashMap<FinancialAsset, Double> result = interestRateService.getInterestRates();

        assertTrue(result.containsKey(FinancialAsset.USD));
        assertEquals(3.625, result.get(FinancialAsset.USD));

        assertTrue(result.containsKey(FinancialAsset.EUR));
        assertEquals(2.0, result.get(FinancialAsset.EUR));

        assertTrue(result.containsKey(FinancialAsset.JPY));
        assertEquals(-0.75, result.get(FinancialAsset.JPY));
    }

    @Test
    public void testGetInterestRatesAreSorted() throws Exception {
        String xmlString = """
            <?xml version="1.0" encoding="UTF-8"?>
            <message:StructureSpecificData xmlns:message="http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message">
            <message:DataSet>
            <Series FREQ="M" REF_AREA="US">
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="1.625" />
            </Series>
            <Series FREQ="M" REF_AREA="JP">
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="-0.75" />
            </Series>
            <Series FREQ="M" REF_AREA="XM">
            <Obs TIME_PERIOD="2026-03" OBS_VALUE="2.5" />
            </Series>
            </message:DataSet>
            </message:StructureSpecificData>
            """;

        INTEREST_RATES_BASE_URL.set(interestRateService, "testUrl");

        when(restTemplate.getForObject("testUrl", String.class)).thenReturn(xmlString);

        updateInterestRates.invoke(interestRateService);

        LinkedHashMap<FinancialAsset, Double> result = interestRateService.getInterestRates();

        var keys = result.keySet().stream().toList();
        assertEquals(FinancialAsset.EUR, keys.get(0));
        assertEquals(FinancialAsset.USD, keys.get(1));
        assertEquals(FinancialAsset.JPY, keys.get(2));
    }
}

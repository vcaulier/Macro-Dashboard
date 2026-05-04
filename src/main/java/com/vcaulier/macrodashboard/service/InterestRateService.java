package com.vcaulier.macrodashboard.service;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.util.Tuple;

import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.model.InterestRate;
import com.vcaulier.macrodashboard.model.NewsRecord;

import jakarta.annotation.PostConstruct;

/**
 * Serving actual interest rates of central banks, from countries linked to main Forex assets
 */
@Service
public class InterestRateService {

    private static final Logger log = LoggerFactory.getLogger(InterestRateService.class);

    /**
     * Our XML datasource, serving actual central banks interest rates, for a lot of countries
     */
    @Value("${interest.rates.url}")
    private String INTEREST_RATES_BASE_URL;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private NewsCalendarService newsCalendarService;

    /**
     * Main data of this service, containing actual interest rates of the Forex market
     */
    private static LinkedList<InterestRate> interestRates = new LinkedList<>();

    /**
     * @return Simple getter of sorted HashMap by interest rate, of each rate by financial asset
     */
    public LinkedList<InterestRate> getInterestRates() {
        return interestRates;
    }

    /**
     * PostConstruct to init interest rates data, as it won't move until next CRON task 
     */
    @PostConstruct
    private void initInterestRates() {
        try {
            this.createInterestRates();
        } catch(Exception e) {
            log.error("Could not initialize interest rates on startup: {}.", e);
        }
    }

    private Tuple<FinancialAsset, Double> parseRate(Element element) {
        String refArea = element.getAttribute("REF_AREA");

        Element obs = (Element) element.getElementsByTagName("Obs").item(0);
        String interestRate = obs.getAttribute("OBS_VALUE");

        FinancialAsset asset = FinancialAsset.fromBisCountryCode(refArea);

        return new Tuple(asset, Double.parseDouble(interestRate));
    }

    /**
     * This method init past interest rates
     */
    private void createInterestRates() throws ParserConfigurationException {

        LocalDate startDate = LocalDate.now().minusMonths(13).withDayOfMonth(1);
        String pastYear = String.valueOf(startDate.getYear());
        String currentMonth = startDate.getMonthValue() < 10 ? 
                    "0" + String.valueOf(startDate.getMonthValue()) : String.valueOf(startDate.getMonthValue());

        String xml = restTemplate.getForObject(INTEREST_RATES_BASE_URL 
            + pastYear + "-" + currentMonth + "-" + "01", String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        LinkedHashMap<FinancialAsset, Double> rates = new LinkedHashMap<>();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDocument = builder.parse(new InputSource(new StringReader(xml)));
            NodeList nodes = xmlDocument.getElementsByTagName("Series");

            for (int i = 0; i<nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                Tuple<FinancialAsset, Double> tuple = this.parseRate(element);
                if (tuple._1() != null) {
                    rates.put(tuple._1(), tuple._2());
                }
            }
        } catch (ParserConfigurationException e) {
            throw new ParserConfigurationException("InterestRateService error : Cannot parse actual XML data source");
        } catch (NumberFormatException e) {
            throw new ParserConfigurationException("InterestRateService error : Cannot parse actual XML data source");
        } catch (NullPointerException e) {
            throw new ParserConfigurationException("InterestRateService error : Cannot parse actual XML data source");
        } catch (SAXException e) {
            throw new ParserConfigurationException("InterestRateService error : Cannot parse actual XML data source");
        } catch (IOException e) {
            throw new ParserConfigurationException("InterestRateService error : Cannot parse actual XML data source");
        }

        rates.entrySet().stream()
            .map(entry -> new InterestRate(startDate, entry.getKey(), entry.getValue()))
            .forEach(rate -> interestRates.push(rate));

        this.newsCalendarService.checkPastNewsRecords();
        
    }

    /**
     * This method will add a new interest rate record, from a news record
     * @param record A news record of an interest rate decision
     */
    public static void addNewInterestRate(NewsRecord record) {
        if (!record.getEventName().contains("Interest Rate Decision")
             || record.getActualValue() == null || record.getAsset() == null) {
            return;
        }
        InterestRate rate = new InterestRate(record.getDateTime().toLocalDate(), record.getAsset(), record.getActualValue());
        if (!interestRates.contains(rate)) {
            interestRates.push(rate);
        }
    }

}

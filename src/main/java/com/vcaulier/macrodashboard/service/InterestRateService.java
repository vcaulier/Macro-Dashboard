package com.vcaulier.macrodashboard.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.util.Tuple;

import com.vcaulier.macrodashboard.model.FinancialAsset;

import jakarta.annotation.PostConstruct;

/**
 * Serving actual interest rates of central banks, from countries linked to main Forex assets
 */
@Service
public class InterestRateService {

    /**
     * Our XML datasource, serving actual central banks interest rates, for a lot of countries
     */
    @Value("${interest.rates.url}")
    private String INTEREST_RATES_BASE_URL;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Main data of this service, containing actual interest rates of the Forex market
     */
    private LinkedHashMap<FinancialAsset, Double> interestRates;

    /**
     * @return Simple getter of sorted HashMap by interest rate, of each rate by financial asset
     */
    public LinkedHashMap<FinancialAsset, Double> getInterestRates() {
        return this.interestRates;
    }

    /**
     * PostConstruct to init interest rates data, as it won't move until next CRON task 
     */
    @PostConstruct
    private void initInterestRates() throws ParserConfigurationException {
        this.updateInterestRates();

    }

    private Tuple<FinancialAsset, Double> parseRate(Element element) {
        String refArea = element.getAttribute("REF_AREA");

        Element obs = (Element) element.getElementsByTagName("Obs").item(0);
        String interestRate = obs.getAttribute("OBS_VALUE");

        FinancialAsset asset = FinancialAsset.fromBisCountryCode(refArea);

        return new Tuple(asset, Double.parseDouble(interestRate));
    }

    /**
     * Interest rates won't move frequently, this method updates interest rates, but only once a day
     */
    @Scheduled(cron = "0 0 2 * * *")
    private void updateInterestRates() throws ParserConfigurationException {

        String xml = restTemplate.getForObject(INTEREST_RATES_BASE_URL, String.class);

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

        this.interestRates = rates.entrySet().stream()
            .sorted(Map.Entry.<FinancialAsset, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

}

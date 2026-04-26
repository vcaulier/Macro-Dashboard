package com.vcaulier.macrodashboard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.model.NewsRecord;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

@Service
public class NewsCalendarService {

    /**
     * Our JSON datasource, serving actual calendar news records
     */
    @Value("${finnhub.api.url}")
    private String NEWS_CALENDAR_BASE_URL;

    @Value("${finnhub.api.key}")
    private String FINNHUB_API_KEY;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Main data of this service, containing actual interest rates of the Forex market
     */
    private LinkedList<NewsRecord> newsCalendarRecords;

    /**
     * @return Simple getter of sorted HashMap by interest rate, of each rate by financial asset
     */
    public LinkedList<NewsRecord> getNewsRecords() {
        return this.newsCalendarRecords;
    }

    /**
     * PostConstruct to init interest rates data, as it won't move until next CRON task 
     */
    @PostConstruct
    private void initNewsRecords() throws ParserConfigurationException {
        this.updateNewsRecords();

    }

    private NewsRecord parseNewsRecord(JsonNode node) {        

        LocalDateTime dateTime = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTime = LocalDateTime.parse(node.get("time").asString(), formatter);
        } catch(DateTimeParseException e) {
            System.err.println("Error: DateTime format error while parsing record :\n" + node.toPrettyString());
            return null;
        }

        String countryCode = node.get("country").asString();
        FinancialAsset asset = FinancialAsset.fromNewsCountryCode(countryCode);

        Double previousValue = node.get("prev").isNull() ? null : node.get("prev").doubleValue();
        Double estimatedValue = node.get("estimate").isNull() ? null : node.get("estimate").doubleValue();
        Double actualValue = node.get("actual").isNull() ? null : node.get("actual").doubleValue();

        String eventName = node.get("event").asString();

        String impact = node.get("impact").asString();

        String unit = node.get("unit").asString();
        
        return new NewsRecord(dateTime, asset, countryCode, previousValue, estimatedValue, actualValue, eventName, impact, unit);
    }

    /**
     * Updating news records frequently, each 5 minutes
     */
    @Scheduled(cron = "0 0,5,10,15,20,25,30,35,40,45,50,55 * * * *")
    private void updateNewsRecords() throws ParserConfigurationException {

        LocalDate now = LocalDate.now();
        LocalDate inOneWeek = now.plusDays(7);

        ArrayNode currentWeek = restTemplate.getForObject(NEWS_CALENDAR_BASE_URL
             + "?from=" + now.toString() + "&to=" + inOneWeek.toString() + "&token=" + FINNHUB_API_KEY, 
             JsonNode.class).get("economicCalendar").asArray();
        LinkedList<NewsRecord> news = new LinkedList<>();

        for (int i = 0; i < currentWeek.size(); i++) {
            JsonNode node = (JsonNode) currentWeek.get(i);
            NewsRecord record = this.parseNewsRecord(node);
            news.add(record);
        }

        this.newsCalendarRecords = news.stream()
            .filter(record -> record != null && record.getDateTime().isAfter(now.atStartOfDay()) && record.getAsset() != null)
            .filter(record -> record.getImpact() != null 
                && (record.getImpact().equals("medium") || record.getImpact().equals("high")))
            .sorted((a,b) -> a.getDateTime().compareTo(b.getDateTime()))
            .collect(Collectors.toCollection(LinkedList::new));
    }
}

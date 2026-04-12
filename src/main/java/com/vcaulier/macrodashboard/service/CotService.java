package com.vcaulier.macrodashboard.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vcaulier.macrodashboard.model.CotRecord;

import tools.jackson.databind.JsonNode;

@Service
public class CotService {

    @Value("${cftc.api.url}")
    private String CFTC_API_URL;

    private List<JsonNode> getRawDataAsJson() {

        RestTemplate restTemplate = new RestTemplate();
        JsonNode[] nodes = restTemplate.getForObject(CFTC_API_URL, JsonNode[].class);

        List<JsonNode> result = new ArrayList<>(Arrays.asList(nodes));

        return result;

    }

    private LocalDate parseDate(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return LocalDate.parse(node.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long parseLong(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) return 0L;
        try {
            return Long.parseLong(node.asText());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private CotRecord parseRow(JsonNode row) {
        LocalDate date = parseDate(row, "report_date_as_yyyy_mm_dd");
        long commercialLong = parseLong(row, "comm_positions_long_all");
        long commercialShort = parseLong(row, "comm_positions_short_all");
        long nonCommercialLong = parseLong(row, "noncomm_positions_long_all");
        long nonCommercialShort = parseLong(row, "noncomm_positions_short_all");
        long nonReportableLong = parseLong(row, "nonrept_positions_long_all");
        long nonReportableShort = parseLong(row, "nonrept_positions_short_all");
        long openInterest = parseLong(row, "open_interest_all");

        return new CotRecord(
            date,
            commercialLong,
            commercialShort,
            commercialLong - commercialShort,
            nonCommercialLong,
            nonCommercialShort,
            nonCommercialLong - nonCommercialShort,
            nonReportableLong,
            nonReportableShort,
            nonReportableLong - nonReportableShort,
            openInterest
        );
    }

    public LinkedList<CotRecord> createCotRecords() {

        LinkedList<CotRecord> result = this.getRawDataAsJson().stream()
            .map((JsonNode row) -> parseRow(row))
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toCollection(LinkedList::new));

        return result;

    }

}

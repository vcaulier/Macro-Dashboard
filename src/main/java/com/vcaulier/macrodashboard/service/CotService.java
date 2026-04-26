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
import com.vcaulier.macrodashboard.model.FinancialAsset;

import tools.jackson.databind.JsonNode;

/**
 * Serving actual trading volumes with their directions, for main market participants
 */
@Service
public class CotService {

    /**
     * Our JSON data source, providing actual volumes, for each market participants, for main financial assets
     */
    @Value("${cftc.api.url}")
    private String CFTC_API_URL;

    /**
     * Getting raw JSON nodes from CFTC JSON url 
     * 
     * @return raw data, built from JSON nodes
     */
    private List<JsonNode> getRawDataAsJson() {

        RestTemplate restTemplate = new RestTemplate();
        JsonNode[] nodes = restTemplate.getForObject(CFTC_API_URL, JsonNode[].class);

        List<JsonNode> result = new ArrayList<>(Arrays.asList(nodes));

        return result;

    }

    /**
     * Date from JSON node parser, parsing a field inside a JSON row
     * 
     * @param row A row as a JSON node, containing a field to lookup
     * @param field Name and id of JSON field to lookup in this row 
     * @return LocalDate value of a specific JsonNode field
     */
    private LocalDate parseDate(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return LocalDate.parse(node.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Long from JSON node parser, parsing a field inside a JSON row
     * 
     * @param row A row as a JSON node, containing a field to lookup
     * @param field Name and id of JSON field to lookup in this row 
     * @return Long value of a specific JsonNode field
     */
    private long parseLong(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) return 0L;
        try {
            return Long.parseLong(node.asText());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Financial asset from JSON node parser, parsing a field inside a JSON row
     * 
     * @param row A row as a JSON node, containing a field to lookup
     * @param field Name and id of JSON field to lookup in this row 
     * @return FinancialAsset value of a specific JsonNode field
     */
    private FinancialAsset parseAsset(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return FinancialAsset.fromMarketName(node.asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * CotRecord from JSON node parser, parsing a raw JSON node
     * 
     * @param row A row as a JSON node, containing all necessary fields to lookup
     * @return CotRecord value of a specific JsonNode
     */
    private CotRecord parseRow(JsonNode row) {
        LocalDate date = parseDate(row, "report_date_as_yyyy_mm_dd");
        FinancialAsset asset = parseAsset(row, "contract_market_name");
        long commercialLong = parseLong(row, "comm_positions_long_all");
        long commercialShort = parseLong(row, "comm_positions_short_all");
        long nonCommercialLong = parseLong(row, "noncomm_positions_long_all");
        long nonCommercialShort = parseLong(row, "noncomm_positions_short_all");
        long nonReportableLong = parseLong(row, "nonrept_positions_long_all");
        long nonReportableShort = parseLong(row, "nonrept_positions_short_all");
        long openInterest = parseLong(row, "open_interest_all");

        return new CotRecord(
            date,
            asset,
            asset != null ? asset.getCategory() : null,
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

    /**
     * CotRecords creation from raw JSON data
     * 
     * @return LinkedList of CotRecord, sorted by date
     */
    public LinkedList<CotRecord> createCotRecords() {

        LinkedList<CotRecord> result = this.getRawDataAsJson().stream()
            .map((JsonNode row) -> parseRow(row))
            .filter(record -> record.getAsset() != null && record.getCategory() != null)
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toCollection(LinkedList::new));

        return result;

    }

}

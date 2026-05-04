package com.vcaulier.macrodashboard.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    @Value("${cftc.api.key.id}")
    private String API_KEY_ID;

    @Value("${cftc.api.key.secret}")
    private String API_SECRET;

    /**
     * Building a Rest Template providing higher timeouts to call CFTC service
     * 
     * @return our Rest Template to be used by CotService
     */
    private RestTemplate buildRestTemplate() {
        // Timeout 30s connect, 60s read pour absorber les lenteurs de l'API
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(30000);
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }

    /**
     * Get raw data and JsonNodes for a specific FinancialAsset
     */
    private List<JsonNode> getRawDataForAsset(FinancialAsset asset) {

        String fromDate = LocalDate.now().minusWeeks(52)
            .format(DateTimeFormatter.ISO_LOCAL_DATE);

        String body = String.format("""
            {
            "query": "SELECT * WHERE report_date_as_yyyy_mm_dd >= '%s' AND contract_market_name = '%s' ORDER BY report_date_as_yyyy_mm_dd ASC",
            "page": { "pageNumber": 1, "pageSize": 100 },
            "includeSynthetic": false
            }
            """, fromDate, asset.getCftcCode());

        String credentials = Base64.getEncoder().encodeToString(
            (API_KEY_ID + ":" + API_SECRET).getBytes(StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + credentials);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = buildRestTemplate();
        ResponseEntity<JsonNode[]> response = restTemplate.exchange(
            CFTC_API_URL,
            HttpMethod.POST,
            request,
            JsonNode[].class
        );

        return new ArrayList<>(Arrays.asList(response.getBody()));
    }

    /**
     * CotRecords creation from raw JSON data
     * 
     * @return LinkedList of CotRecord, sorted by date
     */
    public LinkedList<CotRecord> createCotRecords() {

        return Stream.of(FinancialAsset.values())
            .flatMap(asset -> {
                try {
                    return getRawDataForAsset(asset).stream()
                        .map(this::parseRow)
                        .filter(r -> r.getAsset() != null && r.getDate() != null);
                } catch (Exception e) {
                    // Un asset en erreur ne bloque pas les autres
                    return Stream.empty();
                }
            })
            .sorted(Comparator.comparing(CotRecord::getDate))
            .collect(Collectors.toCollection(LinkedList::new));
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

}

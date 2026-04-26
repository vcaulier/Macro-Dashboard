package com.vcaulier.macrodashboard.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsRecord {

    /**
     * LocalDateTime of this record creation
     */
    private LocalDateTime dateTime;

    /**
     * FinancialAsset linked to this record
     */
    private FinancialAsset asset;

    /**
     * Linked country code
     */
    private String countryCode;

    /**
     * Previous value of news behind this record
     */
    private Double previousValue;

    /**
     * Estimated value of news behind this record
     */
    private Double estimatedValue;

    /**
     * Actual value of news behind this record
     */
    private Double actualValue;

    /**
     * Event name of this news record
     */
    private String eventName;

    /**
     * Pre-supposed impact of this economic news
     */
    private String impact;

    /**
     * Unit of all values in this news record
     */
    private String unit;

}

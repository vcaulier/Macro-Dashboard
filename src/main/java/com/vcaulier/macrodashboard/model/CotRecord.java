package com.vcaulier.macrodashboard.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CotRecord {

    private LocalDate date;
    private CotAsset asset;
    private String category;
    
    private long commercialLong;
    private long commercialShort;
    private long commercialNet;
    
    private long nonCommercialLong;
    private long nonCommercialShort;
    private long nonCommercialNet;
    
    private long nonReportableLong;
    private long nonReportableShort;
    private long nonReportableNet;
    
    private long openInterest;

}

package com.vcaulier.macrodashboard.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Descriptive record of current trading volumes on a specific financial asset 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CotRecord {

    /**
     * LocalDate of this record creation
     */
    private LocalDate date;

    /**
     * FinancialAsset linked to this record
     */
    private FinancialAsset asset;

    /**
     * Market category of this Financial asset (Commodities or Forex)
     */
    private String category;
    
    /**
     * Hedgers long daily volumes, here to protect their business by hedging
     */
    private long hedgersLong;

    /**
     * Hedgers short daily volumes, here to protect their business by hedging
     */
    private long hedgersShort;

    /**
     * Hedgers net (long-short) daily volumes, here to protect their business by hedging
     */
    private long hedgersNet;
    
    /**
     * Institutionnal long traders daily volumes, hedge funds, banks, ..
     */
    private long institutionnalLong;

    /**
     * Institutionnal short traders daily volumes, hedge funds, banks, ..
     */
    private long institutionnalShort;

    /**
     * Institutionnal net (long-short) traders daily volumes, hedge funds, banks, ..
     */
    private long institutionnalNet;

    /**
     * Retail traders long daily volumes, private individuals
     */
    private long retailLong;

    /**
     * Retail traders short daily volumes, private individuals
     */
    private long retailShort;

    /**
     * Retail traders net (long-short) daily volumes, private individuals
     */
    private long retailNet;
    
    /**
     * Total number of current futures contracts for this asset
     * ( Actual volume, and not last closed day volume )
     */
    private long openInterest;

}

package com.vcaulier.macrodashboard.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Interest rate record, timed at a specific date, for a specific asset
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestRate {

    /**
     * LocalDate of this record creation
     */
    private LocalDate date;

    /**
     * FinancialAsset linked to this record
     */
    private FinancialAsset asset;

    /**
     * Value of this interest rate at this specified date
     */
    private double interestRate;
}

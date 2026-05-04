package com.vcaulier.macrodashboard.service;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vcaulier.macrodashboard.model.InterestRate;
import com.vcaulier.macrodashboard.model.NewsRecord;

/**
 * Serving actual interest rates of central banks, from countries linked to main Forex assets
 */
@Service
public class InterestRateService {

    private RestTemplate restTemplate = new RestTemplate();

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

package com.vcaulier.macrodashboard.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vcaulier.macrodashboard.model.CotRecord;
import com.vcaulier.macrodashboard.model.FinancialAsset;
import com.vcaulier.macrodashboard.model.InterestRate;
import com.vcaulier.macrodashboard.model.NewsRecord;
import com.vcaulier.macrodashboard.service.CotService;
import com.vcaulier.macrodashboard.service.InterestRateService;
import com.vcaulier.macrodashboard.service.NewsCalendarService;

/**
 * Main dashboard backend controller, serving 3 services :
 * - COT data of main assets of the market, who is buying or selling
 * - Interest Rates of countries for main Forex assets
 * - Economical news with their planning
 */
@RestController
@RequestMapping("/api")
public class MacroDashboardController {

    @Autowired
    CotService cotService;

    @Autowired
    InterestRateService interestRatesService;

    @Autowired
    NewsCalendarService newsCalendarService;

    /** 
     * Returns volume of traders, long or shorts, for main financial assets
     * @return LinkedList<CotRecord> List of trader volumes, long and short, per financial asset
     */
    @GetMapping("/cot-data")
    public LinkedList<CotRecord> getCotData() {
        return cotService.createCotRecords();
    }

    /** 
     * Return interest rates per country, sorted by rates, for main financial assets
     * @return LinkedHashMap<FinancialAsset, Double> Hashmap of interest rates linked to their financial assets
     */
    @GetMapping("/interest-rates")
    public LinkedList<InterestRate> getInterestRates() {
        return this.interestRatesService.getInterestRates().stream()
                            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                            .collect(Collectors.toCollection(LinkedList::new));
    }

    /** 
     * Return current news calendar, sorted by date, for main financial assets
     * @return LinkedList<NewsRecord> List of current news, sorted by date
     */
    @GetMapping("/news-calendar")
    public LinkedList<NewsRecord> getNewsCalendarRecords() {
        return this.newsCalendarService.getNewsRecords();
    }

}

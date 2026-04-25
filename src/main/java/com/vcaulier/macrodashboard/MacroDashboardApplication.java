package com.vcaulier.macrodashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Backend of MacroEconomics Dashboard application, serving 3 openned services :
 * - COT data of main assets of the market, who is buying or selling
 * - Interest Rates of countries for main Forex assets
 * - Economical news with their planning
 */
@EnableScheduling
@SpringBootApplication
public class MacroDashboardApplication {

	/**
	 * Main Spring-Boot application launcher
	 */
	public static void main(String[] args) {
		SpringApplication.run(MacroDashboardApplication.class, args);
	}

}

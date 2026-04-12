package com.vcaulier.macrodashboard.model;

public enum CotInstrument {
    EUR("Euro FX", "forex"),
    GBP("British Pound", "forex"),
    JPY("Japanese Yen", "forex"),
    AUD("Australian Dollar", "forex"),
    CAD("Canadian Dollar", "forex"),
    USD("United State Dollar", "forex"),
    NZD("New Zealand Dollar", "forex"),
    CHF("Swiss Franc", "forex"),
    GOLD("Gold", "commodity"),
    SILVER("Silver", "commodity"),
    CRUDE("Crude Oil", "commodity");

    private final String cftcCode;
    private final String category;

    CotInstrument(String cftcCode, String category) {
        this.cftcCode = cftcCode;
        this.category = category;
    }
}

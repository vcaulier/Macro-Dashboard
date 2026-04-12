package com.vcaulier.macrodashboard.api;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vcaulier.macrodashboard.model.CotRecord;
import com.vcaulier.macrodashboard.service.CotService;

@RestController
@RequestMapping("/api")
public class MacroDashboardController {

    @Autowired
    CotService apiService;

    @GetMapping("/cot-data")
    public LinkedList<CotRecord> getCotData() {
        return apiService.createCotRecords();
    }

}

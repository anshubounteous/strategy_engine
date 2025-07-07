package com.strategyengine.strategyengine.controller;

import com.strategyengine.strategyengine.service.ImportService;
import com.strategyengine.strategyengine.service.YahooFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final YahooFinanceService yahooFinanceService;



    @PostMapping
    public ResponseEntity<String> importIndex(@RequestBody Map<String, String> body) {
        String index = body.get("indexName");
        importService.importIndexData(index);
        return ResponseEntity.ok("Imported all companies and candles for " + index);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<String>> listCompanies(@RequestParam String index) {
        List<String> symbols = yahooFinanceService.getSymbolsForIndex(index);
        return ResponseEntity.ok(symbols);
    }

}

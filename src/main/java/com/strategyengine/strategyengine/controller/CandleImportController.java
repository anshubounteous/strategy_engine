package com.strategyengine.strategyengine.controller;


import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleImportController {

    private final CandleRepository candleRepository;

    @PostMapping("/import")
    public ResponseEntity<String> importCandles(@RequestBody List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            return ResponseEntity.badRequest().body("Candle list is empty");
        }

        candleRepository.saveAll(candles);
        return ResponseEntity.ok("Successfully imported " + candles.size() + " candles.");
    }
}

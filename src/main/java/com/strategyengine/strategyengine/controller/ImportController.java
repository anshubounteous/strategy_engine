package com.strategyengine.strategyengine.controller;

import com.strategyengine.strategyengine.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<String> importIndex(@RequestBody Map<String, String> body) {
        String index = body.get("indexName");
        importService.importIndexData(index);
        return ResponseEntity.ok("Imported all companies and candles for " + index);
    }

}

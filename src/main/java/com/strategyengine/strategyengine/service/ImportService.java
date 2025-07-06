package com.strategyengine.strategyengine.service;

import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.model.Company;
import com.strategyengine.strategyengine.repository.CandleRepository;
import com.strategyengine.strategyengine.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final YahooFinanceService yahooService;
    private final CandleRepository candleRepo;
    private final CompanyRepository companyRepo;

    public void importIndexData(String indexName) {
        List<String> symbols = yahooService.getSymbolsForIndex(indexName);

        for (String symbol : symbols) {
            if (!companyRepo.existsBySymbol(symbol)) {
                companyRepo.save(Company.builder()
                        .symbol(symbol)
                        .name(symbol)
                        .indexName(indexName)
                        .build());
            }

            List<Candle> candles = yahooService.fetchHistoricalData(symbol);
            candleRepo.saveAll(candles);
        }
    }
}

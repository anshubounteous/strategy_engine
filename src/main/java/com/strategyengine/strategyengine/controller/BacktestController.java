package com.strategyengine.strategyengine.controller;


import com.strategyengine.strategyengine.dto.BacktestResultDTO;
import com.strategyengine.strategyengine.dto.CandleDataToShowInGraphDTO;
import com.strategyengine.strategyengine.dto.StrategyRequest;
import com.strategyengine.strategyengine.dto.TradeDTO;
import com.strategyengine.strategyengine.engine.StrategyEngine;
import com.strategyengine.strategyengine.model.BacktestResult;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.model.Strategy;
import com.strategyengine.strategyengine.repository.CandleRepository;
import com.strategyengine.strategyengine.service.CandleGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final CandleRepository candleRepository;
    private final StrategyEngine strategyEngine;
    private final CandleGraphService candleGraphService;

    @PostMapping
    public BacktestResultDTO runBacktest(@RequestBody StrategyRequest request) {
        List<Candle> candles = candleRepository
                .findBySymbolAndDateBetweenOrderByDateAsc(
                        request.getSymbol(), request.getStartDate(), request.getEndDate());

        Strategy strategy = Strategy.builder()
                .name(request.getStrategyName())
                .symbol(request.getSymbol())
                .script(request.getStrategyScript())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        BacktestResult result = strategyEngine.run(strategy, candles);

        return BacktestResultDTO.builder()
                .initialEquity(result.getInitialEquity())
                .finalEquity(result.getFinalEquity())
                .totalTrades(result.getTotalTrades())
                .trades(result.getTrades().stream()
                        .map(t -> TradeDTO.builder()
                                .date(t.getDate())
                                .price(t.getPrice())
                                .action(t.getAction())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @PostMapping ("/candles")
    public CandleDataToShowInGraphDTO candleData(@RequestBody StrategyRequest request) {
        return candleGraphService.getCandleDataForChart(request);
    }

}


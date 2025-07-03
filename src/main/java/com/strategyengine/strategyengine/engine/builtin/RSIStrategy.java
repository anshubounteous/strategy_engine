package com.strategyengine.strategyengine.engine.builtin;


import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.indicator.IndicatorService;
import com.strategyengine.strategyengine.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("RSI-Based Strategy")
public class RSIStrategy implements StrategyExecutor {

    @Autowired
    private IndicatorService indicatorService;

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        Map<Integer, Double> rsiMap = indicatorService.calculateRSI(candles, 14);

        boolean holding = false;
        double capital = 10000;
        double buyPrice = 0;

        for (int i = 14; i < candles.size(); i++) {
            double rsi = rsiMap.get(i);
            Candle candle = candles.get(i);

            if (!holding && rsi < 30) {
                trades.add(Trade.builder().date(candle.getDate()).price(candle.getClose()).action("BUY").strategy(strategy).build());
                buyPrice = candle.getClose();
                holding = true;
            } else if (holding && rsi > 70) {
                trades.add(Trade.builder().date(candle.getDate()).price(candle.getClose()).action("SELL").strategy(strategy).build());
                capital = capital * (candle.getClose() / buyPrice);
                holding = false;
            }
        }

        if (holding) {
            Candle last = candles.get(candles.size() - 1);
            capital = capital * (last.getClose() / buyPrice);
        }

        return BacktestResult.builder()
                .strategy(strategy)
                .initialEquity(10000)
                .finalEquity(capital)
                .totalTrades(trades.size())
                .trades(trades)
                .build();
    }
}

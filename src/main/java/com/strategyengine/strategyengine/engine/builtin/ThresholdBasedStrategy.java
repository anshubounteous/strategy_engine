package com.strategyengine.strategyengine.engine.builtin;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("Threshold-Based Buy/Sell Strategy")
public class ThresholdBasedStrategy implements StrategyExecutor {

    static class Params {
        public double buyBelow = 100.0;
        public double sellAbove = 120.0;
    }

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;
        double capital = 10000;
        double buyPrice = 0;

        Params params = new Params();
        try {
            params = new ObjectMapper().readValue(strategy.getParamsJson(), Params.class);
        } catch (Exception ignored) {}

        for (Candle candle : candles) {
            if (!holding && candle.getClose() < params.buyBelow) {
                trades.add(Trade.builder().date(candle.getDate()).price(candle.getClose()).action("BUY").strategy(strategy).build());
                buyPrice = candle.getClose();
                holding = true;
            } else if (holding && candle.getClose() > params.sellAbove) {
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

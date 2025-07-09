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
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        double initialCapital = 10000;
        double capital = initialCapital;

        Params params = new Params();
        try {
            params = new ObjectMapper().readValue(strategy.getParamsJson(), Params.class);
        } catch (Exception ignored) {}

        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> candles = entry.getValue();

            boolean holding = false;
            double buyPrice = 0;
            int quantity = 0;

            for (Candle candle : candles) {
                if (!holding && candle.getClose() < params.buyBelow) {
                    buyPrice = candle.getClose();
                    quantity = (int) (capital / buyPrice);
                    double cost = quantity * buyPrice;
                    capital -= cost;

                    trades.add(Trade.builder()
                            .date(candle.getDate())
                            .symbol(symbol)
                            .action("BUY")
                            .price(buyPrice)
                            .quantity(quantity)
                            .totalCostPrice(cost)
                            .openingBalance(capital + cost)
                            .closingBalance(capital)
                            .nav(quantity * buyPrice)
                            .realizedProfit(0.0)
                            .strategy(strategy)
                            .build());

                    holding = true;
                } else if (holding && candle.getClose() > params.sellAbove) {
                    double sellPrice = candle.getClose();
                    double proceeds = quantity * sellPrice;
                    double profit = proceeds - (quantity * buyPrice);
                    double openingBalance = capital;
                    capital += proceeds;

                    trades.add(Trade.builder()
                            .date(candle.getDate())
                            .symbol(symbol)
                            .action("SELL")
                            .price(sellPrice)
                            .quantity(quantity)
                            .totalCostPrice(quantity * buyPrice)
                            .openingBalance(openingBalance)
                            .closingBalance(capital)
                            .nav(quantity * sellPrice)
                            .realizedProfit(profit)
                            .strategy(strategy)
                            .build());

                    holding = false;
                }
            }

            if (holding) {
                Candle last = candles.get(candles.size() - 1);
                double sellPrice = last.getClose();
                double proceeds = quantity * sellPrice;
                double profit = proceeds - (quantity * buyPrice);
                double openingBalance = capital;
                capital += proceeds;

                trades.add(Trade.builder()
                        .date(last.getDate())
                        .symbol(symbol)
                        .action("SELL")
                        .price(sellPrice)
                        .quantity(quantity)
                        .totalCostPrice(quantity * buyPrice)
                        .openingBalance(openingBalance)
                        .closingBalance(capital)
                        .nav(quantity * sellPrice)
                        .realizedProfit(profit)
                        .strategy(strategy)
                        .build());
            }
        }

        return BacktestResult.builder()
                .strategy(strategy)
                .initialEquity(initialCapital)
                .finalEquity(capital)
                .totalTrades(trades.size())
                .trades(trades)
                .build();
    }
}
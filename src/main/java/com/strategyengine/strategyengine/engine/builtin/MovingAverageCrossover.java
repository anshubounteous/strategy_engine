package com.strategyengine.strategyengine.engine.builtin;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.indicator.IndicatorService;
import com.strategyengine.strategyengine.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("Moving Average Crossover Strategy")
public class MovingAverageCrossover implements StrategyExecutor {

    @Autowired
    private IndicatorService indicatorService;

    @Override
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        double initialCapital = 10000;
        double capital = initialCapital;

        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> candles = entry.getValue();

            Map<Integer, Double> smaShort = indicatorService.calculateSMA(candles, 10);
            Map<Integer, Double> smaLong = indicatorService.calculateSMA(candles, 20);

            boolean holding = false;
            double buyPrice = 0;
            int quantity = 0;

            for (int i = 20; i < candles.size(); i++) {
                double shortSma = smaShort.getOrDefault(i, 0.0);
                double longSma = smaLong.getOrDefault(i, 0.0);
                Candle candle = candles.get(i);

                if (!holding && shortSma > longSma) {
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

                } else if (holding && shortSma < longSma) {
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
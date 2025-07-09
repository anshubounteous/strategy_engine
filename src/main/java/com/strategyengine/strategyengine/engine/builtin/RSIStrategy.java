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
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        double initialCapital = 10000;
        double capital = initialCapital;

        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> candles = entry.getValue();

            Map<Integer, Double> rsiMap = indicatorService.calculateRSI(candles, 14);

            boolean holding = false;
            double buyPrice = 0;
            int quantity = 0;

            for (int i = 14; i < candles.size(); i++) {
                double rsi = rsiMap.getOrDefault(i, 0.0);
                Candle candle = candles.get(i);

                if (!holding && rsi < 30) {
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

                } else if (holding && rsi > 70) {
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

package com.strategyengine.strategyengine.engine.builtin;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;

@Component("Day of the Week")
public class DayOfWeekStrategy implements StrategyExecutor {

    @Override
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        double initialCapital = 10000;
        double capital = initialCapital;

        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> candles = entry.getValue();

            boolean holding = false;
            double buyPrice = 0;
            int quantity = 0;

            for (Candle candle : candles) {
                DayOfWeek day = candle.getDate().getDayOfWeek();

                if (!holding && day == DayOfWeek.MONDAY) {
                    buyPrice = candle.getClose();
                    quantity = (int) (capital / buyPrice);
                    double cost = quantity * buyPrice;
                    capital -= cost;

                    trades.add(Trade.builder()
                            .date(candle.getDate())
                            .price(buyPrice)
                            .action("BUY")
                            .strategy(strategy)
                            .symbol(symbol)
                            .quantity(quantity)
                            .totalCostPrice(cost)
                            .openingBalance(capital + cost)
                            .closingBalance(capital)
                            .nav(quantity * buyPrice)
                            .realizedProfit(0.0)
                            .build());

                    holding = true;
                } else if (holding && day == DayOfWeek.FRIDAY) {
                    double sellPrice = candle.getClose();
                    double proceeds = quantity * sellPrice;
                    double profit = proceeds - (quantity * buyPrice);
                    double openingBalance = capital;
                    capital += proceeds;

                    trades.add(Trade.builder()
                            .date(candle.getDate())
                            .price(sellPrice)
                            .action("SELL")
                            .strategy(strategy)
                            .symbol(symbol)
                            .quantity(quantity)
                            .totalCostPrice(quantity * buyPrice)
                            .openingBalance(openingBalance)
                            .closingBalance(capital)
                            .nav(quantity * sellPrice)
                            .realizedProfit(profit)
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
                        .price(sellPrice)
                        .action("SELL")
                        .strategy(strategy)
                        .symbol(symbol)
                        .quantity(quantity)
                        .totalCostPrice(quantity * buyPrice)
                        .openingBalance(openingBalance)
                        .closingBalance(capital)
                        .nav(quantity * sellPrice)
                        .realizedProfit(profit)
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
package com.strategyengine.strategyengine.engine.builtin;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("Buy & Hold")
public class BuyAndHoldStrategy implements StrategyExecutor {

    @Override
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        double initialCapital = 500000;
        double capital = initialCapital;

        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> candles = entry.getValue();

            if (candles == null || candles.size() < 2) continue;

            Candle first = candles.get(0);
            Candle last = candles.get(candles.size() - 1);

            double buyPrice = first.getClose();
            double sellPrice = last.getClose();
            int quantity = (int) (capital / buyPrice);
            double cost = quantity * buyPrice;
            capital -= cost;

            trades.add(Trade.builder()
                    .date(first.getDate())
                    .symbol(symbol)
                    .action("BUY")
                    .price(buyPrice)
                    .quantity(quantity)
                    .totalCostPrice(cost)
                    .openingBalance(initialCapital)
                    .closingBalance(capital)
                    .nav(quantity * buyPrice)
                    .realizedProfit(0.0)
                    .strategy(strategy)
                    .build());

            double proceeds = quantity * sellPrice;
            double profit = proceeds - cost;
            double openingBalance = capital;
            capital += proceeds;

            trades.add(Trade.builder()
                    .date(last.getDate())
                    .symbol(symbol)
                    .action("SELL")
                    .price(sellPrice)
                    .quantity(quantity)
                    .totalCostPrice(cost)
                    .openingBalance(openingBalance)
                    .closingBalance(capital)
                    .nav(quantity * sellPrice)
                    .realizedProfit(profit)
                    .strategy(strategy)
                    .build());
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

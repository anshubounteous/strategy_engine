package com.strategyengine.strategyengine.engine.builtin;


import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;

@Component("Day of the Week")
public class DayOfWeekStrategy implements StrategyExecutor {

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;
        double capital = 10000;
        double buyPrice = 0;

        for (Candle candle : candles) {
            DayOfWeek day = candle.getDate().getDayOfWeek();

            if (!holding && day == DayOfWeek.MONDAY) {
                trades.add(Trade.builder().date(candle.getDate()).price(candle.getClose()).action("BUY").strategy(strategy).build());
                buyPrice = candle.getClose();
                holding = true;
            } else if (holding && day == DayOfWeek.FRIDAY) {
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

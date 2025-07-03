package com.strategyengine.strategyengine.engine.builtin;


import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("Volume Surge")
public class VolumeSurgeStrategy implements StrategyExecutor {

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();

        boolean holding = false;
        double capital = 10000;
        double buyPrice = 0;

        for (int i = 1; i < candles.size(); i++) {
            Candle prev = candles.get(i - 1);
            Candle curr = candles.get(i);

            boolean surge = curr.getVolume() > prev.getVolume() * 2;

            if (!holding && surge) {
                trades.add(Trade.builder().date(curr.getDate()).price(curr.getClose()).action("BUY").strategy(strategy).build());
                buyPrice = curr.getClose();
                holding = true;
            } else if (holding && !surge) {
                trades.add(Trade.builder().date(curr.getDate()).price(curr.getClose()).action("SELL").strategy(strategy).build());
                capital = capital * (curr.getClose() / buyPrice);
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

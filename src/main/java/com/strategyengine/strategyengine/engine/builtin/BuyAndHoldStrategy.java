package com.strategyengine.strategyengine.engine.builtin;


import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("Buy & Hold")
public class BuyAndHoldStrategy implements StrategyExecutor {

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<Trade> trades = new ArrayList<>();

        Candle first = candles.get(0);
        Candle last = candles.get(candles.size() - 1);

        trades.add(Trade.builder().date(first.getDate()).price(first.getClose()).action("BUY").strategy(strategy).build());
        trades.add(Trade.builder().date(last.getDate()).price(last.getClose()).action("SELL").strategy(strategy).build());

        double capital = 10000 * (last.getClose() / first.getClose());

        return BacktestResult.builder()
                .strategy(strategy)
                .initialEquity(10000)
                .finalEquity(capital)
                .totalTrades(2)
                .trades(trades)
                .build();
    }
}

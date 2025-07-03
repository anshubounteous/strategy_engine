package com.strategyengine.strategyengine.engine;


import com.strategyengine.strategyengine.model.BacktestResult;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.model.Strategy;

import java.util.List;

public interface StrategyExecutor {
    BacktestResult execute(List<Candle> candles, Strategy strategy);
}

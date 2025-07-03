package com.strategyengine.strategyengine.engine;


import com.strategyengine.strategyengine.model.BacktestResult;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.model.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StrategyEngine {

    @Autowired
    private Map<String, StrategyExecutor> strategyExecutors;

    public BacktestResult run(Strategy strategy, List<Candle> candles) {
        StrategyExecutor executor;

        // Choose executor based on strategyName
        if (strategy.getScript() != null && !strategy.getScript().isBlank()) {
            executor = strategyExecutors.get("dslStrategyExecutor"); // bean name
        } else {
            executor = strategyExecutors.get(strategy.getName()); // predefined name
        }

        if (executor == null) {
            throw new IllegalArgumentException("Unknown strategy: " + strategy.getName());
        }

        return executor.execute(candles, strategy);
    }
}

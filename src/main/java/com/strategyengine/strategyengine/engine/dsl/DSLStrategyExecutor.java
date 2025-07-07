
package com.strategyengine.strategyengine.engine.dsl;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.indicator.IndicatorService;
import com.strategyengine.strategyengine.model.*;
import com.strategyengine.strategyengine.parser.ScriptParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component("dslStrategyExecutor")
public class DSLStrategyExecutor implements StrategyExecutor {

    @Autowired
    private ScriptParserService parser;

    @Autowired
    private IndicatorService indicatorService;

    @Override
    public BacktestResult execute(List<Candle> candles, Strategy strategy) {
        List<CompositeRule> rules = parser.parse(strategy.getScript());

        Map<Integer, Map<String, Double>> indicatorCache = new HashMap<>();
        List<Trade> trades = new ArrayList<>();
        boolean holding = false;
        double initialCapital = 500000;
        double capital = initialCapital;
        double buyPrice = 0;

        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);

            for (CompositeRule rule : rules) {
                boolean allMatched = true;
                for (Condition c : rule.getConditions()) {
                    double leftVal = getValue(candles, c.getLeft(), c.getLeftArg(), i, indicatorCache);
                    double rightVal = getValue(candles, c.getRight(), c.getRightArg(), i, indicatorCache);

                    if (!evaluate(leftVal, rightVal, c.getOperator())) {
                        allMatched = false;
                        break;
                    }
                }

                if (allMatched) {
                    if ("BUY".equalsIgnoreCase(rule.getAction()) && !holding) {
                        trades.add(Trade.builder()
                                .date(candle.getDate())
                                .price(candle.getClose())
                                .action("BUY")
                                .strategy(strategy)
                                .build());
                        buyPrice = candle.getClose();
                        holding = true;
                    } else if ("SELL".equalsIgnoreCase(rule.getAction()) && holding) {
                        trades.add(Trade.builder()
                                .date(candle.getDate())
                                .price(candle.getClose())
                                .action("SELL")
                                .strategy(strategy)
                                .build());
                        capital = capital * (candle.getClose() / buyPrice);
                        holding = false;
                    }
                }
            }
        }

        if (holding) {
            Candle lastCandle = candles.get(candles.size() - 1);
            capital = capital * (lastCandle.getClose() / buyPrice);
        }

        return BacktestResult.builder()
                .initialEquity(initialCapital)
                .finalEquity(capital)
                .totalTrades(trades.size())
                .trades(trades)
                .strategy(strategy)
                .build();
    }

    private double getValue(List<Candle> candles, String type, Integer arg, int index,
                            Map<Integer, Map<String, Double>> cache) {

        cache.putIfAbsent(index, new HashMap<>());

        if ("SMA".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("SMA" + arg,
                    key -> indicatorService.calculateSMA(candles, arg).getOrDefault(index, 0.0));
        } else if ("RSI".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("RSI" + arg,
                    key -> indicatorService.calculateRSI(candles, arg).getOrDefault(index, 0.0));
        } else if ("VALUE".equalsIgnoreCase(type)) {
            return arg;
        } else if ("CLOSE".equalsIgnoreCase(type)) {
            return candles.get(index).getClose();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private boolean evaluate(double left, double right, String op) {
        return switch (op) {
            case ">" -> left > right;
            case "<" -> left < right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            case "==" -> left == right;
            case "!=" -> left != right;
            default -> false;
        };
    }
}

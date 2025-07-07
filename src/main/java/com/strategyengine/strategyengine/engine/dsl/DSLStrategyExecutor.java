package com.strategyengine.strategyengine.engine.dsl;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.indicator.IndicatorService;
import com.strategyengine.strategyengine.model.*;
import com.strategyengine.strategyengine.parser.ScriptParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        System.out.println(rules);
        Map<Integer, Map<String, Double>> indicatorCache = new HashMap<>();
        List<Trade> trades = new ArrayList<>();

        boolean holding = false;
        double initialCapital = 500000;
        double capital = initialCapital;
        double buyPrice = 0;
        int quantity = 0;
        double openingBalance = initialCapital;

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
                        buyPrice = candle.getClose();
                        quantity = (int) (capital / buyPrice);

                        System.out.println(buyPrice+" "+quantity);

                        if (quantity < 1) {
                            System.out.println("Skipped BUY on " + candle.getDate() + " — capital: " + capital + ", price: " + buyPrice);
                            continue; // skip if not enough capital
                        }

                        trades.add(Trade.builder()
                                .date(candle.getDate())
                                .price(buyPrice)
                                .action("BUY")
                                .strategy(strategy)
                                .symbol(strategy.getSymbol())
                                .quantity(quantity)
                                .totalCostPrice(quantity * buyPrice)
                                .openingBalance(openingBalance)
                                .closingBalance(capital)
                                .nav(quantity * buyPrice)
                                .realizedProfit(0.0)
                                .build());

                        holding = true;
                    } else if ("SELL".equalsIgnoreCase(rule.getAction()) && holding) {
                        double sellPrice = candle.getClose();
                        double profit = quantity * (sellPrice - buyPrice);
                        capital = quantity * sellPrice;

                        trades.add(Trade.builder()
                                .date(candle.getDate())
                                .price(sellPrice)
                                .action("SELL")
                                .strategy(strategy)
                                .symbol(strategy.getSymbol())
                                .quantity(quantity)
                                .totalCostPrice(quantity * buyPrice)
                                .openingBalance(openingBalance)
                                .closingBalance(capital)
                                .nav(quantity * sellPrice)
                                .realizedProfit(profit)
                                .build());

                        openingBalance = capital;
                        holding = false;
                    }
                }
            }
        }

        // Sell at the end if still holding
        if (holding) {
            Candle lastCandle = candles.get(candles.size() - 1);
            double sellPrice = lastCandle.getClose();
            double profit = quantity * (sellPrice - buyPrice);
            capital = quantity * sellPrice;

            trades.add(Trade.builder()
                    .date(lastCandle.getDate())
                    .price(sellPrice)
                    .action("SELL")
                    .strategy(strategy)
                    .symbol(strategy.getSymbol())
                    .quantity(quantity)
                    .totalCostPrice(quantity * buyPrice)
                    .openingBalance(openingBalance)
                    .closingBalance(capital)
                    .nav(quantity * sellPrice)
                    .realizedProfit(profit)
                    .build());
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

        if(!type.equals("SMA") && !type.equals("RSI") && !type.equals("VALUE") && !type.equals("CLOSE")){
            arg = Integer.parseInt(type);
            type="VALUE";
        }
        Integer finalArg=arg;

        if ("SMA".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("SMA" + arg,
                    key -> indicatorService.calculateSMA(candles, finalArg).getOrDefault(index, 0.0));
        } else if ("RSI".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("RSI" + arg,
                    key -> indicatorService.calculateRSI(candles, finalArg).getOrDefault(index, 0.0));
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

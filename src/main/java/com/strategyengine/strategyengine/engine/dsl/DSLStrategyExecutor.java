package com.strategyengine.strategyengine.engine.dsl;

import com.strategyengine.strategyengine.engine.StrategyExecutor;
import com.strategyengine.strategyengine.indicator.IndicatorService;
import com.strategyengine.strategyengine.model.*;
import com.strategyengine.strategyengine.parser.ScriptParserService;
import com.strategyengine.strategyengine.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component("dslStrategyExecutor")
public class DSLStrategyExecutor implements StrategyExecutor {

    @Autowired
    private ScriptParserService parser;

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public BacktestResult execute(HashMap<String, List<Candle>> candleMap, Strategy strategy) {
        List<CompositeRule> rules = parser.parse(strategy.getScript());
        Map<String, Map<Integer, Map<String, Double>>> indicatorCachePerSymbol = new HashMap<>();
        Map<String, Boolean> holding = new HashMap<>();
        Map<String, Double> buyPrice = new HashMap<>();
        Map<String, Integer> quantity = new HashMap<>();
        List<Trade> trades = new ArrayList<>();

        double initialCapital = 500000;
        double capital = initialCapital;

        int totalDays = candleMap.values().stream().findFirst().map(List::size).orElse(0);

        for (int day = 0; day < totalDays; day++) {
            List<String> buySymbolsToday = new ArrayList<>();
            Map<String, Candle> candleOfDay = new HashMap<>();

            for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
                String symbol = entry.getKey();
                List<Candle> candles = entry.getValue();

                if (day >= candles.size()) continue;

                Candle candle = candles.get(day);
                candleOfDay.put(symbol, candle);

                boolean alreadyBought = false;

                for (CompositeRule rule : rules) {
                    boolean allMatched = true;

                    for (Condition c : rule.getConditions()) {
                        double leftVal = getValue(candles, c.getLeft(), c.getLeftArg(), day, getCache(indicatorCachePerSymbol, symbol));
                        double rightVal = getValue(candles, c.getRight(), c.getRightArg(), day, getCache(indicatorCachePerSymbol, symbol));

                        if (!evaluate(leftVal, rightVal, c.getOperator())) {
                            allMatched = false;
                            break;
                        }
                    }

                    if (allMatched) {
                        if ("BUY".equalsIgnoreCase(rule.getAction())
                                && !holding.getOrDefault(symbol, false)
                                && !buySymbolsToday.contains(symbol)
                                && !alreadyBought) {
                            buySymbolsToday.add(symbol);
                            alreadyBought = true;
                            break;
                        }

                        if ("SELL".equalsIgnoreCase(rule.getAction()) && holding.getOrDefault(symbol, false)) {
                            double sell = candle.getClose();
                            int qty = quantity.get(symbol);
                            double cost = qty * buyPrice.get(symbol);
                            double proceeds = qty * sell;
                            double profit = proceeds - cost;

                            double openingBalance = capital;
                            capital += proceeds;

                            trades.add(Trade.builder()
                                    .date(candle.getDate())
                                    .symbol(symbol)
                                    .action("SELL")
                                    .price(sell)
                                    .quantity(qty)
                                    .totalCostPrice(cost)
                                    .openingBalance(openingBalance)
                                    .closingBalance(capital)
                                    .nav(qty * sell)
                                    .realizedProfit(profit)
                                    .build());

                            holding.put(symbol, false);
                            break;
                        }
                    }
                }
            }

            if (!buySymbolsToday.isEmpty()) {
                // Calculate total weight based on totalCapital and riskFactor
                double totalWeight = 0.0;
                Map<String, Double> weights = new HashMap<>();

                for (String symbol : buySymbolsToday) {
                    Optional<Company> companyOpt = companyRepository.findBySymbol(symbol);
                    if (companyOpt.isPresent()) {
                        Company company = companyOpt.get();
                        BigDecimal totalCapitalBD = company.getTotalCapital();
                        Double riskFactor = company.getRiskFactor() != null ? company.getRiskFactor() : 0.0;

                        double weight = totalCapitalBD != null ? totalCapitalBD.doubleValue() * (1 - riskFactor) : 0.0;
                        weights.put(symbol, weight);
                        totalWeight += weight;
                    }
                }

                for (String symbol : buySymbolsToday) {
                    Candle candle = candleOfDay.get(symbol);
                    double price = candle.getClose();
                    double weight = weights.getOrDefault(symbol, 0.0);
                    double allocatedCapital = (totalWeight > 0) ? capital * (weight / totalWeight) : 0;
                    int qty = (int) (allocatedCapital / price);

                    if (qty <= 0) continue;

                    double cost = qty * price;
                    double openingBalance = capital;
                    capital -= cost;

                    trades.add(Trade.builder()
                            .date(candle.getDate())
                            .symbol(symbol)
                            .action("BUY")
                            .price(price)
                            .quantity(qty)
                            .totalCostPrice(cost)
                            .openingBalance(openingBalance)
                            .closingBalance(capital)
                            .nav(qty * price)
                            .realizedProfit(0.0)
                            .build());

                    holding.put(symbol, true);
                    buyPrice.put(symbol, price);
                    quantity.put(symbol, qty);
                }
            }
        }

        // Final day forced SELL
        for (Map.Entry<String, List<Candle>> entry : candleMap.entrySet()) {
            String symbol = entry.getKey();
            if (holding.getOrDefault(symbol, false)) {
                List<Candle> candles = entry.getValue();
                if (candles.isEmpty()) continue;

                Candle last = candles.get(candles.size() - 1);
                double sell = last.getClose();
                int qty = quantity.getOrDefault(symbol, 0);
                double cost = qty * buyPrice.get(symbol);
                double proceeds = qty * sell;
                double profit = proceeds - cost;

                double openingBalance = capital;
                capital += proceeds;

                trades.add(Trade.builder()
                        .date(last.getDate())
                        .symbol(symbol)
                        .action("SELL")
                        .price(sell)
                        .quantity(qty)
                        .totalCostPrice(cost)
                        .openingBalance(openingBalance)
                        .closingBalance(capital)
                        .nav(qty * sell)
                        .realizedProfit(profit)
                        .build());

                holding.put(symbol, false);
            }
        }

        return BacktestResult.builder()
                .initialEquity(initialCapital)
                .finalEquity(capital)
                .totalTrades(trades.size())
                .trades(trades)
                .strategy(strategy)
                .build();
    }

    private Map<Integer, Map<String, Double>> getCache(Map<String, Map<Integer, Map<String, Double>>> cacheMap, String symbol) {
        return cacheMap.computeIfAbsent(symbol, k -> new HashMap<>());
    }

    private double getValue(List<Candle> candles, String type, Integer arg, int index, Map<Integer, Map<String, Double>> cache) {
        cache.putIfAbsent(index, new HashMap<>());

        if (!type.equalsIgnoreCase("SMA") && !type.equalsIgnoreCase("RSI") &&
                !type.equalsIgnoreCase("VALUE") && !type.equalsIgnoreCase("CLOSE")) {
            arg = Integer.parseInt(type);
            type = "VALUE";
        }
        final Integer finalArg = arg;

        if ("SMA".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("SMA" + arg,
                    k -> indicatorService.calculateSMA(candles, finalArg).getOrDefault(index, 0.0));
        } else if ("RSI".equalsIgnoreCase(type)) {
            return cache.get(index).computeIfAbsent("RSI" + arg,
                    k -> indicatorService.calculateRSI(candles, finalArg).getOrDefault(index, 0.0));
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
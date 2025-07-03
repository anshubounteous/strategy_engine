package com.strategyengine.strategyengine.indicator;


import com.strategyengine.strategyengine.model.Candle;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndicatorService {

    // Returns SMA(period) for each index starting at index = period - 1
    public Map<Integer, Double> calculateSMA(List<Candle> candles, int period) {
        Map<Integer, Double> smaMap = new HashMap<>();
        double sum = 0;

        for (int i = 0; i < candles.size(); i++) {
            sum += candles.get(i).getClose();

            if (i >= period) {
                sum -= candles.get(i - period).getClose();
            }

            if (i >= period - 1) {
                smaMap.put(i, sum / period);
            }
        }
        return smaMap;
    }

    // Returns RSI(index) where index >= period
    public Map<Integer, Double> calculateRSI(List<Candle> candles, int period) {
        Map<Integer, Double> rsiMap = new HashMap<>();
        double gain = 0, loss = 0;

        for (int i = 1; i <= period; i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            if (change > 0) gain += change;
            else loss -= change;
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;
        rsiMap.put(period, calculateRSIValue(avgGain, avgLoss));

        for (int i = period + 1; i < candles.size(); i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            double currentGain = Math.max(change, 0);
            double currentLoss = Math.max(-change, 0);

            avgGain = (avgGain * (period - 1) + currentGain) / period;
            avgLoss = (avgLoss * (period - 1) + currentLoss) / period;

            rsiMap.put(i, calculateRSIValue(avgGain, avgLoss));
        }

        return rsiMap;
    }

    private double calculateRSIValue(double avgGain, double avgLoss) {
        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }
}

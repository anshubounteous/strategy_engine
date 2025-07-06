package com.strategyengine.strategyengine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.strategyengine.strategyengine.model.Candle;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class YahooFinanceParser {

    public static List<Candle> parse(JsonNode root, String symbol) {
        List<Candle> candles = new ArrayList<>();

        JsonNode result = root.path("chart").path("result").get(0);
        JsonNode timestamps = result.path("timestamp");

        JsonNode indicators = result.path("indicators").path("quote").get(0);
        JsonNode opens = indicators.path("open");
        JsonNode highs = indicators.path("high");
        JsonNode lows = indicators.path("low");
        JsonNode closes = indicators.path("close");
        JsonNode volumes = indicators.path("volume");

        for (int i = 0; i < timestamps.size(); i++) {
            try {
                LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                Double open = opens.get(i).isNull() ? null : opens.get(i).asDouble();
                Double high = highs.get(i).isNull() ? null : highs.get(i).asDouble();
                Double low = lows.get(i).isNull() ? null : lows.get(i).asDouble();
                Double close = closes.get(i).isNull() ? null : closes.get(i).asDouble();
                Long volume = volumes.get(i).isNull() ? null : volumes.get(i).asLong();

                if (open == null || high == null || low == null || close == null || volume == null) {
                    continue; // skip bad rows
                }

                Candle candle = Candle.builder()
                        .symbol(symbol)
                        .date(date)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();

                candles.add(candle);
            } catch (Exception e) {
                // skip corrupt entries
            }
        }

        return candles;
    }
}
